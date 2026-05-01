package com.assignment_alert.Assignment_Alert.assignments;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.assignment_alert.Assignment_Alert.courses.CourseRepository;
import com.assignment_alert.Assignment_Alert.canvas.CanvasSyncService;
import com.assignment_alert.Assignment_Alert.courses.Course;
import com.assignment_alert.Assignment_Alert.exceptions.*;

import jakarta.transaction.Transactional;

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
    private final CanvasSyncService canvasSyncService;

    public List<AssignmentResponseDTO> getUpcomingAssignments() {
            return assignmentRepo.findByDueAtAfterOrderByDueAtAsc(LocalDateTime.now())
            .stream()
            .map(AssignmentResponseDTO::from)
            .collect(Collectors.toList());
    }

    public List<AssignmentResponseDTO> getAssignmentsByCourse(Long courseId) {
        return assignmentRepo.findByCanvasCourseIdOrderByDueAtAsc(courseId)
        .stream()
        .map(AssignmentResponseDTO::from)
        .collect(Collectors.toList());
    }

    public AssignmentResponseDTO getAssignmentByCanvasAssignmentId(Long canvasAssignmentId) {
        Assignment assignment = assignmentRepo.findByCanvasAssignmentId(canvasAssignmentId).orElseThrow(() -> new AssignmentNotFoundException("Assignment Not Found"));
        return AssignmentResponseDTO.from(assignment);
    }

    public AssignmentResponseDTO getAssignmentByCanvasId(Long canvasAssignmentId) {
        Assignment assignment = assignmentRepo.findByCanvasAssignmentId(canvasAssignmentId).orElseThrow(() -> new AssignmentNotFoundException("Assignment Not Found"));
        return AssignmentResponseDTO.from(assignment);
    }

    public AssignmentResponseDTO getAssignment(Long assignmentId) {
        Assignment assignment = assignmentRepo.findById(assignmentId).orElseThrow(() -> new AssignmentNotFoundException("Assignment Not Found"));
        return AssignmentResponseDTO.from(assignment);
    }

    public List<AssignmentResponseDTO> getIncompleteAssignments() {
        return assignmentRepo.findByCompletedFalseOrderByDueAtAsc()
        .stream()
        .map(AssignmentResponseDTO::from)
        .collect(Collectors.toList());
    }

    public List<AssignmentResponseDTO> getActiveBlockingAssignments() {
        return assignmentRepo.findByBlockingEnabledTrueAndDueAtAfter(LocalDateTime.now())
        .stream()
        .map(AssignmentResponseDTO::from)
        .collect(Collectors.toList());
    }

    public List<AssignmentResponseDTO> getAssignmentsByPriority(Priority priority) {
        return assignmentRepo.findByPriorityOrderByDueAtAsc(priority)
        .stream()
        .map(AssignmentResponseDTO::from)
        .collect(Collectors.toList());
    }

    @Transactional
    public AssignmentResponseDTO updateAssignment(Long id, AssignmentUpdateRequest updateRequest) {
        Assignment assignment = assignmentRepo.findById(id).orElseThrow(() -> new AssignmentNotFoundException("Assignment Not Found"));

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
    public AssignmentResponseDTO markAsCompleted(Long assignmentId, Boolean completed) {
        Assignment assignment = assignmentRepo.findById(assignmentId).orElseThrow(() -> new AssignmentNotFoundException("Assignment Not Found"));
        assignment.setCompleted(completed);

        if(completed) {
            assignment.setCompletedAt(LocalDateTime.now());
        }
        else {
            assignment.setCompleted(false);
            assignment.setCompletedAt(null);
        }

        Assignment completedAssignment = assignmentRepo.save(assignment);

        return AssignmentResponseDTO.from(completedAssignment);
        
    }

}
