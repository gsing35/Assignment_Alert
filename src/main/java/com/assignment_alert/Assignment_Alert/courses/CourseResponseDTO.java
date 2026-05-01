package com.assignment_alert.Assignment_Alert.courses;

import java.util.List;
import java.util.stream.Collectors;

import com.assignment_alert.Assignment_Alert.assignments.Assignment;
import com.assignment_alert.Assignment_Alert.assignments.AssignmentResponseDTO;

public record CourseResponseDTO(
        String courseName,
        Long canvasCourseId,
        Long courseId,
        String url,
        List<AssignmentResponseDTO> assignments
        ) {

    public static CourseResponseDTO from(Course course) {
        return new CourseResponseDTO(
                course.getCourseName(),
                course.getCanvasCourseId(),
                course.getCourseId(),
                course.getUrl(),
                course.getAssignments()
                    .stream()
                    .map(AssignmentResponseDTO::from)
                    .collect(Collectors.toList())
        );
    }   

}
