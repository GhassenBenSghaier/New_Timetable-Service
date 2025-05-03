package tn.esprit.new_timetableservice.services;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.new_timetableservice.dto.ProgramDTO;
import tn.esprit.new_timetableservice.dto.TeacherCreationDTO;
import tn.esprit.new_timetableservice.dto.TeacherResponseDTO;
import tn.esprit.new_timetableservice.entities.Program;
import tn.esprit.new_timetableservice.entities.School;
import tn.esprit.new_timetableservice.entities.Subject;
import tn.esprit.new_timetableservice.entities.Teacher;
import tn.esprit.new_timetableservice.repositories.ProgramRepository;
import tn.esprit.new_timetableservice.repositories.SchoolRepository;
import tn.esprit.new_timetableservice.repositories.SubjectRepository;
import tn.esprit.new_timetableservice.repositories.TeacherRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeacherService {

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private SchoolRepository schoolRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private ProgramRepository programRepository;

    public TeacherResponseDTO createTeacher(TeacherCreationDTO teacherDTO) {
        if (teacherDTO.getSchoolId() == null) {
            throw new IllegalArgumentException("School ID is required");
        }
        if (teacherDTO.getSubjectId() == null) {
            throw new IllegalArgumentException("Subject ID is required");
        }

        School school = schoolRepository.findById(teacherDTO.getSchoolId())
                .orElseThrow(() -> new IllegalArgumentException("School not found with id: " + teacherDTO.getSchoolId()));
        Subject subject = subjectRepository.findById(teacherDTO.getSubjectId())
                .orElseThrow(() -> new IllegalArgumentException("Subject not found with id: " + teacherDTO.getSubjectId()));

        Teacher teacher = new Teacher();
        teacher.setId(teacherDTO.getId());
        teacher.setName(teacherDTO.getName());
        teacher.setMaxHoursPerWeek(teacherDTO.getMaxHoursPerWeek());
        teacher.setSchool(school);
        teacher.setSubject(subject);

        Teacher savedTeacher = teacherRepository.save(teacher);
        return toResponseDTO(savedTeacher);
    }

    public TeacherResponseDTO updateTeacher(Long id, TeacherCreationDTO teacherDTO) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found with id: " + id));

        if (teacherDTO.getSchoolId() == null) {
            throw new IllegalArgumentException("School ID is required");
        }
        if (teacherDTO.getSubjectId() == null) {
            throw new IllegalArgumentException("Subject ID is required");
        }

        School school = schoolRepository.findById(teacherDTO.getSchoolId())
                .orElseThrow(() -> new IllegalArgumentException("School not found with id: " + teacherDTO.getSchoolId()));
        Subject subject = subjectRepository.findById(teacherDTO.getSubjectId())
                .orElseThrow(() -> new IllegalArgumentException("Subject not found with id: " + teacherDTO.getSubjectId()));

        teacher.setName(teacherDTO.getName());
        teacher.setMaxHoursPerWeek(teacherDTO.getMaxHoursPerWeek());
        teacher.setSchool(school);
        teacher.setSubject(subject);

        Teacher updatedTeacher = teacherRepository.save(teacher);
        return toResponseDTO(updatedTeacher);
    }

    public void deleteTeacher(Long id) {
        if (!teacherRepository.existsById(id)) {
            throw new IllegalArgumentException("Teacher not found with id: " + id);
        }
        teacherRepository.deleteById(id);
    }

    public List<ProgramDTO> getTeacherPrograms(Long teacherId) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found with id: " + teacherId));
        return teacher.getPrograms().stream()
                .map(this::toProgramDTO)
                .collect(Collectors.toList());
    }

    public void addProgramToTeacher(Long teacherId, Long programId) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found with id: " + teacherId));
        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new EntityNotFoundException("Program not found with id: " + programId));

        if (teacher.getPrograms().contains(program)) {
            throw new IllegalArgumentException("Program already assigned to teacher");
        }

        teacher.getPrograms().add(program);
        teacherRepository.save(teacher);
    }

    public void removeProgramFromTeacher(Long teacherId, Long programId) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found with id: " + teacherId));
        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new EntityNotFoundException("Program not found with id: " + programId));

        if (!teacher.getPrograms().contains(program)) {
            throw new IllegalArgumentException("Program not assigned to teacher");
        }

        teacher.getPrograms().remove(program);
        teacherRepository.save(teacher);
    }

    private TeacherResponseDTO toResponseDTO(Teacher teacher) {
        TeacherResponseDTO dto = new TeacherResponseDTO();
        dto.setId(teacher.getId());
        dto.setName(teacher.getName());
        dto.setMaxHoursPerWeek(teacher.getMaxHoursPerWeek());
        dto.setSubjectId(teacher.getSubject().getId());
        dto.setSubjectName(teacher.getSubject().getName());
        dto.setSchoolId(teacher.getSchool().getId());
        dto.setSchoolName(teacher.getSchool().getName());
        dto.setPrograms(teacher.getPrograms().stream()
                .map(this::toProgramDTO)
                .collect(Collectors.toList()));
        return dto;
    }

    private ProgramDTO toProgramDTO(Program program) {
        ProgramDTO dto = new ProgramDTO();
        dto.setId(program.getId());
        dto.setName(program.getName());
        dto.setLevelId(program.getLevel() != null ? program.getLevel().getId() : null);
        dto.setSpecialtyId(program.getSpecialty() != null ? program.getSpecialty().getId() : null);
        return dto;
    }
}