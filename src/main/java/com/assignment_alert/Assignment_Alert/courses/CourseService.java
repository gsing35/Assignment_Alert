package com.assignment_alert.Assignment_Alert.courses;

import org.springframework.stereotype.Service;

import com.assignment_alert.Assignment_Alert.canvas.CanvasSyncService;
import com.assignment_alert.Assignment_Alert.exceptions.CourseNotFoundException;
import com.assignment_alert.Assignment_Alert.exceptions.UserNotFoundException;
import com.assignment_alert.Assignment_Alert.user.User;
import com.assignment_alert.Assignment_Alert.user.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepo;
    private final CanvasSyncService canvasSyncService;
    private final UserRepository userRepo;

    public CourseResponseDTO getCourseByIdAndUser(Long courseId, Long userId) {
        User user = userRepo.findByUserId(userId).orElseThrow(() -> new UserNotFoundException("User with id" + userId + "for course with Id" + courseId + " Not Found"));
        Course course = courseRepo.findByCourseIdAndUser(courseId, user).orElseThrow(() -> new CourseNotFoundException("Course Not Found"));
        return CourseResponseDTO.from(course);
    }

    @Transactional
    public CourseResponseDTO updateAssignments(Long courseId, Long userId) {
        User user = userRepo.findByUserId(userId).orElseThrow(() -> new UserNotFoundException("User with id" + userId + "for course with Id" + courseId + " Not Found"));
        Course course = courseRepo.findByCourseIdAndUser(courseId, user).orElseThrow(() -> new CourseNotFoundException("Course Not Found"));
        canvasSyncService.syncForCourse(course, user);

        return CourseResponseDTO.from(course);
    }


    
}
