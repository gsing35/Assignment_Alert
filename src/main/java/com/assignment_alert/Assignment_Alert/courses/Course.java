package com.assignment_alert.Assignment_Alert.courses;

import java.util.ArrayList;
import java.util.List;

import com.assignment_alert.Assignment_Alert.assignments.Assignment;
import com.assignment_alert.Assignment_Alert.user.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "courses", uniqueConstraints = {
    @UniqueConstraint(name = "unique_course_url", columnNames = "url"),
    @UniqueConstraint(name = "unique_course_id", columnNames = "courseId")

})
@Entity
public class Course {

    @Id
    @SequenceGenerator(name = "course_id_seq", sequenceName = "course_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "course_id_seq")
    @Column(nullable = false)
    private Long courseId;

    @Column(columnDefinition = "TEXT")
    private String courseName;

    @Column(nullable = false)
    private Long canvasCourseId;

    // Each user will get its own copy of coures with own copy of assignments
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String url;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Assignment> assignments = new ArrayList<>();

}
