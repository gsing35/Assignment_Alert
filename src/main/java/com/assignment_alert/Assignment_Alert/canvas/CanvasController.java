package com.assignment_alert.Assignment_Alert.canvas;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.assignment_alert.Assignment_Alert.user.UserRepository;
import com.assignment_alert.Assignment_Alert.canvas.dtos.CanvasConnectionDTO;
import com.assignment_alert.Assignment_Alert.user.User;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/canvas")
@RequiredArgsConstructor
@CrossOrigin
public class CanvasController {

    private final CanvasAuthenticationService canvasService;
    //private final RestTemplate e;
    
    @PostMapping("/connect")
    public ResponseEntity<CanvasConnectionDTO> connectCanvas(@RequestBody CanvasAuthenticationRequest request) {

        User user = canvasService.connectCanvasAccount(request.domain(), request.accessToken());

        CanvasConnectionDTO response = new CanvasConnectionDTO(
        user.getUserId(),
        user.getName(),
        user.getEmail(),
        user.getSchoolDomain(),
        "Connected successfully"
    );

        return ResponseEntity.ok(response);
    }
}
