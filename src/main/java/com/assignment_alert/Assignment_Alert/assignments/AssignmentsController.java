package com.assignment_alert.Assignment_Alert.assignments;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/assignments")
@RequiredArgsConstructor
@CrossOrigin
public class AssignmentsController {

    private final AssignmentService assignmentService;

    @GetMapping
    public ResponseEntity<List<AssignmentResponseDTO>> getAssignments(@RequestParam(required = false) Long courseId, @RequestParam(required = false) String filter, @RequestParam Long userId) {
        if(courseId != null) {
            return ResponseEntity.ok(assignmentService.getAssignmentsByCourse(courseId, userId));
        }

        String normalizedFilter = (filter == null) ? "upcoming" : filter;
        switch(normalizedFilter) {
            case "upcoming":
                return ResponseEntity.ok(assignmentService.getUpcomingAssignments(userId));

            case "incomplete":
                return ResponseEntity.ok(assignmentService.getIncompleteAssignments(userId));

            case "blocking":
                return ResponseEntity.ok(assignmentService.getActiveBlockingAssignments(userId));
            
            default: return ResponseEntity.ok(assignmentService.getUpcomingAssignments(userId));
        }
    }

    @GetMapping("/{assignmentId}")
    public ResponseEntity<AssignmentResponseDTO> getAssignment(@PathVariable Long assignmentId) {
        return ResponseEntity.ok(assignmentService.getAssignment(assignmentId));
    }

    @PutMapping("/{assignmentId}")
    public ResponseEntity<AssignmentResponseDTO> updateAssignment(@PathVariable Long assignmentId, @RequestBody AssignmentUpdateRequest updateRequest) {
        return ResponseEntity.ok(assignmentService.updateAssignment(assignmentId, updateRequest));
    }

    @PutMapping("/{assignmentId}/completed")
    public ResponseEntity<AssignmentResponseDTO> markAsCompleted(@PathVariable Long assignmentId, @RequestParam Boolean completed) {
        return ResponseEntity.ok(assignmentService.markAsCompleted(assignmentId, completed));
    }

}

    