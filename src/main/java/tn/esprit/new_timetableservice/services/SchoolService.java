package tn.esprit.new_timetableservice.services;

import org.springframework.stereotype.Service;
import tn.esprit.new_timetableservice.entities.School;
import tn.esprit.new_timetableservice.repositories.SchoolRepository;

@Service
public class SchoolService extends BaseCrudService<School, Long> {
    public SchoolService(SchoolRepository repository) {
        super(repository);
    }
}

