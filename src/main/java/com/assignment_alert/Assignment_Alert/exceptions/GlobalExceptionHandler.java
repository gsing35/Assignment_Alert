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
        ErrorResponse errorResponse = new ErrorResponse(
            exception.getMessage(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(CourseNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCourseNotFound(CourseNotFoundException exception) {
        ErrorResponse errorResponse = new ErrorResponse(
            exception.getMessage(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DuplicateAssignmentException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateAssignmentFound(DuplicateAssignmentException exception) {
        ErrorResponse errorResponse = new ErrorResponse(
            exception.getMessage(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DuplicateCourseException.class)
    public ResponseEntity<ErrorResponse> hanldeDuplicateCourseFound(DuplicateCourseException exception) {
        ErrorResponse errorResponse = new ErrorResponse(
            exception.getMessage(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RequestValidationException.class)
    public ResponseEntity<ErrorResponse> handleRequestValidation(RequestValidationException exception) {
        ErrorResponse errorResponse = new ErrorResponse(
            exception.getMessage(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidToken(InvalidTokenException exception) {
        ErrorResponse errorResponse = new ErrorResponse(
            exception.getMessage(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(CanvasApiException.class)
    public ResponseEntity<ErrorResponse> handleCanvasApi(CanvasApiException exception) {
        ErrorResponse errorResponse = new ErrorResponse(
            exception.getMessage(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException exception) {
        ErrorResponse errorResponse = new ErrorResponse(
            exception.getMessage(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
