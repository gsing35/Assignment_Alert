package com.assignment_alert.Assignment_Alert.canvas;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.assignment_alert.Assignment_Alert.canvas.dtos.CanvasAssignmentDTO;
import com.assignment_alert.Assignment_Alert.canvas.dtos.CanvasCourseDTO;
import com.assignment_alert.Assignment_Alert.canvas.dtos.CanvasUserDTO;
import com.assignment_alert.Assignment_Alert.exceptions.CanvasApiException;
import com.assignment_alert.Assignment_Alert.exceptions.InvalidTokenException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
// This class is the api I make to access elements in canvas it will send get requests to canvas and talk to their api
public class CanvasApiClient {

    private final RestTemplate restTemplate;

    public CanvasUserDTO validateTokenAndGetUser(String schoolDomain, String token) {
        try {
            String fullUrl = schoolDomain + "/api/v1/users/self/profile";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<CanvasUserDTO> response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, CanvasUserDTO.class);

            return response.getBody();

        } catch(HttpClientErrorException.Unauthorized e) {
            throw new InvalidTokenException("Invalid Canvas access token");
        } catch(Exception e) {
            throw new CanvasApiException("Failed to connect to Canvas: " + e.getMessage());
        }
        
    }

    public List<CanvasCourseDTO> getCourses(String schoolDomain, String token) {
            String fullUrl = schoolDomain + "/api/v1/courses";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<CanvasCourseDTO[]> response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, CanvasCourseDTO[].class);

            return Arrays.asList(response.getBody());
    }

    public List<CanvasAssignmentDTO> getAssignments(String schoolDomain, String token, Long canvasCourseId) {
        String fullUrl = schoolDomain + "/api/v1/courses/" + canvasCourseId + "assignments?per_page=100&include[]=submission&include[]=score_statistics";

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(token);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<CanvasAssignmentDTO[]> response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, CanvasAssignmentDTO[].class);

        return Arrays.asList(response.getBody());
    }
    
}
