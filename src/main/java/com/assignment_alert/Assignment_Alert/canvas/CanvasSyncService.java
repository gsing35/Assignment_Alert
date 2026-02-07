package com.assignment_alert.Assignment_Alert.canvas;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.assignment_alert.Assignment_Alert.assignments.Assignment;
import com.assignment_alert.Assignment_Alert.assignments.AssignmentRepository;
import com.assignment_alert.Assignment_Alert.aws.AwsSecretsManagerService;
import com.assignment_alert.Assignment_Alert.canvas.dtos.CanvasAssignmentDTO;
import com.assignment_alert.Assignment_Alert.canvas.dtos.CanvasAssignmentDTOMapper;
import com.assignment_alert.Assignment_Alert.canvas.dtos.CanvasCourseDTO;
import com.assignment_alert.Assignment_Alert.canvas.dtos.CanvasCourseDTOMapper;
import com.assignment_alert.Assignment_Alert.courses.Course;
import com.assignment_alert.Assignment_Alert.courses.CourseRepository;
import com.assignment_alert.Assignment_Alert.user.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CanvasSyncService {

    private final CanvasApiClient apiClient;

    private final AwsSecretsManagerService secretManager;

    private final CanvasCourseDTOMapper courseMapper;

    private final CourseRepository courseRepo;

    private final CanvasAssignmentDTOMapper assignmentMapper;

    private final AssignmentRepository assignmentRepo;

    public void initalSync(User user) {
        String canvasToken = secretManager.getToken(user.getUserId());

        List<CanvasCourseDTO> coursesDTOs = apiClient.getCourses(user.getSchoolDomain(), canvasToken);

        for(CanvasCourseDTO c : coursesDTOs) {
            Course course = courseMapper.toEntity(c, user.getSchoolDomain(), assignmentRepo);
            courseRepo.save(course);

            syncAssignments(course, canvasToken, user.getSchoolDomain());
            courseRepo.save(course);
        }

    }

    public void syncForCourse(Course course, User user) {
        String canvasToken = secretManager.getToken(user.getUserId());

        syncAssignments(course, canvasToken, user.getSchoolDomain());
    }

    public void syncAssignments(Course course, String canvasToken, String schoolDomain) {
        
        List<CanvasAssignmentDTO> assignmentsDTOs = apiClient.getAssignments(schoolDomain, canvasToken, course.getCanvasCourseId());

        for(CanvasAssignmentDTO a : assignmentsDTOs) {
            Assignment assignment = assignmentMapper.toEntity(a, course);
            assignmentRepo.save(assignment);
        }




    }
}
