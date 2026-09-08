package com.assignment_alert.Assignment_Alert.canvas;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

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
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
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

        for (CanvasCourseDTO c : coursesDTOs) {
            try {
                Course course = courseRepo.findByCanvasCourseIdAndUser(c.canvasCourseId(), user).orElseGet(() ->  {
                    Course newCourse = courseMapper.toEntity(c, user.getSchoolDomain(), assignmentRepo, user);
                    return courseRepo.save(newCourse);
                });

                log.info("Syncing course: {} (ID: {})", course.getCourseName(), course.getCanvasCourseId());
                syncAssignments(course, canvasToken, user.getSchoolDomain());
                log.info("Successfully synced course: {}", course.getCourseName());
                courseRepo.save(course);

            } catch (HttpClientErrorException.Forbidden e) {
                log.warn("Skipping course {}, Access Denied 403", c.name());
            } catch (HttpClientErrorException e) {
                log.error("Failed to sync course {}, HTTP {}: {}", c.name(), e.getStatusCode(), e.getMessage());
            } catch (Exception e) {
                log.error("Unexcepted error syncing course {}: {}", c.name(), e.getMessage(), e);
            }

        }

    }

    public void syncForCourse(Course course, User user) {
        String canvasToken = secretManager.getToken(user.getUserId());

        syncAssignments(course, canvasToken, user.getSchoolDomain());
    }

    public void syncAssignments(Course course, String canvasToken, String schoolDomain) {

        List<CanvasAssignmentDTO> assignmentsDTOs = apiClient.getAssignments(schoolDomain, canvasToken, course.getCanvasCourseId());

        for (CanvasAssignmentDTO a : assignmentsDTOs) {
            Assignment assignment = assignmentRepo.findByCanvasAssignmentId(a.id()).map(existing -> {
                assignmentMapper.toEntity(a, existing.getCourse(), existing);
                return existing;
            })
            .orElseGet(() -> assignmentMapper.toEntity(a, course, null));
            assignmentRepo.save(assignment);
        }

    }
}
