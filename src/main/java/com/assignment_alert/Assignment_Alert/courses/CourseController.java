package com.assignment_alert.Assignment_Alert.courses;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
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
@CrossOrigin
public class CourseController {

    private final CourseService courseService;

    @GetMapping("/{courseId}/users/{userId}")
    public ResponseEntity<CourseResponseDTO> getCourseByIdAndUser(@PathVariable Long courseId, @PathVariable Long userId) {
        return ResponseEntity.ok(courseService.getCourseByIdAndUser(courseId, userId));
    }

    @PutMapping("/{courseId}/users/{userId}")
    public ResponseEntity<CourseResponseDTO> updateAssignments(@PathVariable Long courseId, @PathVariable Long userId ) {
        return ResponseEntity.ok(courseService.updateAssignments(courseId, userId));
    }

    
}   
//225702