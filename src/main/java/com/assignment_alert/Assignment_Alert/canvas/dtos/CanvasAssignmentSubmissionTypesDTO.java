package com.assignment_alert.Assignment_Alert.canvas.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CanvasAssignmentSubmissionTypesDTO(
        @JsonProperty("0")
        String submissionType
        ) {

}
