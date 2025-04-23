package tn.esprit.new_timetableservice.services;

import org.springframework.stereotype.Service;
import tn.esprit.new_timetableservice.entities.Specialty;
import tn.esprit.new_timetableservice.repositories.SpecialtyRepository;

@Service
public class SpecialtyService extends BaseCrudService<Specialty, Long> {
    public SpecialtyService(SpecialtyRepository repository) {
        super(repository);
    }
}