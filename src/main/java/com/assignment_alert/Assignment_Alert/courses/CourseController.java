package com.assignment_alert.Assignment_Alert.courses;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.assignment_alert.Assignment_Alert.user.User;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RequestMapping("/api/v1/courses")
@RestController
public class CourseController {

    private final CourseService courseService;

    @GetMapping("/{courseId}")
    public ResponseEntity<CourseResponseDTO> getCourseByIdAndUser(@PathVariable Long courseId, @RequestBody User user) {
        return ResponseEntity.ok(courseService.getCourseByIdAndUser(courseId, user));
    }

    @PutMapping("/{courseId}")
    public ResponseEntity<CourseResponseDTO> updateAssignments(@PathVariable Long courseId, @RequestBody User user ) {
        return ResponseEntity.ok(courseService.updateAssignments(courseId, user));
    }

    
}   
