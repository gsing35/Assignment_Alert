package com.assignment_alert.Assignment_Alert.courses;

import java.util.List;

import com.assignment_alert.Assignment_Alert.assignments.Assignment;

public record CourseResponseDTO(
    String courseName,
    Long canvasCourseId,
    String url,
    List<Assignment> assignments

) {

    public static CourseResponseDTO from(Course course) {
        return new CourseResponseDTO(
            course.getCourseName(),
            course.getCanvasCourseId(),
            course.getUrl(),
            course.getAssignments()
        );
    }

}
