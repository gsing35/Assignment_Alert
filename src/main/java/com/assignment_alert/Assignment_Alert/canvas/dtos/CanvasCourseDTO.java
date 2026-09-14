package com.assignment_alert.Assignment_Alert.canvas.dtos;


import com.fasterxml.jackson.annotation.JsonProperty;

// DTOs represent external data not internal entittes and shoudl only include fields from json api response
public record CanvasCourseDTO(
    @JsonProperty("id")
    Long canvasCourseId,

    String name
) {

}
