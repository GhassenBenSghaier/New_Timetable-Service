package tn.esprit.new_timetableservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import tn.esprit.new_timetableservice.entities.School;

import java.util.List;


public interface SchoolRepository extends JpaRepository<School, Long> {

    List<School> findAll();

}