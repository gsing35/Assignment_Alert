package com.assignment_alert.Assignment_Alert.assignments;

import com.assignment_alert.Assignment_Alert.courses.CourseRepository;
import com.assignment_alert.Assignment_Alert.courses.Courses;

import jakarta.transaction.Transactional;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository assignmentRepo;
    private final CourseRepository courseRepo;

    public Assignments saveAssignment(AssignmentRegistrationRequest request) {
        Courses course = courseRepo.findById(request.courseId()).orElseThrow(() -> new IllegalStateException());
        Assignments assignment = request.toEntity(course);
        return assignmentRepo.save(assignment);
    }

    public Assignments getAssignmentById(Long id) {
        // TODO Exceptions
        return assignmentRepo.findById(id).orElseThrow(() -> new IllegalStateException());
    }

    public Page<Assignments> getAllAssignments(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return assignmentRepo.findAll(pageable);
    }

    @Transactional
    public void updateAssignment(Long id, AssignmentUpdateRequest request) {
        Assignments assignment = assignmentRepo.findById(id).orElseThrow(() -> new IllegalStateException());

        boolean changes = applyChanges(assignment, request);

        if(!changes) {
            throw new IllegalStateException("No changes were made");
        }

        assignmentRepo.save(assignment);
    }

    public void deleteAssignment(Long id) {
        Assignments assignment = assignmentRepo.findById(id).orElseThrow(() -> new IllegalStateException());
        assignmentRepo.delete(assignment);
    }

    public boolean applyChanges(Assignments assignment, AssignmentUpdateRequest request) {

        boolean changes = false;

        if(!assignment.getDueDate().equals(request.dueDate())) {
            assignment.setDueDate(request.dueDate());
            changes = true;
        }

        if(!assignment.getCompleted().equals(request.completed())) {
            assignment.setCompleted(request.completed());
            changes = true;
        }

        if(!assignment.getPriority().equals(request.priority())) {
            assignment.setPriority(request.priority());
            changes = true;
        }

        if(!assignment.getReminderSent().equals(request.reminderSent())) {
            assignment.setReminderSent(request.reminderSent());
            changes = true;
        }

        if(assignment.getBlockedUntil().equals(request.reminderSent())) {
            assignment.setBlockedUntil(request.blockedUntil());
            changes = true;
        }

        if(assignment.getBlockingEnabled().equals(request.blockingEnabled())) {
            assignment.setBlockingEnabled(request.blockingEnabled());
            changes = true;
        }

        return changes;
    }

}
