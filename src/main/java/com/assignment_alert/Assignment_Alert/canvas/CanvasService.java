package com.assignment_alert.Assignment_Alert.canvas;

import org.springframework.boot.security.autoconfigure.SecurityProperties.User;
import org.springframework.stereotype.Service;

import com.assignment_alert.Assignment_Alert.user.UserRepository;
import com.assignment_alert.Assignment_Alert.canvas.CanvasApiClient;
import com.assignment_alert.Assignment_Alert.user.UserDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CanvasService {

    private final UserRepository userRepo;
    private final CanvasApiClient canvasApiClient;

    public User registerUser(CanvasAuthenticationRequest request) {
        UserDTO userDTO = canvasApiClient.registerUser(request.schoolUrl(), request.accessToken());



    }
    
}
