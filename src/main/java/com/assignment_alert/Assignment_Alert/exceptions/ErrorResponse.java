package com.assignment_alert.Assignment_Alert.exceptions;

import java.time.LocalDateTime;


public record ErrorResponse (

    String path,
    String message,
    int statusCode,
    LocalDateTime localDateTime
) {
}
