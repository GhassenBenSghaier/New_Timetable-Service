package tn.esprit.new_timetableservice.services;

import org.springframework.stereotype.Service;
import tn.esprit.new_timetableservice.entities.Level;
import tn.esprit.new_timetableservice.repositories.LevelRepository;

@Service
public class LevelService extends BaseCrudService<Level, Long> {
    public LevelService(LevelRepository repository) {
        super(repository);
    }
}