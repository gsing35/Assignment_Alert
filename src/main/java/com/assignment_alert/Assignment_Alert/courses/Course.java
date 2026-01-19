package com.assignment_alert.Assignment_Alert.courses;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.assignment_alert.Assignment_Alert.assignments.Assignments;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name="courses", uniqueConstraints={
    @UniqueConstraint(name="unique_course_url", columnNames="url")
})
@Entity
public class Course {
    

    @Id
    @SequenceGenerator(name="courses_id_seq", sequenceName="courses_id_seq", allocationSize=1)
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="courses_id_seq")
    @Column(nullable=false)
    private Long id;

    @NotBlank(message = "Course name is required")
    @Column(nullable=false, columnDefinition="TEXT")
    private String courseName;

    @Column
    @Min(0)
    private Double grade;

    @NotBlank
    @Column(nullable=false)
    private URI url;

    @OneToMany(mappedBy="course", cascade=CascadeType.ALL, orphanRemoval=true)
    private List<Assignments> assignments = new ArrayList<>();
}
