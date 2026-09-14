package com.assignment_alert.Assignment_Alert.canvas.dtos;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.assignment_alert.Assignment_Alert.assignments.Assignment;
import com.assignment_alert.Assignment_Alert.assignments.Priority;
import com.assignment_alert.Assignment_Alert.courses.Course;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CanvasAssignmentDTOMapper {

    public Assignment toEntity(CanvasAssignmentDTO assignmentDto, Course course, Assignment oldAssignment) {
        Assignment assignment = oldAssignment != null ? oldAssignment : new Assignment();

        assignment.setCanvasAssignmentId(assignmentDto.id());
        assignment.setAssignmentName(assignmentDto.assignmentName());
        assignment.setCourse(course);
        assignment.setCanvasCourseId(course.getCanvasCourseId());
        assignment.setUrl(assignmentDto.url());

        if (assignmentDto.dueAt() != null) {
            assignment.setDueAt(assignmentDto.dueAt());
        }

        // Might chnage this to the field in the json created_at
        if (assignment.getCreatedDate() == null) {
            assignment.setCreatedDate(LocalDateTime.now());
        }

        if (assignmentDto.completed() != null) {
            assignment.setCompleted(assignmentDto.completed());
        }

        if (assignmentDto.submission() != null && assignmentDto.submission().submittedAt() != null) {
            assignment.setCompletedAt(assignmentDto.submission().submittedAt());
        }

        if (assignmentDto.submissionTypes() != null && !assignmentDto.submissionTypes().isEmpty()) {
            assignment.setSubmissionType(assignmentDto.submissionTypes().get(0));
        }

        if (assignmentDto.pointsPossible() != null) {
            assignment.setPointsWorth(assignmentDto.pointsPossible());
        }

        if (assignmentDto.submission() != null && assignment.getPointsWorth() != null && assignment.getPointsWorth() > 0 && assignmentDto.submission().score() != null) {
            double score = assignmentDto.submission().score();
            assignment.setGrade(score / assignment.getPointsWorth() * 100);
        }

        if (oldAssignment == null) {
            assignment.setPriority(calculatePriority(assignmentDto.dueAt()));
            assignment.setBlockingEnabled(false);
            assignment.setCompleted(false);
            assignment.setReminderSent(false);
        }

        //TODO working on fixing fields for entities and making proper DTOS with the response json
        // Gonna need to learn how to parse an array field for this bc grade is there
        // Get grade by score from usbmission array / possible points
        assignment.setLastSynced(LocalDateTime.now());

        return assignment;
    }

    private Priority calculatePriority(LocalDateTime dueDate) {
        if (dueDate == null) {
            return Priority.LOW;
        }

        long hourUntilDue = Duration.between(LocalDateTime.now(), dueDate).toHours();

        if (hourUntilDue <= 24) {
            return Priority.HIGH;
        }
        if (hourUntilDue <= 72) {
            return Priority.MEDIUM;
        } else {
            return Priority.LOW;
        }
    }
    // TODO finsih implementing dto mapper and check if current assinment entity class implementio is valid for all the assignment service classes

    public Assignment toEntity(CanvasAssignmentDTO assignmentDto, Course course) {
        return toEntity(assignmentDto, course, null);
    }

}
