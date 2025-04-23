package tn.esprit.new_timetableservice.services;

import org.springframework.stereotype.Service;
import tn.esprit.new_timetableservice.entities.Classroom;
import tn.esprit.new_timetableservice.repositories.ClassroomRepository;

import java.util.List;

@Service
public class ClassroomService {
    private final ClassroomRepository repository;

    public ClassroomService(ClassroomRepository repository) {
        this.repository = repository;
    }

    public Classroom create(Classroom classroom) {
        return repository.save(classroom);
    }

    public Classroom update(Long id, Classroom classroom) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Classroom with id " + id + " not found");
        }
        classroom.setId(id);
        return repository.save(classroom);
    }

    public Classroom findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Classroom with id " + id + " not found"));
    }

    public List<Classroom> findAll() {
        return repository.findAll();
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Classroom with id " + id + " not found");
        }
        repository.deleteById(id);
    }
}