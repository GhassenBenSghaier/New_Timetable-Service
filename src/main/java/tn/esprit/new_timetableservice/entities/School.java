package tn.esprit.new_timetableservice.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "school")
@Getter
@Setter
public class School {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "academic_year")
    private String academicYear;

    @Column(name = "region")
    private String region;

    @Column(name = "type")
    private String type;

    @OneToMany(mappedBy = "school", cascade = CascadeType.ALL)
    private List<Teacher> teachers;

    @OneToMany(mappedBy = "school", cascade = CascadeType.ALL)
    private List<Classroom> classrooms;

    @OneToMany(mappedBy = "school", cascade = CascadeType.ALL)
    private List<TimeSlot> timeSlots;
}