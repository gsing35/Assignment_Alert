package com.assignment_alert.Assignment_Alert.user;

import java.util.List;
import java.util.stream.Collectors;

import com.assignment_alert.Assignment_Alert.courses.Course;
import com.assignment_alert.Assignment_Alert.courses.CourseResponseDTO;

public record UserResponseDTO(
    Long userId,
    String name, 
    List<CourseResponseDTO> courses,
    String email
) {
   
    public static UserResponseDTO from(User user) {
        return new UserResponseDTO(user.getUserId(), user.getName(), user.getCourses().stream().map(CourseResponseDTO::from).collect(Collectors.toList()), user.getEmail());
    }
}
