package com.assignment_alert.Assignment_Alert.assignments;

import java.time.LocalDateTime;

public record AssignmentUpdateRequest(
    LocalDateTime dueDate,
    Boolean completed,
    Priority priority,
    Boolean reminderSent,
    LocalDateTime blockedUntil,
    Boolean blockingEnabled
) {
    

}
