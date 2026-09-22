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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
// This class is the api I make to access elements in canvas it will send get requests to canvas and talk to their api
public class CanvasApiClient {

    private final RestTemplate restTemplate;

    private final CanvasDomainValidator domainValidator;

    public CanvasUserDTO validateTokenAndGetUser(String schoolDomain, String token) {
        String fullUrl = domainValidator.normalize(schoolDomain) + "/api/v1/users/self/profile";

        try {
            ResponseEntity<CanvasUserDTO> response = restTemplate.exchange(fullUrl, HttpMethod.GET, authorized(token), CanvasUserDTO.class);

            return response.getBody();

        } catch (HttpClientErrorException.Unauthorized e) {
            throw new InvalidTokenException("Invalid Canvas access token");
        } catch (Exception e) {
            log.warn("Canvas request to {} failed: {}", fullUrl, e.getMessage());
            throw new CanvasApiException("Failed to connect to Canvas. Check the institution URL and try again.");
        }
    }

    public List<CanvasCourseDTO> getCourses(String schoolDomain, String token) {
        String fullUrl = domainValidator.normalize(schoolDomain) + "/api/v1/courses?per_page=1000";

        ResponseEntity<CanvasCourseDTO[]> response = restTemplate.exchange(fullUrl, HttpMethod.GET, authorized(token), CanvasCourseDTO[].class);

        return Arrays.asList(response.getBody());
    }

    public List<CanvasAssignmentDTO> getAssignments(String schoolDomain, String token, Long canvasCourseId) {
        String fullUrl = domainValidator.normalize(schoolDomain) + "/api/v1/courses/" + canvasCourseId
                + "/assignments?per_page=1000&include[]=submission&include[]=score_statistics";

        ResponseEntity<CanvasAssignmentDTO[]> response = restTemplate.exchange(fullUrl, HttpMethod.GET, authorized(token), CanvasAssignmentDTO[].class);

        return Arrays.asList(response.getBody());
    }

    private HttpEntity<String> authorized(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        return new HttpEntity<>(headers);
    }

}
