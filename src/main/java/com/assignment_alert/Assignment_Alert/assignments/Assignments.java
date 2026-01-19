package com.assignment_alert.Assignment_Alert.assignments;

import java.time.LocalDateTime;

import com.assignment_alert.Assignment_Alert.courses.Course;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name="assignments", uniqueConstraints={
    @UniqueConstraint(name="unique_assignment_url", columnNames="url")
})
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Assignments {

    @Id
    @SequenceGenerator(name="assignment_id_seq", sequenceName="assignment_id_seq", allocationSize=1)
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="assignment_id_seq")
    @Column(nullable=false)
    private Long assignmentId;

    @NotBlank(message = "Assignment name is required")
    @Column(nullable=false, columnDefinition="TEXT")
    private String assignmentName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    @NotNull
    private Course course;

    @NotBlank
    @Column(nullable=false, columnDefinition="TEXT")
    private String assignmentType;

    @NotNull
    @Future(message = "Due date must be in the future")
    @Column(nullable=false)
    private LocalDateTime dueDate;

    @Column(nullable=false, updatable=false)
    private LocalDateTime createdDate;

    @Column(nullable=false)
    private Boolean completed;

    @Min(0)
    @Column(nullable=false)
    private Double overallGradeWeight;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private Priority priority;

    @Column(nullable=false, columnDefinition="TEXT")
    private String url;

    @Column(nullable=false)
    private Boolean reminderSent;

    @Column()
    private LocalDateTime blockedUntil;

    @Column(nullable=false)
    private Boolean blockingEnabled;

    @Column
    @Min(0)
    private Double grade;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        if (completed == null) completed = false;
        if (reminderSent == null) reminderSent = false;
        if (blockingEnabled == null) blockingEnabled = false;
    }


    
}
