package com.assignment_alert.Assignment_Alert.courses;

import org.springframework.aot.hint.annotation.Reflective;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    
}
