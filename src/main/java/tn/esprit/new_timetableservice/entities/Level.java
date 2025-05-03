package tn.esprit.new_timetableservice.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "level")
@Getter
@Setter
public class Level {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean supportsSpecialty;

    @OneToMany(mappedBy = "level", cascade = CascadeType.ALL)
    private List<Specialty> specialties;
}