package com.assignment_alert.Assignment_Alert.user;

import java.util.ArrayList;
import java.util.List;

import com.assignment_alert.Assignment_Alert.courses.Course;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(name = "unique_canvas_id", columnNames = "canvas_id"),
    @UniqueConstraint(name = "unique_email", columnNames = "email")
})
@Entity
public class User {

    @Id
    @SequenceGenerator(name = "user_id_seq", sequenceName = "user_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_id_seq")
    @Column(nullable = false, updatable = false)
    private Long userId;

    @Column(columnDefinition = "TEXT")
    private String schoolDomain;

    @Column(columnDefinition = "TEXT")
    private String awsSecretArn;

    @Column(nullable = false, updatable = false)
    private Long canvasId;

    @Column
    private String name;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Course> courses = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String email;

}
