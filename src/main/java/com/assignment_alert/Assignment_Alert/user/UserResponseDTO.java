package com.assignment_alert.Assignment_Alert.user;

import java.util.List;

import com.assignment_alert.Assignment_Alert.courses.Course;

public record UserResponseDTO(
    String name, 
    List<Course> courses,
    String email
) {
   
    public static UserResponseDTO from(User user) {
        return new UserResponseDTO(user.getName(), user.getCourses(), user.getEmail());
    }
}
