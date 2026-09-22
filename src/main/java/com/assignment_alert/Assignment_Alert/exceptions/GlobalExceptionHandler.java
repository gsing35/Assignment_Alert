package com.assignment_alert.Assignment_Alert.exceptions;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AssignmentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAssignmentNotFound(AssignmentNotFoundException exception) {
        return build(exception.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CourseNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCourseNotFound(CourseNotFoundException exception) {
        return build(exception.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException exception) {
        return build(exception.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateAssignmentException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateAssignmentFound(DuplicateAssignmentException exception) {
        return build(exception.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DuplicateCourseException.class)
    public ResponseEntity<ErrorResponse> hanldeDuplicateCourseFound(DuplicateCourseException exception) {
        return build(exception.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(RequestValidationException.class)
    public ResponseEntity<ErrorResponse> handleRequestValidation(RequestValidationException exception) {
        return build(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidToken(InvalidTokenException exception) {
        return build(exception.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(CanvasApiException.class)
    public ResponseEntity<ErrorResponse> handleCanvasApi(CanvasApiException exception) {
        return build(exception.getMessage(), HttpStatus.BAD_GATEWAY);
    }

    private ResponseEntity<ErrorResponse> build(String message, HttpStatus status) {
        ErrorResponse errorResponse = new ErrorResponse(
            message,
            status.value(),
            LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, status);
    }
}
