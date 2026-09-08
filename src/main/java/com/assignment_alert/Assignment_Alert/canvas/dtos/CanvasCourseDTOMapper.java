package com.assignment_alert.Assignment_Alert.canvas.dtos;

import org.springframework.stereotype.Component;

import com.assignment_alert.Assignment_Alert.assignments.AssignmentRepository;
import com.assignment_alert.Assignment_Alert.courses.Course;
import com.assignment_alert.Assignment_Alert.user.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CanvasCourseDTOMapper {

    private final AssignmentRepository assignmentRepo;

    // Check which method is better
    public Course toEntity(CanvasCourseDTO courseDto, Course oldCourse, String schoolDomain, AssignmentRepository assignmentRepo, User user) {
        Course course = oldCourse != null ? oldCourse : new Course();

        course.setCanvasCourseId(courseDto.canvasCourseId());
        if (courseDto.name() != null) {
            course.setCourseName(courseDto.name());
        }
        course.setUrl("https://%s/courses/%d".formatted(schoolDomain, course.getCanvasCourseId()));

        if (course.getCanvasCourseId() != null) {
            course.setAssignments(assignmentRepo.findByCanvasCourseIdAndCourse_UserOrderByDueAtAsc(course.getCanvasCourseId(), user));
        }
        course.setUser(user);

        return course;

    }

    public Course toEntity(CanvasCourseDTO courseDto, String schoolDomain, AssignmentRepository assignmentRepo, User user) {
        return toEntity(courseDto, null, schoolDomain, assignmentRepo, user);

    }

}
