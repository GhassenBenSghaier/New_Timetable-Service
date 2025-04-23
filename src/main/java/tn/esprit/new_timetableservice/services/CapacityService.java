package tn.esprit.new_timetableservice.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.new_timetableservice.dto.CapacityResponse;
import tn.esprit.new_timetableservice.dto.ConstraintCheck;
import tn.esprit.new_timetableservice.entities.*;
import tn.esprit.new_timetableservice.entities.Class;
import tn.esprit.new_timetableservice.repositories.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CapacityService {

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private ProgramSubjectRepository programSubjectRepository;

    @Autowired
    private ProgramRepository programRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    public CapacityResponse calculateCapacity(Long schoolId, Map<Long, Integer> desiredClasses) {
        // Validate inputs
        if (schoolId == null || desiredClasses == null || desiredClasses.isEmpty()) {
            throw new IllegalArgumentException("School ID and desired classes must be provided.");
        }
        desiredClasses.forEach((progId, count) -> {
            if (count < 0) throw new IllegalArgumentException("Class count cannot be negative for program " + progId);
        });

        // Fetch school data
        List<Class> existingClasses = classRepository.findAll().stream()
                .filter(c -> {
                    Program prog = c.getProgram();
                    Level level = prog.getLevel();
                    return level != null && level.getSchool() != null && schoolId.equals(level.getSchool().getId());
                })
                .collect(Collectors.toList());
        List<Classroom> classrooms = classroomRepository.findBySchoolId(schoolId);
        List<ProgramSubject> programSubjects = programSubjectRepository.findAll();
        List<Teacher> teachers = teacherRepository.findBySchoolId(schoolId);
        List<TimeSlot> timeSlots = timeSlotRepository.findBySchoolId(schoolId);

        // Validate data
        if (classrooms.isEmpty()) {
            return new CapacityResponse(schoolId, false, new HashMap<>(), new HashMap<>(), new HashMap<>(),
                    new ConstraintCheck(0, 0, false), new ConstraintCheck(0, 0, false), "No classrooms available.");
        }
        if (timeSlots.isEmpty()) {
            return new CapacityResponse(schoolId, false, new HashMap<>(), new HashMap<>(), new HashMap<>(),
                    new ConstraintCheck(0, 0, false), new ConstraintCheck(0, 0, false), "No timeslots available.");
        }

        // Calculate required hours per subject
        Map<Long, Integer> requiredHours = new HashMap<>();
        for (Map.Entry<Long, Integer> entry : desiredClasses.entrySet()) {
            Long programId = entry.getKey();
            Integer numClasses = entry.getValue();
            if (!programRepository.existsById(programId)) {
                throw new IllegalArgumentException("Program ID " + programId + " does not exist.");
            }
            List<ProgramSubject> subjects = programSubjectRepository.findByProgramId(programId);
            for (ProgramSubject ps : subjects) {
                requiredHours.merge(ps.getSubject().getId(), numClasses * ps.getHoursPerWeek(), Integer::sum);
            }
        }

        // Calculate available teacher hours per subject, considering teacher_program
        Map<Long, Integer> availableTeacherHours = new HashMap<>();
        for (Teacher teacher : teachers) {
            Long subjectId = teacher.getSubject().getId();
            List<Long> programIds = teacher.getPrograms().stream().map(Program::getId).collect(Collectors.toList());
            int maxHours = teacher.getMaxHoursPerWeek() != null ? teacher.getMaxHoursPerWeek() : 0;
            // Only count hours for programs the teacher can teach
            int relevantHours = desiredClasses.entrySet().stream()
                    .filter(e -> programIds.contains(e.getKey()))
                    .mapToInt(e -> requiredHours.getOrDefault(subjectId, 0))
                    .sum() > 0 ? maxHours : 0;
            availableTeacherHours.merge(subjectId, relevantHours, Integer::sum);
        }

        // Check teacher constraints
        Map<Long, ConstraintCheck> teacherConstraints = new HashMap<>();
        boolean teacherFeasible = true;
        for (Map.Entry<Long, Integer> entry : requiredHours.entrySet()) {
            Long subjectId = entry.getKey();
            Integer reqHours = entry.getValue();
            Integer availHours = availableTeacherHours.getOrDefault(subjectId, 0);
            boolean isSatisfied = availHours >= reqHours;
            teacherConstraints.put(subjectId, new ConstraintCheck(reqHours, availHours, isSatisfied));
            if (!isSatisfied) {
                teacherFeasible = false;
            }
        }

        // Map subjects to required classroom types
        Map<Long, String> subjectToRoomType = new HashMap<>();
        subjectToRoomType.put(1L, "standard"); // Arabic
        subjectToRoomType.put(2L, "standard"); // French
        subjectToRoomType.put(3L, "standard"); // English
        subjectToRoomType.put(4L, "standard"); // History
        subjectToRoomType.put(5L, "standard"); // Geography
        subjectToRoomType.put(6L, "standard"); // Islamic Education
        subjectToRoomType.put(7L, "standard"); // Civic Education
        subjectToRoomType.put(8L, "standard"); // Mathematics
        subjectToRoomType.put(9L, "science_lab"); // Physics
        subjectToRoomType.put(10L, "science_lab"); // Biology
        subjectToRoomType.put(11L, "computer_lab"); // Computer Science
        subjectToRoomType.put(12L, "technology_lab"); // Technological Education
        subjectToRoomType.put(13L, "gym"); // Physical Education
        subjectToRoomType.put(14L, "standard"); // Philosophy
        subjectToRoomType.put(15L, "standard"); // Economics
        subjectToRoomType.put(16L, "standard"); // Business Management
        subjectToRoomType.put(17L, "electrical_lab"); // Electrical Engineering
        subjectToRoomType.put(18L, "mechanical_lab"); // Mechanical Engineering
        subjectToRoomType.put(19L, "standard"); // Third Foreign Language
        subjectToRoomType.put(20L, "standard"); // Musical Education
        subjectToRoomType.put(21L, "standard"); // Artistic Education
        subjectToRoomType.put(22L, "computer_lab"); // Algorithms
        subjectToRoomType.put(23L, "computer_lab"); // ICT
        subjectToRoomType.put(24L, "computer_lab"); // Systems and Networks
        subjectToRoomType.put(25L, "computer_lab"); // Databases

        // Calculate required hours per room type
        Map<String, Integer> roomTypeHours = new HashMap<>();
        for (Map.Entry<Long, Integer> entry : requiredHours.entrySet()) {
            Long subjectId = entry.getKey();
            Integer hours = entry.getValue();
            String roomType = subjectToRoomType.getOrDefault(subjectId, "standard");
            roomTypeHours.merge(roomType, hours, Integer::sum);
        }

        // Calculate available room hours
        Map<String, Integer> availableRoomHours = classrooms.stream()
                .collect(Collectors.groupingBy(
                        Classroom::getType,
                        Collectors.summingInt(c -> timeSlots.size())
                ));

        // Check classroom constraints
        Map<String, ConstraintCheck> roomConstraints = new HashMap<>();
        boolean roomFeasible = true;
        for (Map.Entry<String, Integer> entry : roomTypeHours.entrySet()) {
            String roomType = entry.getKey();
            Integer reqHours = entry.getValue();
            Integer availHours = availableRoomHours.getOrDefault(roomType, 0);
            boolean isSatisfied = availHours >= reqHours;
            roomConstraints.put(roomType, new ConstraintCheck(reqHours, availHours, isSatisfied));
            if (!isSatisfied) {
                roomFeasible = false;
            }
        }

        // Total sessions vs. total available slots
        int totalRequiredSessions = requiredHours.values().stream().mapToInt(Integer::intValue).sum();
        int totalAvailableSlots = classrooms.size() * timeSlots.size();
        boolean slotFeasible = totalRequiredSessions <= totalAvailableSlots;
        ConstraintCheck slotConstraint = new ConstraintCheck(totalRequiredSessions, totalAvailableSlots, slotFeasible);

        // Check if desired classes exceed classroom count
        int totalDesiredClasses = desiredClasses.values().stream().mapToInt(Integer::intValue).sum();
        int totalClassrooms = classrooms.size();
        boolean classCountFeasible = totalDesiredClasses <= totalClassrooms;
        ConstraintCheck classCountConstraint = new ConstraintCheck(totalDesiredClasses, totalClassrooms, classCountFeasible);

        // Determine feasibility
        boolean overallFeasible = teacherFeasible && roomFeasible && slotFeasible && classCountFeasible;

        // Suggest maximum classes if infeasible
        Map<Long, Integer> maxClasses = new HashMap<>(desiredClasses);
        if (!overallFeasible) {
            maxClasses = adjustClassCounts(schoolId, desiredClasses);
        }

        // Build response
        return new CapacityResponse(
                schoolId,
                overallFeasible,
                maxClasses,
                teacherConstraints,
                roomConstraints,
                slotConstraint,
                classCountConstraint,
                overallFeasible ? "Desired configuration is feasible."
                        : "Adjusted class counts to meet constraints."
        );
    }

    private Map<Long, Integer> adjustClassCounts(Long schoolId, Map<Long, Integer> desiredClasses) {
        Map<Long, Integer> adjustedClasses = new HashMap<>(desiredClasses);
        List<Long> programIds = new ArrayList<>(desiredClasses.keySet());

        // Sort programs by level (Level 1 first, then others) to prioritize core programs
        programIds.sort((p1, p2) -> {
            Program prog1 = programRepository.findById(p1).orElseThrow();
            Program prog2 = programRepository.findById(p2).orElseThrow();
            Integer level1 = prog1.getLevel() != null ? prog1.getLevel().getId().intValue() : 0;
            Integer level2 = prog2.getLevel() != null ? prog2.getLevel().getId().intValue() : 0;
            return Integer.compare(level1, level2);
        });

        boolean constraintsMet = false;
        while (!constraintsMet && !adjustedClasses.isEmpty()) {
            constraintsMet = true;

            // Recalculate hours
            Map<Long, Integer> requiredHours = new HashMap<>();
            Map<String, Integer> roomTypeHours = new HashMap<>();
            for (Map.Entry<Long, Integer> entry : adjustedClasses.entrySet()) {
                Long programId = entry.getKey();
                Integer numClasses = entry.getValue();
                List<ProgramSubject> subjects = programSubjectRepository.findByProgramId(programId);
                for (ProgramSubject ps : subjects) {
                    requiredHours.merge(ps.getSubject().getId(), numClasses * ps.getHoursPerWeek(), Integer::sum);
                    String roomType = getRoomTypeForSubject(ps.getSubject().getId());
                    roomTypeHours.merge(roomType, numClasses * ps.getHoursPerWeek(), Integer::sum);
                }
            }

            // Check teacher constraints
            Map<Long, Integer> availableTeacherHours = teacherRepository.findBySchoolId(schoolId).stream()
                    .collect(Collectors.groupingBy(
                            teacher -> teacher.getSubject().getId(),
                            Collectors.summingInt(t -> t.getMaxHoursPerWeek() != null ? t.getMaxHoursPerWeek() : 0)
                    ));
            for (Map.Entry<Long, Integer> entry : requiredHours.entrySet()) {
                Long subjectId = entry.getKey();
                Integer req = entry.getValue();
                Integer avail = availableTeacherHours.getOrDefault(subjectId, 0);
                if (req > avail) {
                    reduceClasses(adjustedClasses, subjectId);
                    constraintsMet = false;
                    break;
                }
            }

            // Check room constraints
            Map<String, Integer> availableRoomHours = classroomRepository.findBySchoolId(schoolId).stream()
                    .collect(Collectors.groupingBy(
                            Classroom::getType,
                            Collectors.summingInt(c -> timeSlotRepository.findBySchoolId(schoolId).size())
                    ));
            for (Map.Entry<String, Integer> entry : roomTypeHours.entrySet()) {
                String roomType = entry.getKey();
                Integer req = entry.getValue();
                Integer avail = availableRoomHours.getOrDefault(roomType, 0);
                if (req > avail) {
                    reduceClassesForRoomType(adjustedClasses, roomType);
                    constraintsMet = false;
                    break;
                }
            }

            // Check total sessions
            int totalSessions = requiredHours.values().stream().mapToInt(Integer::intValue).sum();
            int totalSlots = classroomRepository.findBySchoolId(schoolId).size() *
                    timeSlotRepository.findBySchoolId(schoolId).size();
            if (totalSessions > totalSlots) {
                reduceClasses(adjustedClasses, findHighestHourProgram(adjustedClasses));
                constraintsMet = false;
            }

            // Check class count vs. classrooms
            int totalClasses = adjustedClasses.values().stream().mapToInt(Integer::intValue).sum();
            int totalClassrooms = classroomRepository.findBySchoolId(schoolId).size();
            if (totalClasses > totalClassrooms) {
                reduceClasses(adjustedClasses, programIds.get(programIds.size() - 1));
                constraintsMet = false;
            }
        }

        return adjustedClasses;
    }

    private void reduceClasses(Map<Long, Integer> classCounts, Long subjectId) {
        List<ProgramSubject> subjects = programSubjectRepository.findAll().stream()
                .filter(ps -> ps.getSubject().getId().equals(subjectId))
                .collect(Collectors.toList());
        subjects.sort((s1, s2) -> {
            Program p1 = s1.getProgram();
            Program p2 = s2.getProgram();
            Integer level1 = p1.getLevel() != null ? p1.getLevel().getId().intValue() : 0;
            Integer level2 = p2.getLevel() != null ? p2.getLevel().getId().intValue() : 0;
            return Integer.compare(level1, level2);
        });
        for (ProgramSubject ps : subjects) {
            Long progId = ps.getProgram().getId();
            if (classCounts.containsKey(progId) && classCounts.get(progId) > 0) {
                classCounts.put(progId, classCounts.get(progId) - 1);
                if (classCounts.get(progId) == 0) {
                    classCounts.remove(progId);
                }
                break;
            }
        }
    }

    private void reduceClassesForRoomType(Map<Long, Integer> classCounts, String roomType) {
        List<ProgramSubject> subjects = programSubjectRepository.findAll().stream()
                .filter(ps -> getRoomTypeForSubject(ps.getSubject().getId()).equals(roomType))
                .collect(Collectors.toList());
        subjects.sort((s1, s2) -> {
            Program p1 = s1.getProgram();
            Program p2 = s2.getProgram();
            Integer level1 = p1.getLevel() != null ? p1.getLevel().getId().intValue() : 0;
            Integer level2 = p2.getLevel() != null ? p2.getLevel().getId().intValue() : 0;
            return Integer.compare(level1, level2);
        });
        for (ProgramSubject ps : subjects) {
            Long progId = ps.getProgram().getId();
            if (classCounts.containsKey(progId) && classCounts.get(progId) > 0) {
                classCounts.put(progId, classCounts.get(progId) - 1);
                if (classCounts.get(progId) == 0) {
                    classCounts.remove(progId);
                }
                break;
            }
        }
    }

    private Long findHighestHourProgram(Map<Long, Integer> classCounts) {
        Long maxProgId = null;
        int maxHours = -1;
        for (Map.Entry<Long, Integer> entry : classCounts.entrySet()) {
            Long progId = entry.getKey();
            int numClasses = entry.getValue();
            int totalHours = programSubjectRepository.findByProgramId(progId).stream()
                    .mapToInt(ProgramSubject::getHoursPerWeek)
                    .sum() * numClasses;
            if (totalHours > maxHours) {
                maxHours = totalHours;
                maxProgId = progId;
            }
        }
        return maxProgId;
    }

    private String getRoomTypeForSubject(Long subjectId) {
        Map<Long, String> subjectToRoomType = new HashMap<>();
        subjectToRoomType.put(1L, "standard"); // Arabic
        subjectToRoomType.put(2L, "standard"); // French
        subjectToRoomType.put(3L, "standard"); // English
        subjectToRoomType.put(4L, "standard"); // History
        subjectToRoomType.put(5L, "standard"); // Geography
        subjectToRoomType.put(6L, "standard"); // Islamic Education
        subjectToRoomType.put(7L, "standard"); // Civic Education
        subjectToRoomType.put(8L, "standard"); // Mathematics
        subjectToRoomType.put(9L, "science_lab"); // Physics
        subjectToRoomType.put(10L, "science_lab"); // Biology
        subjectToRoomType.put(11L, "computer_lab"); // Computer Science
        subjectToRoomType.put(12L, "technology_lab"); // Technological Education
        subjectToRoomType.put(13L, "gym"); // Physical Education
        subjectToRoomType.put(14L, "standard"); // Philosophy
        subjectToRoomType.put(15L, "standard"); // Economics
        subjectToRoomType.put(16L, "standard"); // Business Management
        subjectToRoomType.put(17L, "electrical_lab"); // Electrical Engineering
        subjectToRoomType.put(18L, "mechanical_lab"); // Mechanical Engineering
        subjectToRoomType.put(19L, "standard"); // Third Foreign Language
        subjectToRoomType.put(20L, "standard"); // Musical Education
        subjectToRoomType.put(21L, "standard"); // Artistic Education
        subjectToRoomType.put(22L, "computer_lab"); // Algorithms
        subjectToRoomType.put(23L, "computer_lab"); // ICT
        subjectToRoomType.put(24L, "computer_lab"); // Systems and Networks
        subjectToRoomType.put(25L, "computer_lab"); // Databases
        return subjectToRoomType.getOrDefault(subjectId, "standard");
    }
}