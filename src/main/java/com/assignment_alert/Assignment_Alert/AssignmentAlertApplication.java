package com.assignment_alert.Assignment_Alert;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AssignmentAlertApplication {

	public static void main(String[] args) {
		SpringApplication.run(AssignmentAlertApplication.class, args);
	}

	@Bean
	CommandLineRunner commandLineRunner() {
		
	}

}
