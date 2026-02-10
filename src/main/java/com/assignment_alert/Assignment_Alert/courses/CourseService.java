package com.assignment_alert.Assignment_Alert.courses;

import org.springframework.stereotype.Service;

import com.assignment_alert.Assignment_Alert.canvas.CanvasSyncService;
import com.assignment_alert.Assignment_Alert.exceptions.CourseNotFoundException;
import com.assignment_alert.Assignment_Alert.user.User;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepo;
    private final CanvasSyncService canvasSyncService;

    public CourseResponseDTO getCourseByIdAndUser(Long canvasCourseId, User user) {
        Course course = courseRepo.findByCanvasCourseIdAndUser(canvasCourseId, user).orElseThrow(() -> new CourseNotFoundException("Course Not Found"));
        return CourseResponseDTO.from(course);
    }

    @Transactional
    public CourseResponseDTO updateAssignments(Long canvasCourseId, User user) {
        Course course = courseRepo.findByCanvasCourseIdAndUser(canvasCourseId, user).orElseThrow(() -> new CourseNotFoundException("Course Not Found"));
        canvasSyncService.syncForCourse(course, user);

        return CourseResponseDTO.from(course);
    }


    
}
