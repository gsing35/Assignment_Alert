package com.assignment_alert.Assignment_Alert.assignments;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.assignment_alert.Assignment_Alert.user.User;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    public Optional<Assignment> findByCanvasAssignmentId(Long canvasAssignmentId);

    public List<Assignment> findByCanvasCourseIdOrderByDueAtAsc(Long courseId);

    public List<Assignment> findByDueAtAfterAndCourse_UserOrderByDueAtAsc(LocalDateTime now, User user);

    public List<Assignment> findByCompletedFalseAndCourse_UserOrderByDueAtAsc(User user);

    public List<Assignment> findByBlockingEnabledTrueAndDueAtAfterAndCourse_User(LocalDateTime now, User user);

    public List<Assignment> findByPriorityAndCourse_UserOrderByDueAtAsc(Priority priority, User user);

    // Might need to change this
    @Query("SELECT a FROM Assignment a WHERE a.reminderSent = false AND a.dueAt < :deadline AND a.completed = false")
    List<Assignment> findAssignmentsNeedingReminders(LocalDateTime deadline);

}
