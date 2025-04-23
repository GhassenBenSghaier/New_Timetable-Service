package tn.esprit.new_timetableservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import tn.esprit.new_timetableservice.entities.Class;

import java.util.List;

public interface ClassRepository extends JpaRepository<Class, Long> {
    List<Class> findBySchoolId(Long schoolId);
    @Modifying
    @Query("DELETE FROM Class c WHERE c.school.id = :schoolId")
    int deleteBySchoolId(Long schoolId);
}