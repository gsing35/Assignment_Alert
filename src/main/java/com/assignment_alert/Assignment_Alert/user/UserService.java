package com.assignment_alert.Assignment_Alert.user;

import org.springframework.stereotype.Service;

import com.assignment_alert.Assignment_Alert.exceptions.UserNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;

    public UserResponseDTO findUserByUserId(Long userId) {
        User user = userRepo.findByUserId(userId).orElseThrow(() -> new UserNotFoundException("User not found"));
        return UserResponseDTO.from(user);
    }
    
}
