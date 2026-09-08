package com.assignment_alert.Assignment_Alert.assignments;

import java.time.LocalDateTime;

public record AssignmentUpdateRequest(
    Boolean completed,
    Priority priority,
    LocalDateTime blockedUntil,
    Boolean blockingEnabled
) {
    

}
