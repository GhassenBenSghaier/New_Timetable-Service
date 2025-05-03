package tn.esprit.new_timetableservice.services;

import org.springframework.stereotype.Service;
import tn.esprit.new_timetableservice.entities.Class;
import tn.esprit.new_timetableservice.repositories.ClassRepository;

import java.util.List;

@Service
public class ClassService {
    private final ClassRepository repository;

    public ClassService(ClassRepository repository) {
        this.repository = repository;
    }

    public Class create(Class classEntity) {
        return repository.save(classEntity);
    }

    public Class update(Long id, Class classEntity) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Class with id " + id + " not found");
        }
        classEntity.setId(id);
        return repository.save(classEntity);
    }

    public Class findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Class with id " + id + " not found"));
    }

    public List<Class> findAll() {
        return repository.findAll();
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Class with id " + id + " not found");
        }
        repository.deleteById(id);
    }

    public List<Class> findBySchoolId(Long schoolId) {
        return repository.findBySchoolId(schoolId);
    }
}