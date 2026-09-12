package com.assignment_alert.Assignment_Alert.assignments;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.assignment_alert.Assignment_Alert.courses.Course;
import com.assignment_alert.Assignment_Alert.user.User;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    // A Canvas assignment id repeats across users (each user owns a copy of the course), so it must
    // always be qualified by course or user - an unqualified lookup can match several rows.
    public Optional<Assignment> findByCanvasAssignmentIdAndCourse(Long canvasAssignmentId, Course course);

    public Optional<Assignment> findByCanvasAssignmentIdAndCourse_User(Long canvasAssignmentId, User user);

    public Optional<Assignment> findByAssignmentIdAndCourse_User(Long assignmentId, User user);

    public List<Assignment> findByCanvasCourseIdAndCourse_UserOrderByDueAtAsc(Long courseId, User user);

    public List<Assignment> findByDueAtAfterAndCourse_UserOrderByDueAtAsc(LocalDateTime now, User user);

    public List<Assignment> findByCompletedFalseAndCourse_UserOrderByDueAtAsc(User user);

    public List<Assignment> findByBlockingEnabledTrueAndDueAtAfterAndCourse_User(LocalDateTime now, User user);

    public List<Assignment> findByPriorityAndCourse_UserOrderByDueAtAsc(Priority priority, User user);

    // Might need to change this
    @Query("SELECT a FROM Assignment a WHERE a.reminderSent = false AND a.dueAt < :deadline AND a.completed = false")
    List<Assignment> findAssignmentsNeedingReminders(LocalDateTime deadline);

}
