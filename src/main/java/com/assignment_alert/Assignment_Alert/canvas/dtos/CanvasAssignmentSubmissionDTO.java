package com.assignment_alert.Assignment_Alert.canvas.dtos;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CanvasAssignmentSubmissionDTO(
        Double score,
        Double grade,
        @JsonProperty("submitted_at")
        LocalDateTime submittedAt
        ) {

}
