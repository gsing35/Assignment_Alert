package com.assignment_alert.Assignment_Alert.assignments;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentRepository extends JpaRepository<Assignments, Long> {
    
    public Optional<Assignments> findByUrl(String url);
    public boolean existsWithUrl(String url);
    

}
