package com.assignment_alert.Assignment_Alert.courses;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.assignment_alert.Assignment_Alert.user.User;

public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByCanvasIdAndUser(Long courseId, User user);
   

    
}
