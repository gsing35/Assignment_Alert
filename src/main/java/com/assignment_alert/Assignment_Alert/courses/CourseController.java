package com.assignment_alert.Assignment_Alert.courses;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.assignment_alert.Assignment_Alert.security.AuthenticatedUser;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RequestMapping("/api/v1/courses")
@RestController
public class CourseController {

    private final CourseService courseService;

    @GetMapping("/{courseId}")
    public ResponseEntity<CourseResponseDTO> getCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(courseService.getCourseByIdAndUser(courseId, user.userId()));
    }

    @PutMapping("/{courseId}")
    public ResponseEntity<CourseResponseDTO> updateAssignments(
            @PathVariable Long courseId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(courseService.updateAssignments(courseId, user.userId()));
    }
}
