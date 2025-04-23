package tn.esprit.new_timetableservice.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "program", indexes = {
        @Index(name = "idx_program_name", columnList = "name", unique = true)
})
@Getter
@Setter
public class Program {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_id", nullable = true) // Null for Level 1
    private Level level;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialty_id", nullable = true) // Null for Level 1
    private Specialty specialty;

    @Column(nullable = false, unique = true)
    private String name; // e.g., "Level 1", "2Science", "3Math"

    @OneToMany(mappedBy = "program", cascade = CascadeType.ALL)
    private List<ProgramSubject> programSubjects;
}