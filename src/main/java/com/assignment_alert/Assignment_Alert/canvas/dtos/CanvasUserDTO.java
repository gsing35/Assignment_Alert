package com.assignment_alert.Assignment_Alert.canvas.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CanvasUserDTO(
        @JsonProperty("id")
        Long canvasId,
        String name,
        @JsonProperty("primary_email")
        String email
        ) {

}
