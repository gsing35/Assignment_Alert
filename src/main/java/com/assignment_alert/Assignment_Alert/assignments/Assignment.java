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
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(
        name = "assignments",
        // Each user gets their own copy of a course and its assignments, so a Canvas assignment id
        // is only unique within one course row - never globally across users.
        uniqueConstraints = {
            @UniqueConstraint(
                name = "unique_canvas_assignment_per_course",
                columnNames = {"canvas_assignment_id", "course_id"}
            )
        },
        indexes = {
            @Index(name = "canvas_assignment_id", columnList = "canvas_assignment_id"),
            @Index(name = "due_at", columnList = "due_at"),
            @Index(name = "completed", columnList = "completed")
        }
)
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Assignment {

    @Id
    @SequenceGenerator(name = "assignment_id_seq", sequenceName = "assignment_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "assignment_id_seq")
    @Column(nullable = false)
    private Long assignmentId;

    @NotBlank(message = "Assignment name is required")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String assignmentName;

    @Column(name = "canvas_assignment_id", nullable = false)
    private Long canvasAssignmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    private Long canvasCourseId;

    @Column(name = "due_at")
    private LocalDateTime dueAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(nullable = false)
    private Boolean completed;

    @Column
    private Double pointsWorth;

    @Column
    private Double grade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "TEXT")
    private Priority priority;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String url;

    @Column(nullable = false)
    private Boolean reminderSent = false;

    @Column(name = "blocked_until")
    private LocalDateTime blockedUntil;

    @Column(nullable = false)
    private Boolean blockingEnabled;

    @Column(nullable = false)
    private LocalDateTime lastSynced;

    @Column(nullable = false)
    private String submissionType;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

}
