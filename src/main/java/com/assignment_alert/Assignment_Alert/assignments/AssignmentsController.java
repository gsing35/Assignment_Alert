package com.assignment_alert.Assignment_Alert.assignments;

import java.util.List;

import org.springframework.http.ResponseEntity;
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
public class AssignmentsController {

    private final AssignmentService assignmentService;

    @GetMapping
    public ResponseEntity<List<AssignmentResponseDTO>> getAssignments(@RequestParam(required = false) Long courseId, @RequestParam(required = false) String filter) {
        if(courseId != null) {
            return ResponseEntity.ok(assignmentService.getAssignmentsByCourse(courseId));
        }

        if(filter.equals("upcoming")) {
            return ResponseEntity.ok(assignmentService.getUpcomingAssignments());
        }

        if(filter.equals("incomplete")) {
            return ResponseEntity.ok(assignmentService.getIncompleteAssignments());
        }

        if(filter.equals("blocking")) {
            return ResponseEntity.ok(assignmentService.getActiveBlockingAssignments());
        }

        return ResponseEntity.ok(assignmentService.getUpcomingAssignments());
    }

    @GetMapping("/{assignmentId}")
    public ResponseEntity<AssignmentResponseDTO> getAssignment(@PathVariable Long assignmentId) {
        return ResponseEntity.ok(assignmentService.getAssignment(assignmentId));
    }

    @PutMapping("/{assignmentId}")
    public ResponseEntity<AssignmentResponseDTO> updateAssignment(@PathVariable Long assignmentId, @RequestBody AssignmentUpdateRequest updateRequest) {
        return ResponseEntity.ok(assignmentService.updateAssignment(assignmentId, updateRequest));
    }

    @PutMapping("/{assignmentId}/{completed}")
    public ResponseEntity<AssignmentResponseDTO> markAsCompleted(@PathVariable Long assignmentId, @PathVariable Boolean completed) {
        return ResponseEntity.ok(assignmentService.markAsCompleted(assignmentId, completed));
    }

}

    