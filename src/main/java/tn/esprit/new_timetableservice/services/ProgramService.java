package tn.esprit.new_timetableservice.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.new_timetableservice.dto.*;
import tn.esprit.new_timetableservice.entities.*;
import tn.esprit.new_timetableservice.repositories.*;
import org.springframework.dao.DataIntegrityViolationException;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProgramService {

    @Autowired private ProgramRepository programRepository;
    @Autowired private ProgramSubjectRepository programSubjectRepository;
    @Autowired private LevelRepository levelRepository;
    @Autowired private SpecialtyRepository specialtyRepository;
    @Autowired private SubjectRepository subjectRepository;

    public ProgramDTO createProgram(ProgramDTO request) {
        Level level = levelRepository.findById(request.getLevelId())
                .orElseThrow(() -> new IllegalArgumentException("Level not found"));

        // Validate specialtyId based on supportsSpecialty
        if (!level.isSupportsSpecialty() && request.getSpecialtyId() != null) {
            throw new IllegalArgumentException("Level does not support specialties");
        }
        if (level.isSupportsSpecialty() && request.getSpecialtyId() != null) {
            specialtyRepository.findById(request.getSpecialtyId())
                    .orElseThrow(() -> new IllegalArgumentException("Specialty not found"));
        }

        String programName = generateProgramName(request.getLevelId(), request.getSpecialtyId());
        request.setName(programName);

        if (programRepository.findByName(programName).isPresent()) {
            throw new DataIntegrityViolationException("Program with name " + programName + " already exists");
        }

        Program program = new Program();
        program.setLevel(level);
        if (request.getSpecialtyId() != null) {
            Specialty specialty = specialtyRepository.findById(request.getSpecialtyId())
                    .orElseThrow(() -> new IllegalArgumentException("Specialty not found"));
            program.setSpecialty(specialty);
        }
        program.setName(programName);
        program.setId(null);

        Program saved = programRepository.save(program);

        for (ProgramSubjectDTO psDto : request.getProgramSubjects()) {
            ProgramSubject ps = new ProgramSubject();
            ps.setProgram(saved);
            Subject subject = subjectRepository.findById(psDto.getSubjectId())
                    .orElseThrow(() -> new IllegalArgumentException("Subject not found"));
            ps.setSubject(subject);
            ps.setHoursPerWeek(psDto.getHoursPerWeek());
            ps.setIsCore(psDto.getIsCore());
            programSubjectRepository.save(ps);
        }

        return mapToProgramDTO(saved);
    }

    public ProgramDTO updateProgram(ProgramDTO request) {
        if (request.getId() == null) {
            throw new IllegalArgumentException("Program ID cannot be null for update");
        }

        Program program = programRepository.findById(request.getId())
                .orElseThrow(() -> new EntityNotFoundException("Program not found with ID: " + request.getId()));

        Level level = levelRepository.findById(request.getLevelId())
                .orElseThrow(() -> new IllegalArgumentException("Level not found"));

        // Validate specialtyId based on supportsSpecialty
        if (!level.isSupportsSpecialty() && request.getSpecialtyId() != null) {
            throw new IllegalArgumentException("Level does not support specialties");
        }
        if (level.isSupportsSpecialty() && request.getSpecialtyId() != null) {
            specialtyRepository.findById(request.getSpecialtyId())
                    .orElseThrow(() -> new IllegalArgumentException("Specialty not found"));
        }

        String programName = generateProgramName(request.getLevelId(), request.getSpecialtyId());
        request.setName(programName);
        program.setName(programName);

        program.setLevel(level);
        if (request.getSpecialtyId() != null) {
            Specialty specialty = specialtyRepository.findById(request.getSpecialtyId())
                    .orElseThrow(() -> new IllegalArgumentException("Specialty not found"));
            program.setSpecialty(specialty);
        } else {
            program.setSpecialty(null);
        }

        List<ProgramSubject> existingSubjects = programSubjectRepository.findByProgramId(program.getId());
        programSubjectRepository.deleteAll(existingSubjects);

        Program updatedProgram = programRepository.save(program);

        for (ProgramSubjectDTO psDto : request.getProgramSubjects()) {
            ProgramSubject ps = new ProgramSubject();
            ps.setProgram(updatedProgram);
            Subject subject = subjectRepository.findById(psDto.getSubjectId())
                    .orElseThrow(() -> new IllegalArgumentException("Subject not found"));
            ps.setSubject(subject);
            ps.setHoursPerWeek(psDto.getHoursPerWeek());
            ps.setIsCore(psDto.getIsCore());
            programSubjectRepository.save(ps);
        }

        return mapToProgramDTO(updatedProgram);
    }

    public List<ProgramDTO> getPrograms() {
        return programRepository.findAll().stream()
                .map(this::mapToProgramDTO)
                .collect(Collectors.toList());
    }

    public ProgramDTO getProgramById(Long id) {
        Program program = programRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Program not found with ID: " + id));
        return mapToProgramDTO(program);
    }

    public void deleteProgram(Long id) {
        Program program = programRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Program not found with ID: " + id));
        programSubjectRepository.deleteAll(programSubjectRepository.findByProgramId(id));
        programRepository.delete(program);
    }

    public ProgramDTO getProgramByName(String name) {
        Program program = programRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Program not found"));
        return mapToProgramDTO(program);
    }

    public List<LevelDTO> getLevels() {
        return levelRepository.findAll().stream()
                .map(this::mapToLevelDTO)
                .collect(Collectors.toList());
    }

    public List<SpecialtyDTO> getSpecialtiesByLevel(Long levelId) {
        return specialtyRepository.findByLevelId(levelId).stream()
                .map(this::mapToSpecialtyDTO)
                .collect(Collectors.toList());
    }

    public List<SubjectDTO> getSubjects() {
        return subjectRepository.findAll().stream()
                .map(this::mapToSubjectDTO)
                .collect(Collectors.toList());
    }

    private String generateProgramName(Long levelId, Long specialtyId) {
        Level level = levelRepository.findById(levelId)
                .orElseThrow(() -> new IllegalArgumentException("Level not found"));
        String levelName = level.getName();
        if (specialtyId == null || !level.isSupportsSpecialty()) {
            return levelName;
        }
        String specialtyName = specialtyRepository.findById(specialtyId)
                .map(Specialty::getName)
                .orElseThrow(() -> new IllegalArgumentException("Specialty not found"));
        return levelName + " " + specialtyName;
    }

    private ProgramDTO mapToProgramDTO(Program program) {
        ProgramDTO dto = new ProgramDTO();
        dto.setId(program.getId());
        dto.setLevelId(program.getLevel() != null ? program.getLevel().getId() : null);
        dto.setSpecialtyId(program.getSpecialty() != null ? program.getSpecialty().getId() : null);
        dto.setName(program.getName());
        dto.setProgramSubjects(programSubjectRepository.findByProgramId(program.getId()).stream()
                .map(this::mapToProgramSubjectDTO)
                .collect(Collectors.toList()));
        return dto;
    }

    private ProgramSubjectDTO mapToProgramSubjectDTO(ProgramSubject ps) {
        ProgramSubjectDTO dto = new ProgramSubjectDTO();
        dto.setId(ps.getId());
        dto.setSubjectId(ps.getSubject().getId());
        dto.setHoursPerWeek(ps.getHoursPerWeek());
        dto.setIsCore(ps.getIsCore());
        return dto;
    }

    private LevelDTO mapToLevelDTO(Level level) {
        LevelDTO dto = new LevelDTO();
        dto.setId(level.getId());
        dto.setName(level.getName());
        dto.setSupportsSpecialty(level.isSupportsSpecialty());
        return dto;
    }

    private SpecialtyDTO mapToSpecialtyDTO(Specialty specialty) {
        SpecialtyDTO dto = new SpecialtyDTO();
        dto.setId(specialty.getId());
        dto.setName(specialty.getName());
        return dto;
    }

    private SubjectDTO mapToSubjectDTO(Subject subject) {
        SubjectDTO dto = new SubjectDTO();
        dto.setId(subject.getId());
        dto.setName(subject.getName());
        dto.setDefaultHoursPerWeek(subject.getDefaultHoursPerWeek());
        return dto;
    }
}