package tn.esprit.new_timetableservice.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tn.esprit.new_timetableservice.dto.*;
import tn.esprit.new_timetableservice.entities.*;
import tn.esprit.new_timetableservice.entities.Class;
import tn.esprit.new_timetableservice.repositories.*;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class TimetableService {
    private static final Logger logger = LoggerFactory.getLogger(TimetableService.class);

    @Value("${timetable.population.size:50}")
    private int populationSize;

    @Value("${timetable.mutation.rate:0.01}")
    private double mutationRate;

    @Value("${timetable.max.generations:500}")
    private int maxGenerations;

    @Value("${timetable.timeout.seconds:15}")
    private int timeoutSeconds;

    @Autowired private ProgramRepository programRepository;
    @Autowired private ProgramSubjectRepository programSubjectRepository;
    @Autowired private ClassRepository classRepository;
    @Autowired private TeacherRepository teacherRepository;
    @Autowired private ClassroomRepository classroomRepository;
    @Autowired private TimeSlotRepository timeSlotRepository;
    @Autowired private TeacherAvailabilityRepository teacherAvailabilityRepository;
    @Autowired private TimetableRepository timetableRepository;
    @Autowired private ScheduleRepository scheduleRepository;
    @Autowired private SchoolRepository schoolRepository;
    @Transactional
    public TimetableDTO generateTimetable(GenerateTimetableRequestDTO request) {
        logger.info("Starting timetable generation for schoolId: {}", request.getSchoolId());
        try {
            // Validate inputs
            Long schoolId = request.getSchoolId();
            logger.debug("Validating programClassCounts: {}", request.getProgramClassCounts());
            Map<Long, Integer> programClassCounts = request.getProgramClassCounts();
            List<Program> programs = programRepository.findAllById(programClassCounts.keySet());
            if (programs.size() != programClassCounts.size()) {
                logger.error("Invalid program IDs: {}", programClassCounts.keySet());
                throw new IllegalArgumentException("Invalid program IDs: " + programClassCounts.keySet());
            }

            // Delete existing data in correct order
            logger.debug("Deleting schedules for schoolId: {}", schoolId);
            int schedulesDeleted = scheduleRepository.deleteByTimetableSchoolId(schoolId);
            logger.info("Deleted {} schedules for schoolId: {}", schedulesDeleted, schoolId);

            logger.debug("Deleting timetable for schoolId: {}", schoolId);
            int timetablesDeleted = timetableRepository.deleteBySchoolId(schoolId);
            logger.info("Deleted {} timetables for schoolId: {}", timetablesDeleted, schoolId);

            // Verify deletion
            Optional<Timetable> existingTimetable = timetableRepository.findBySchoolId(schoolId);
            if (existingTimetable.isPresent()) {
                logger.error("Timetable with schoolId: {} still exists after deletion", schoolId);
                throw new IllegalStateException("Failed to delete existing timetable for schoolId: " + schoolId);
            }
            logger.debug("Verified no timetable exists for schoolId: {}", schoolId);

            logger.debug("Deleting classes for schoolId: {}", schoolId);
            int classesDeleted = classRepository.deleteBySchoolId(schoolId);
            logger.info("Deleted {} classes for schoolId: {}", classesDeleted, schoolId);

            // Create new classes
            logger.debug("Creating new classes for schoolId: {}", schoolId);
            List<Class> classes = createClasses(schoolId, programClassCounts);
            logger.info("Created {} classes for schoolId: {}", classes.size(), schoolId);

            // Fetch school data
            logger.debug("Fetching school data for schoolId: {}", schoolId);
            List<TimeSlot> timeSlots = timeSlotRepository.findBySchoolId(schoolId);
            List<Teacher> teachers = teacherRepository.findBySchoolId(schoolId);
            List<Classroom> classrooms = classroomRepository.findBySchoolId(schoolId);
            Map<Long, List<TeacherAvailability>> teacherAvailabilities = teacherAvailabilityRepository.findAll().stream()
                    .collect(Collectors.groupingBy(ta -> ta.getTeacher().getId()));
            logger.info("Fetched {} time slots, {} teachers, {} classrooms", timeSlots.size(), teachers.size(), classrooms.size());

            // Get program requirements
            logger.debug("Fetching program requirements");
            Map<Long, List<ProgramSubject>> programRequirements = programs.stream()
                    .collect(Collectors.toMap(Program::getId, p -> programSubjectRepository.findByProgramId(p.getId())));
            logger.info("Fetched requirements for {} programs", programRequirements.size());

            // Initialize population
            logger.debug("Initializing population");
            List<GeneticTimetable> population = initializePopulation(classes, programRequirements, timeSlots, teachers, classrooms, teacherAvailabilities);
            logger.info("Initialized population with {} timetables", population.size());

            // Optimize using genetic algorithm
            logger.debug("Starting genetic algorithm optimization");
            GeneticTimetable bestTimetable = optimizeTimetable(population, classes, timeSlots, teachers, classrooms, teacherAvailabilities);
            logger.info("Optimization complete, best timetable has {} schedules", bestTimetable.getSchedules().size());

            // Save timetable
            logger.debug("Saving timetable for schoolId: {}", schoolId);
            Timetable savedTimetable = saveTimetable(bestTimetable, schoolId);
            logger.info("Timetable saved successfully with ID: {} for schoolId: {}", savedTimetable.getId(), schoolId);

            return mapToTimetableDTO(savedTimetable);
        } catch (Exception e) {
            logger.error("Failed to generate timetable for schoolId: {}", request.getSchoolId(), e);
            throw new RuntimeException("Timetable generation failed: " + e.getMessage(), e);
        }
    }

    private List<Class> createClasses(Long schoolId, Map<Long, Integer> programClassCounts) {
        logger.debug("Creating classes for schoolId: {}", schoolId);
        List<Class> classes = new ArrayList<>();
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new IllegalArgumentException("School not found: " + schoolId));
        for (Map.Entry<Long, Integer> entry : programClassCounts.entrySet()) {
            Long programId = entry.getKey();
            int count = entry.getValue();
            Program program = programRepository.findById(programId)
                    .orElseThrow(() -> new IllegalArgumentException("Program not found: " + programId));
            for (int i = 1; i <= count; i++) {
                Class clazz = new Class();
                clazz.setSchool(school);
                clazz.setProgram(program);
                clazz.setName(program.getName() + " Class " + i);
                clazz.setStudentCount(35); // Default from DB
                classes.add(classRepository.save(clazz));
                logger.debug("Created class: {} for programId: {}", clazz.getName(), programId);
            }
        }
        return classes;
    }

    private List<GeneticTimetable> initializePopulation(
            List<Class> classes,
            Map<Long, List<ProgramSubject>> requirements,
            List<TimeSlot> timeSlots,
            List<Teacher> teachers,
            List<Classroom> classrooms,
            Map<Long, List<TeacherAvailability>> teacherAvailabilities) {
        logger.debug("Initializing population with {} classes", classes.size());
        List<GeneticTimetable> population = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < populationSize; i++) {
            GeneticTimetable timetable = new GeneticTimetable();
            for (Class clazz : classes) {
                List<ProgramSubject> programSubjects = requirements.get(clazz.getProgram().getId());
                for (ProgramSubject ps : programSubjects) {
                    for (int h = 0; h < ps.getHoursPerWeek(); h++) {
                        Schedule schedule = createRandomSchedule(
                                clazz, ps.getSubject(), timeSlots, teachers, classrooms, teacherAvailabilities, random, timetable);
                        if (schedule != null) {
                            timetable.getSchedules().add(schedule);
                        }
                    }
                }
            }
            population.add(timetable);
            logger.debug("Created timetable {} with {} schedules", i, timetable.getSchedules().size());
        }
        return population;
    }

    private Schedule createRandomSchedule(
            Class clazz,
            Subject subject,
            List<TimeSlot> timeSlots,
            List<Teacher> teachers,
            List<Classroom> classrooms,
            Map<Long, List<TeacherAvailability>> teacherAvailabilities,
            Random random,
            GeneticTimetable timetable) {
        logger.debug("Creating random schedule for classId: {}, subjectId: {}", clazz.getId(), subject.getId());
        List<Teacher> eligibleTeachers = teachers.stream()
                .filter(t -> t.getSubject().getId().equals(subject.getId()) &&
                        t.getPrograms().stream().anyMatch(p -> p.getId().equals(clazz.getProgram().getId())))
                .filter(t -> isTeacherAvailable(t, teacherAvailabilities.getOrDefault(t.getId(), Collections.emptyList()), timetable))
                .collect(Collectors.toList());
        if (eligibleTeachers.isEmpty()) {
            logger.warn("No eligible teachers for subject {} in program {}", subject.getId(), clazz.getProgram().getId());
            return null;
        }

        String requiredRoomType = subject.getRoomType();
        List<Classroom> eligibleClassrooms = classrooms.stream()
                .filter(c -> c.getType().equalsIgnoreCase(requiredRoomType))
                .collect(Collectors.toList());
        if (eligibleClassrooms.isEmpty()) {
            logger.warn("No classrooms of type {} for subject {}", requiredRoomType, subject.getId());
            return null;
        }

        Collections.shuffle(timeSlots, random);
        for (TimeSlot timeSlot : timeSlots) {
            if (timetable.getSchedules().stream().noneMatch(s ->
                    s.getClassEntity().getId().equals(clazz.getId()) && s.getTimeSlot().getId().equals(timeSlot.getId()))) {
                Teacher teacher = eligibleTeachers.get(random.nextInt(eligibleTeachers.size()));
                Classroom classroom = eligibleClassrooms.get(random.nextInt(eligibleClassrooms.size()));
                if (timetable.getSchedules().stream().noneMatch(s ->
                        (s.getTeacher().getId().equals(teacher.getId()) ||
                                s.getClassroom().getId().equals(classroom.getId())) &&
                                s.getTimeSlot().getId().equals(timeSlot.getId()))) {
                    logger.debug("Assigned schedule: classId={}, teacherId={}, classroomId={}, timeSlotId={}",
                            clazz.getId(), teacher.getId(), classroom.getId(), timeSlot.getId());
                    return new Schedule(null, clazz, subject, teacher, classroom, timeSlot);
                }
            }
        }
        logger.debug("No valid schedule found for classId: {}, subjectId: {}", clazz.getId(), subject.getId());
        return null;
    }

    private boolean isTeacherAvailable(Teacher teacher, List<TeacherAvailability> availabilities, GeneticTimetable timetable) {
        int assignedHours = timetable.getSchedules().stream()
                .filter(s -> s.getTeacher().getId().equals(teacher.getId()))
                .mapToInt(s -> 1)
                .sum();
        if (assignedHours >= teacher.getMaxHoursPerWeek()) {
            return false;
        }
        return availabilities.isEmpty() ||
                availabilities.stream().anyMatch(ta -> ta.getIsAvailable());
    }

    private GeneticTimetable optimizeTimetable(
            List<GeneticTimetable> population,
            List<Class> classes,
            List<TimeSlot> timeSlots,
            List<Teacher> teachers,
            List<Classroom> classrooms,
            Map<Long, List<TeacherAvailability>> teacherAvailabilities) {
        logger.debug("Starting timetable optimization");
        long startTime = System.currentTimeMillis();
        Random random = new Random();

        for (int gen = 0; gen < maxGenerations && (System.currentTimeMillis() - startTime) / 1000 < timeoutSeconds; gen++) {
            List<Integer> fitnessScores = population.stream()
                    .map(t -> calculateFitness(t, classes, timeSlots))
                    .collect(Collectors.toList());

            GeneticTimetable best = population.get(fitnessScores.indexOf(Collections.max(fitnessScores)));
            if (isOptimal(best, classes)) {
                logger.info("Optimal timetable found at generation {}", gen);
                return repairTimetable(best, classes, timeSlots, teachers, classrooms, teacherAvailabilities);
            }

            List<GeneticTimetable> newPopulation = new ArrayList<>();
            List<Integer> sortedIndices = IntStream.range(0, fitnessScores.size())
                    .boxed()
                    .sorted((i, j) -> fitnessScores.get(j).compareTo(fitnessScores.get(i)))
                    .collect(Collectors.toList());
            for (int i = 0; i < 2 && i < population.size(); i++) {
                newPopulation.add(new GeneticTimetable(population.get(sortedIndices.get(i)).getSchedules()));
            }

            while (newPopulation.size() < populationSize) {
                GeneticTimetable parent1 = selectParent(population, fitnessScores, random);
                GeneticTimetable parent2 = selectParent(population, fitnessScores, random);
                GeneticTimetable child = crossover(parent1, parent2, random);
                if (random.nextDouble() < mutationRate) {
                    mutate(child, timeSlots, teachers, classrooms, teacherAvailabilities, random);
                }
                newPopulation.add(child);
            }
            population = newPopulation;

            if (gen % 100 == 0) {
                logger.info("Generation {}: Best Fitness = {}", gen, calculateFitness(best, classes, timeSlots));
            }
        }

        GeneticTimetable best = population.stream()
                .max(Comparator.comparingInt(t -> calculateFitness(t, classes, timeSlots)))
                .orElse(population.get(0));
        logger.info("Optimization ended, best fitness: {}", calculateFitness(best, classes, timeSlots));
        return repairTimetable(best, classes, timeSlots, teachers, classrooms, teacherAvailabilities);
    }

    private int calculateFitness(GeneticTimetable timetable, List<Class> classes, List<TimeSlot> timeSlots) {
        logger.debug("Calculating fitness for timetable with {} schedules", timetable.getSchedules().size());
        int score = 0;

        Map<Long, Map<Long, Integer>> classSubjectHours = new HashMap<>();
        for (Class clazz : classes) {
            classSubjectHours.putIfAbsent(clazz.getId(), new HashMap<>());
        }
        for (Schedule s : timetable.getSchedules()) {
            classSubjectHours.get(s.getClassEntity().getId())
                    .merge(s.getSubject().getId(), 1, Integer::sum);
        }
        for (Class clazz : classes) {
            List<ProgramSubject> requirements = programSubjectRepository.findByProgramId(clazz.getProgram().getId());
            for (ProgramSubject ps : requirements) {
                int actual = classSubjectHours.getOrDefault(clazz.getId(), Collections.emptyMap())
                        .getOrDefault(ps.getSubject().getId(), 0);
                if (actual == ps.getHoursPerWeek()) {
                    score += 100;
                } else {
                    score -= Math.abs(actual - ps.getHoursPerWeek()) * 1000;
                }
            }
        }

        Map<Long, Set<Long>> teacherSlots = new HashMap<>();
        Map<Long, Set<Long>> classroomSlots = new HashMap<>();
        Map<Long, Set<Long>> classSlots = new HashMap<>();
        for (Schedule s : timetable.getSchedules()) {
            Long timeSlotId = s.getTimeSlot().getId();
            teacherSlots.computeIfAbsent(timeSlotId, k -> new HashSet<>());
            classroomSlots.computeIfAbsent(timeSlotId, k -> new HashSet<>());
            classSlots.computeIfAbsent(timeSlotId, k -> new HashSet<>());
            if (!teacherSlots.get(timeSlotId).add(s.getTeacher().getId())) score -= 1000;
            if (!classroomSlots.get(timeSlotId).add(s.getClassroom().getId())) score -= 1000;
            if (!classSlots.get(timeSlotId).add(s.getClassEntity().getId())) score -= 1000;
        }

        for (Schedule s : timetable.getSchedules()) {
            if (s.getClassroom().getType().equalsIgnoreCase(s.getSubject().getRoomType())) {
                score += 10;
            } else {
                score -= 1000;
            }
        }

        for (Schedule s : timetable.getSchedules()) {
            boolean eligible = s.getTeacher().getSubject().getId().equals(s.getSubject().getId()) &&
                    s.getTeacher().getPrograms().stream().anyMatch(p -> p.getId().equals(s.getClassEntity().getProgram().getId()));
            score += eligible ? 10 : -1000;
        }

        Map<Long, Integer> teacherHours = timetable.getSchedules().stream()
                .collect(Collectors.groupingBy(
                        s -> s.getTeacher().getId(),
                        Collectors.summingInt(s -> 1)));
        for (Map.Entry<Long, Integer> entry : teacherHours.entrySet()) {
            Teacher t = teacherRepository.findById(entry.getKey()).orElseThrow();
            if (entry.getValue() <= t.getMaxHoursPerWeek()) {
                score += 10;
            } else {
                score -= (entry.getValue() - t.getMaxHoursPerWeek()) * 1000;
            }
        }

        double avgHours = teacherHours.values().stream().mapToInt(Integer::intValue).average().orElse(0);
        double variance = teacherHours.values().stream()
                .mapToDouble(h -> Math.pow(h - avgHours, 2))
                .average()
                .orElse(0);
        score -= (int) variance * 10;

        for (Class clazz : classes) {
            Map<Long, List<Schedule>> subjectSchedules = timetable.getSchedules().stream()
                    .filter(s -> s.getClassEntity().getId().equals(clazz.getId()))
                    .collect(Collectors.groupingBy(s -> s.getSubject().getId()));
            for (List<Schedule> schedules : subjectSchedules.values()) {
                Set<String> days = schedules.stream()
                        .map(s -> s.getTimeSlot().getDay())
                        .collect(Collectors.toSet());
                score += days.size() * 5;
            }
        }

        logger.debug("Fitness score: {}", score);
        return score;
    }

    private boolean isOptimal(GeneticTimetable timetable, List<Class> classes) {
        logger.debug("Checking if timetable is optimal");
        Map<Long, Map<Long, Integer>> classSubjectHours = timetable.getSchedules().stream()
                .collect(Collectors.groupingBy(
                        s -> s.getClassEntity().getId(),
                        Collectors.groupingBy(
                                s -> s.getSubject().getId(),
                                Collectors.summingInt(s -> 1))));
        for (Class clazz : classes) {
            List<ProgramSubject> requirements = programSubjectRepository.findByProgramId(clazz.getProgram().getId());
            for (ProgramSubject ps : requirements) {
                int actual = classSubjectHours.getOrDefault(clazz.getId(), Collections.emptyMap())
                        .getOrDefault(ps.getSubject().getId(), 0);
                if (actual != ps.getHoursPerWeek()) return false;
            }
        }

        Map<Long, Set<Long>> teacherSlots = new HashMap<>();
        Map<Long, Set<Long>> classroomSlots = new HashMap<>();
        Map<Long, Set<Long>> classSlots = new HashMap<>();
        for (Schedule s : timetable.getSchedules()) {
            Long timeSlotId = s.getTimeSlot().getId();
            if (!teacherSlots.computeIfAbsent(timeSlotId, k -> new HashSet<>()).add(s.getTeacher().getId()) ||
                    !classroomSlots.computeIfAbsent(timeSlotId, k -> new HashSet<>()).add(s.getClassroom().getId()) ||
                    !classSlots.computeIfAbsent(timeSlotId, k -> new HashSet<>()).add(s.getClassEntity().getId())) {
                return false;
            }
            if (!s.getClassroom().getType().equalsIgnoreCase(s.getSubject().getRoomType())) {
                return false;
            }
            if (!s.getTeacher().getSubject().getId().equals(s.getSubject().getId()) ||
                    s.getTeacher().getPrograms().stream().noneMatch(p -> p.getId().equals(s.getClassEntity().getProgram().getId()))) {
                return false;
            }
        }

        Map<Long, Integer> teacherHours = timetable.getSchedules().stream()
                .collect(Collectors.groupingBy(
                        s -> s.getTeacher().getId(),
                        Collectors.summingInt(s -> 1)));
        for (Map.Entry<Long, Integer> entry : teacherHours.entrySet()) {
            Teacher t = teacherRepository.findById(entry.getKey()).orElseThrow();
            if (entry.getValue() > t.getMaxHoursPerWeek()) return false;
        }

        logger.debug("Timetable is optimal");
        return true;
    }

    private GeneticTimetable selectParent(List<GeneticTimetable> population, List<Integer> fitnessScores, Random random) {
        logger.debug("Selecting parent");
        int tournamentSize = 3;
        List<Integer> tournament = new ArrayList<>();
        for (int i = 0; i < tournamentSize; i++) {
            tournament.add(random.nextInt(population.size()));
        }
        return tournament.stream()
                .map(population::get)
                .max(Comparator.comparingInt(t -> fitnessScores.get(population.indexOf(t))))
                .orElse(population.get(0));
    }

    private GeneticTimetable crossover(GeneticTimetable parent1, GeneticTimetable parent2, Random random) {
        logger.debug("Performing crossover");
        GeneticTimetable child = new GeneticTimetable();
        Map<Long, List<Schedule>> p1ByClass = parent1.getSchedules().stream()
                .collect(Collectors.groupingBy(s -> s.getClassEntity().getId()));
        Map<Long, List<Schedule>> p2ByClass = parent2.getSchedules().stream()
                .collect(Collectors.groupingBy(s -> s.getClassEntity().getId()));
        for (Long classId : p1ByClass.keySet()) {
            List<Schedule> schedules = random.nextBoolean() ? p1ByClass.get(classId) : p2ByClass.getOrDefault(classId, Collections.emptyList());
            child.getSchedules().addAll(schedules.stream()
                    .map(s -> new Schedule(null, s.getClassEntity(), s.getSubject(), s.getTeacher(), s.getClassroom(), s.getTimeSlot()))
                    .collect(Collectors.toList()));
        }
        return child;
    }

    private void mutate(
            GeneticTimetable timetable,
            List<TimeSlot> timeSlots,
            List<Teacher> teachers,
            List<Classroom> classrooms,
            Map<Long, List<TeacherAvailability>> teacherAvailabilities,
            Random random) {
        logger.debug("Mutating timetable");
        if (!timetable.getSchedules().isEmpty()) {
            int idx = random.nextInt(timetable.getSchedules().size());
            Schedule toMutate = timetable.getSchedules().get(idx);
            Schedule newSchedule = createRandomSchedule(
                    toMutate.getClassEntity(),
                    toMutate.getSubject(),
                    timeSlots,
                    teachers,
                    classrooms,
                    teacherAvailabilities,
                    random,
                    timetable);
            if (newSchedule != null) {
                timetable.getSchedules().set(idx, newSchedule);
                logger.debug("Mutated schedule at index {}", idx);
            }
        }
    }

    private GeneticTimetable repairTimetable(
            GeneticTimetable timetable,
            List<Class> classes,
            List<TimeSlot> timeSlots,
            List<Teacher> teachers,
            List<Classroom> classrooms,
            Map<Long, List<TeacherAvailability>> teacherAvailabilities) {
        logger.debug("Repairing timetable");
        GeneticTimetable repaired = new GeneticTimetable();
        Map<Long, Set<Long>> teacherSlots = new HashMap<>();
        Map<Long, Set<Long>> classroomSlots = new HashMap<>();
        Map<Long, Set<Long>> classSlots = new HashMap<>();

        for (Schedule s : timetable.getSchedules()) {
            Long timeSlotId = s.getTimeSlot().getId();
            teacherSlots.computeIfAbsent(timeSlotId, k -> new HashSet<>());
            classroomSlots.computeIfAbsent(timeSlotId, k -> new HashSet<>());
            classSlots.computeIfAbsent(timeSlotId, k -> new HashSet<>());
            if (teacherSlots.get(timeSlotId).add(s.getTeacher().getId()) &&
                    classroomSlots.get(timeSlotId).add(s.getClassroom().getId()) &&
                    classSlots.get(timeSlotId).add(s.getClassEntity().getId()) &&
                    s.getClassroom().getType().equalsIgnoreCase(s.getSubject().getRoomType()) &&
                    isTeacherAvailable(s.getTeacher(), teacherAvailabilities.getOrDefault(s.getTeacher().getId(), Collections.emptyList()), timetable)) {
                repaired.getSchedules().add(new Schedule(null, s.getClassEntity(), s.getSubject(), s.getTeacher(), s.getClassroom(), s.getTimeSlot()));
            }
        }

        Map<Long, Map<Long, Integer>> classSubjectHours = repaired.getSchedules().stream()
                .collect(Collectors.groupingBy(
                        s -> s.getClassEntity().getId(),
                        Collectors.groupingBy(
                                s -> s.getSubject().getId(),
                                Collectors.summingInt(s -> 1))));
        for (Class clazz : classes) {
            List<ProgramSubject> requirements = programSubjectRepository.findByProgramId(clazz.getProgram().getId());
            for (ProgramSubject ps : requirements) {
                int actual = classSubjectHours.getOrDefault(clazz.getId(), Collections.emptyMap())
                        .getOrDefault(ps.getSubject().getId(), 0);
                int needed = ps.getHoursPerWeek() - actual;
                for (int i = 0; i < needed; i++) {
                    Schedule newSchedule = createRandomSchedule(
                            clazz, ps.getSubject(), timeSlots, teachers, classrooms, teacherAvailabilities, new Random(), repaired);
                    if (newSchedule != null) {
                        repaired.getSchedules().add(newSchedule);
                        classSubjectHours.computeIfAbsent(clazz.getId(), k -> new HashMap<>())
                                .merge(ps.getSubject().getId(), 1, Integer::sum);
                    }
                }
            }
        }

        logger.debug("Repaired timetable has {} schedules", repaired.getSchedules().size());
        return repaired;
    }

    private Timetable saveTimetable(GeneticTimetable geneticTimetable, Long schoolId) {
        logger.debug("Saving timetable with {} schedules for schoolId: {}", geneticTimetable.getSchedules().size(), schoolId);
        try {
            // Verify no existing timetable
            Optional<Timetable> existing = timetableRepository.findBySchoolId(schoolId);
            if (existing.isPresent()) {
                logger.error("Timetable still exists for schoolId: {} before save", schoolId);
                throw new IllegalStateException("Cannot save timetable; existing timetable found for schoolId: " + schoolId);
            }

            Timetable newTimetable = new Timetable();
            newTimetable.setSchool(schoolRepository.findById(schoolId)
                    .orElseThrow(() -> new IllegalArgumentException("School not found: " + schoolId)));
            newTimetable.setStatus("Draft");
            newTimetable.setGeneratedAt(LocalDateTime.now());
            newTimetable.setSchedules(geneticTimetable.getSchedules());
            geneticTimetable.getSchedules().forEach(s -> s.setTimetable(newTimetable));
            Timetable saved = timetableRepository.save(newTimetable);
            logger.info("Successfully saved timetable with ID: {}", saved.getId());
            return saved;
        } catch (Exception e) {
            logger.error("Failed to save timetable for schoolId: {}", schoolId, e);
            throw new RuntimeException("Failed to save timetable: " + e.getMessage(), e);
        }
    }

    public TimetableDTO mapToTimetableDTO(Timetable timetable) {
        logger.debug("Mapping timetable ID: {} to DTO", timetable.getId());
        TimetableDTO dto = new TimetableDTO();
        dto.setId(timetable.getId());
        dto.setSchoolId(timetable.getSchool().getId());
        dto.setStatus(timetable.getStatus());
        dto.setGeneratedAt(timetable.getGeneratedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        dto.setSchedules(timetable.getSchedules().stream()
                .map(this::mapToScheduleDTO)
                .sorted((s1, s2) -> {
                    String spec1 = getSpecialtyName(s1.getClassEntity().getProgramId());
                    String spec2 = getSpecialtyName(s2.getClassEntity().getProgramId());
                    List<String> order = Arrays.asList("Technology", "Math", "Letters");
                    return Integer.compare(order.indexOf(spec1), order.indexOf(spec2));
                })
                .collect(Collectors.toList()));
        return dto;
    }

    private String getSpecialtyName(Long programId) {
        Program program = programRepository.findById(programId).orElseThrow();
        if (program.getSpecialty() == null) return "General";
        String spec = program.getSpecialty().getName();
        if (spec.contains("Tech")) return "Technology";
        if (spec.contains("Math")) return "Math";
        if (spec.contains("Letter") || spec.contains("Econ")) return "Letters";
        return "General";
    }

    private ScheduleDTO mapToScheduleDTO(Schedule schedule) {
        ScheduleDTO dto = new ScheduleDTO();
        dto.setId(schedule.getId());
        dto.setClassEntity(mapToClassDTO(schedule.getClassEntity()));
        dto.setSubject(mapToSubjectDTO(schedule.getSubject()));
        dto.setTeacher(mapToTeacherDTO(schedule.getTeacher()));
        dto.setClassroom(mapToClassroomDTO(schedule.getClassroom()));
        dto.setTimeSlot(mapToTimeSlotDTO(schedule.getTimeSlot()));
        return dto;
    }

    private ClassDTO mapToClassDTO(Class clazz) {
        ClassDTO dto = new ClassDTO();
        dto.setId(clazz.getId());
        dto.setProgramId(clazz.getProgram().getId());
        dto.setName(clazz.getName());
        dto.setStudentCount(clazz.getStudentCount());
        return dto;
    }

    private SubjectDTO mapToSubjectDTO(Subject subject) {
        SubjectDTO dto = new SubjectDTO();
        dto.setId(subject.getId());
        dto.setName(subject.getName());
        dto.setDefaultHoursPerWeek(subject.getDefaultHoursPerWeek());
        dto.setRoomType(subject.getRoomType());
        return dto;
    }

    private TeacherDTO mapToTeacherDTO(Teacher teacher) {
        TeacherDTO dto = new TeacherDTO();
        dto.setId(teacher.getId());
        dto.setName(teacher.getName());
        dto.setMaxHoursPerWeek(teacher.getMaxHoursPerWeek());
        dto.setSubject(mapToSubjectDTO(teacher.getSubject()));
        dto.setPrograms(teacher.getPrograms().stream()
                .map(p -> {
                    ProgramDTO programDTO = new ProgramDTO();
                    programDTO.setId(p.getId());
                    programDTO.setName(p.getName());
                    programDTO.setLevelId(p.getLevel() != null ? p.getLevel().getId() : null);
                    programDTO.setSpecialtyId(p.getSpecialty() != null ? p.getSpecialty().getId() : null);
                    return programDTO;
                })
                .collect(Collectors.toList()));
        return dto;
    }

    private ClassroomDTO mapToClassroomDTO(Classroom classroom) {
        ClassroomDTO dto = new ClassroomDTO();
        dto.setId(classroom.getId());
        dto.setName(classroom.getName());
        dto.setCapacity(classroom.getCapacity());
        dto.setType(classroom.getType());
        return dto;
    }

    private TimeSlotDTO mapToTimeSlotDTO(TimeSlot timeSlot) {
        TimeSlotDTO dto = new TimeSlotDTO();
        dto.setId(timeSlot.getId());
        dto.setDay(timeSlot.getDay());
        dto.setStartTime(timeSlot.getStartTime().toString());
        dto.setEndTime(timeSlot.getEndTime().toString());
        return dto;
    }

    public TimetableRepository getTimetableRepository() {
        return timetableRepository;
    }

    private static class GeneticTimetable {
        private List<Schedule> schedules;

        public GeneticTimetable() {
            this.schedules = new ArrayList<>();
        }

        public GeneticTimetable(List<Schedule> schedules) {
            this.schedules = new ArrayList<>(schedules);
        }

        public List<Schedule> getSchedules() {
            return schedules;
        }
    }
}