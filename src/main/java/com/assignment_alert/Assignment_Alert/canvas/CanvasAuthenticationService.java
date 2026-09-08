package com.assignment_alert.Assignment_Alert.canvas;

import org.springframework.stereotype.Service;

import com.assignment_alert.Assignment_Alert.aws.AwsSecretsManagerService;
import com.assignment_alert.Assignment_Alert.canvas.dtos.CanvasUserDTO;
import com.assignment_alert.Assignment_Alert.canvas.dtos.CanvasUserDTOMapper;
import com.assignment_alert.Assignment_Alert.user.User;
import com.assignment_alert.Assignment_Alert.user.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
// Class that will be called from canvasController to call canvasapiclient to validate tokens, call awssecret to store need user repo to save to db and find users, and sync to get data 
public class CanvasAuthenticationService {

    private final AwsSecretsManagerService secretManager;

    private final CanvasApiClient canvasApi;

    private final CanvasSyncService syncService;

    private final UserRepository userRepo;

    private final CanvasUserDTOMapper userMapper;

    // Get the dto from the api checking that the domain and token are valid then use mapper to create user entity with all the fields and then sync the user with their assignments
    @Transactional
    public User connectCanvasAccount(String domain, String canvasToken) {

        CanvasUserDTO userDto = canvasApi.validateTokenAndGetUser(domain, canvasToken);

        User user = userMapper.toEntity(userDto, domain, userRepo, canvasToken, secretManager);
        user = userRepo.save(user);
        syncService.initalSync(user);

        return user;
    }

    //todo make a new method to connect to canvas with an existing user and a new token 
    public User dissconnectCanvasAccount() {
        //Not needed yet
        return null;
    }

}
