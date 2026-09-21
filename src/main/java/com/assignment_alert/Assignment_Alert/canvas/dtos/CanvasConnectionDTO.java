package com.assignment_alert.Assignment_Alert.canvas.dtos;

public record CanvasConnectionDTO(
    Long userId,
    String name,
    String email,
    String schoolDomain,
    String message,
    String sessionToken
) {

}
