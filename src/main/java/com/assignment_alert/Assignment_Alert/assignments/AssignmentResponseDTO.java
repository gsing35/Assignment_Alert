package com.assignment_alert.Assignment_Alert.assignments;

import java.time.Duration;
import java.time.LocalDateTime;

public record AssignmentResponseDTO(
        Long assignmentId,
        String assignmentName,
        Long canvasAssignmentId,
        String courseName,
        Long courseId,
        LocalDateTime dueDate,
        LocalDateTime createdDate,
        Boolean completed,
        Double pointsWorth,
        Double grade,
        Priority priority,
        String url,
        Boolean blockingEnabled,
        Long hoursUntilDue,
        Long userId
        ) {

    public static AssignmentResponseDTO from(Assignment assignment) {
        long hoursUntilDue = 0;
        if (assignment.getDueAt() != null) {
            hoursUntilDue = Duration.between(LocalDateTime.now(), assignment.getDueAt()).toHours();
        }

        return new AssignmentResponseDTO(
                assignment.getAssignmentId(),
                assignment.getAssignmentName(),
                assignment.getCanvasAssignmentId(),
                assignment.getCourse().getCourseName(),
                assignment.getCourse().getCanvasCourseId(),
                assignment.getDueAt(),
                assignment.getCreatedDate(),
                assignment.getCompleted(),
                assignment.getPointsWorth(),
                assignment.getGrade(),
                assignment.getPriority(),
                assignment.getUrl(),
                assignment.getBlockingEnabled(),
                hoursUntilDue,
                assignment.getCourse().getUser().getUserId()
        );
    }

}