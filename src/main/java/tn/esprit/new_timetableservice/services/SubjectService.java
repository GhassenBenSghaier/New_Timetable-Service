package tn.esprit.new_timetableservice.services;

import org.springframework.stereotype.Service;
import tn.esprit.new_timetableservice.entities.Subject;
import tn.esprit.new_timetableservice.repositories.SubjectRepository;

@Service
public class SubjectService extends BaseCrudService<Subject, Long> {
    public SubjectService(SubjectRepository repository) {
        super(repository);
    }
}