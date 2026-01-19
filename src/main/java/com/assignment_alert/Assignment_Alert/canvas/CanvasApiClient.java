package com.assignment_alert.Assignment_Alert.canvas;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.assignment_alert.Assignment_Alert.exceptions.CanvasApiException;
import com.assignment_alert.Assignment_Alert.exceptions.InvalidTokenException;
import com.assignment_alert.Assignment_Alert.user.UserDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CanvasApiClient {

    private final RestTemplate restTemplate;

    public UserDTO registerUser(String schoolDomain, String token) {
        try {
            String fullUrl = schoolDomain + "/api/users/self/profile";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<UserDTO> response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, UserDTO.class);

            return response.getBody();

        } catch(HttpClientErrorException.Unauthorized e) {
            throw new InvalidTokenException("Invalid Canvas access token");
        } catch(Exception e) {
            throw new CanvasApiException("Failed to connect to Canvas: " + e.getMessage());
        }
        
    }
    
}
