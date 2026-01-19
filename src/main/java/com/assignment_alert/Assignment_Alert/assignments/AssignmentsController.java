package com.assignment_alert.Assignment_Alert.assignments;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    @PostMapping
    public ResponseEntity<Assignments> saveAssingment(@RequestBody AssignmentRegistrationRequest request) {
        Assignments assignment = assignmentService.saveAssignment(request);
        URI url = URI.create("api/v1/assignments/" + assignment.getAssignmentId());
        return ResponseEntity.created(url).body(assignment);
    }

    @GetMapping(value="/{assignmentId}")
    public ResponseEntity<Assignments> getAssignmentById(@PathVariable("assignmentId") Long assignmentId) {
        return ResponseEntity.ok(assignmentService.getAssignmentById(assignmentId));
    }

    @GetMapping
    public ResponseEntity<Page<Assignments>> getAllAssignments(
            @RequestParam(defaultValue="0") int page, 
            @RequestParam(defaultValue="10") int size,
            @RequestParam(defaultValue="assignmentId") String sortBy) {
        return ResponseEntity.ok(assignmentService.getAllAssignments(page, size, sortBy));
    }

    @PostMapping(value="/{assignmentId}")
    public ResponseEntity<Void> updateAssignment(@PathVariable("id") Long id, @RequestBody AssignmentUpdateRequest request) {
        assignmentService.updateAssignment(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(value="/{assignmentId}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable("assignmentId") Long id) {
        assignmentService.deleteAssignment(id);
        return ResponseEntity.noContent().build();
    }
 
        // Implementted pagtion for get all, next to do is add expceptions
    
}
