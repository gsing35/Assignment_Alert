package com.assignment_alert.Assignment_Alert.assignments;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.assignment_alert.Assignment_Alert.canvas.CanvasSyncService;
import com.assignment_alert.Assignment_Alert.exceptions.AssignmentNotFoundException;
import com.assignment_alert.Assignment_Alert.exceptions.UserNotFoundException;
import com.assignment_alert.Assignment_Alert.user.User;
import com.assignment_alert.Assignment_Alert.user.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AssignmentService {
    private final AssignmentRepository assignmentRepo;
    private final CanvasSyncService canvasSyncService;
    private final UserRepository userRepo;

    private User requireUser(Long userId) {
        return userRepo.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id " + userId + " Not Found"));
    }

    // Looks up an assignment only within the given user's own courses, so another user's
    // assignment is indistinguishable from one that doesn't exist.
    private Assignment requireOwnedAssignment(Long assignmentId, Long userId) {
        return assignmentRepo.findByAssignmentIdAndCourse_User(assignmentId, requireUser(userId))
                .orElseThrow(() -> new AssignmentNotFoundException("Assignment Not Found"));
    }

    private void applyCompletion(Assignment assignment, boolean completed) {
        assignment.setCompleted(completed);
        assignment.setCompletedAt(completed ? LocalDateTime.now() : null);
    }

    public List<AssignmentResponseDTO> getUpcomingAssignments(Long userId) {
        return assignmentRepo.findByDueAtAfterAndCourse_UserOrderByDueAtAsc(LocalDateTime.now(), requireUser(userId))
        .stream()
        .map(AssignmentResponseDTO::from)
        .collect(Collectors.toList());
    }

    public List<AssignmentResponseDTO> getAssignmentsByCourse(Long courseId, Long userId) {
        return assignmentRepo.findByCanvasCourseIdAndCourse_UserOrderByDueAtAsc(courseId, requireUser(userId))
        .stream()
        .map(AssignmentResponseDTO::from)
        .collect(Collectors.toList());
    }

    public AssignmentResponseDTO getAssignmentByCanvasAssignmentId(Long canvasAssignmentId, Long userId) {
        Assignment assignment = assignmentRepo
                .findByCanvasAssignmentIdAndCourse_User(canvasAssignmentId, requireUser(userId))
                .orElseThrow(() -> new AssignmentNotFoundException("Assignment Not Found"));
        return AssignmentResponseDTO.from(assignment);
    }

    public AssignmentResponseDTO getAssignment(Long assignmentId, Long userId) {
        return AssignmentResponseDTO.from(requireOwnedAssignment(assignmentId, userId));
    }

    public List<AssignmentResponseDTO> getIncompleteAssignments(Long userId) {
        return assignmentRepo.findByCompletedFalseAndCourse_UserOrderByDueAtAsc(requireUser(userId))
        .stream()
        .map(AssignmentResponseDTO::from)
        .collect(Collectors.toList());
    }

    public List<AssignmentResponseDTO> getActiveBlockingAssignments(Long userId) {
        return assignmentRepo.findByBlockingEnabledTrueAndDueAtAfterAndCourse_User(LocalDateTime.now(), requireUser(userId))
        .stream()
        .map(AssignmentResponseDTO::from)
        .collect(Collectors.toList());
    }

    public List<AssignmentResponseDTO> getAssignmentsByPriority(Priority priority, Long userId) {
        return assignmentRepo.findByPriorityAndCourse_UserOrderByDueAtAsc(priority, requireUser(userId))
        .stream()
        .map(AssignmentResponseDTO::from)
        .collect(Collectors.toList());
    }

    @Transactional
    public AssignmentResponseDTO updateAssignment(Long id, AssignmentUpdateRequest updateRequest, Long userId) {
        Assignment assignment = requireOwnedAssignment(id, userId);

        if(updateRequest.completed() != null) {
            applyCompletion(assignment, updateRequest.completed());
        }

        if(updateRequest.priority() != null) {
            assignment.setPriority(updateRequest.priority());
        }

        if(updateRequest.blockedUntil() != null) {
            assignment.setBlockedUntil(updateRequest.blockedUntil());
        }

        if(updateRequest.blockingEnabled() != null) {
            assignment.setBlockingEnabled(updateRequest.blockingEnabled());
        }

        Assignment updated = assignmentRepo.save(assignment);
        return AssignmentResponseDTO.from(updated);
    }

    @Transactional
    public AssignmentResponseDTO markAsCompleted(Long assignmentId, Boolean completed, Long userId) {
        Assignment assignment = requireOwnedAssignment(assignmentId, userId);
        applyCompletion(assignment, completed);

        Assignment completedAssignment = assignmentRepo.save(assignment);

        return AssignmentResponseDTO.from(completedAssignment);
    }

}
