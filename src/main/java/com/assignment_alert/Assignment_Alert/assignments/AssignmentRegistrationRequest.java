package com.assignment_alert.Assignment_Alert.assignments;

import java.time.LocalDateTime;

import com.assignment_alert.Assignment_Alert.courses.Courses;

public record  AssignmentRegistrationRequest(
    String assignmentName,
    Long courseId,
    String assignmentType,
    LocalDateTime dueDate,
    Double overallGradeWeight,
    Priority priority,
    String url,
    LocalDateTime blockedUntil,
    Boolean blockingEnabled
) {
    public Assignments toEntity(Course course) {
        Assignments assignment = new Assignments();
        assignment.setAssignmentName(this.assignmentName);
        assignment.setCourse(course);
        assignment.setAssignmentType(this.assignmentType);
        assignment.setDueDate(this.dueDate);
        assignment.setOverallGradeWeight(this.overallGradeWeight);
        assignment.setPriority(this.priority);
        assignment.setUrl(this.url);
        assignment.setBlockingEnabled(this.blockingEnabled != null ? this.blockingEnabled : false);
        assignment.setBlockedUntil(this.blockedUntil);
        return assignment;
    }
}
