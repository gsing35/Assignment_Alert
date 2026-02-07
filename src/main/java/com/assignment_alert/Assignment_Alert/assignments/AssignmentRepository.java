package com.assignment_alert.Assignment_Alert.assignments;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    
    public Optional<Assignment> findByCanvasAssignmentId(Long canvasAssignmentId);  
    public List<Assignment> findByCanvasCourseIdOrderByDueAtAsc(Long courseId);
    public List<Assignment> findByDueAtAfterOrderByDueAtAsc(LocalDateTime now);
    public List<Assignment> findByCompletedFalseOrderByDueAtAsc();
    public List<Assignment> findByBlockingEnabledTrueAndDueAtAfter(LocalDateTime now);
    public List<Assignment> findByPriorityOrderByDueAtAsc(Priority priority);

    // Might need to change this
    @Query("SELECT a FROM Assignment a WHERE a.reminderSent = false AND a.dueAt < :deadline AND a.completed = false")
    List<Assignment> findAssignmentsNeedingReminders(LocalDateTime deadline);


    

}
