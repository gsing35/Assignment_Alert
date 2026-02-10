package com.assignment_alert.Assignment_Alert.canvas.dtos;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CanvasAssignmentDTO(
        Long id,
        @JsonProperty("name")
        String assignmentName,
        @JsonProperty("course_id")
        Long CourseId,
        @JsonProperty("due_at")
        LocalDateTime dueAt,
        @JsonProperty("html_url")
        String url,
        @JsonProperty("points_possible")
        Double pointsPossible,
        @JsonProperty("has_submitted_submissions")
        Boolean completed,
        List<String> submission,
        @JsonProperty("submission_types")
        List<String> submissionTypes,
        @JsonProperty("submitted_at")
        LocalDateTime submittedAt
        ) {

}
