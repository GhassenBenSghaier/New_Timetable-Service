

package tn.esprit.new_timetableservice.entities;

import jakarta.persistence.*;
        import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "teacher")
@Getter
@Setter
public class Teacher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToMany
    @JoinTable(
            name = "teacher_program",
            joinColumns = @JoinColumn(name = "teacher_id"),
            inverseJoinColumns = @JoinColumn(name = "program_id")
    )
    private List<Program> programs;

    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL)
    private List<TeacherAvailability> availabilities;

    @Column(name = "max_hours_per_week")
    private Integer maxHoursPerWeek;

}