package com.assignment_alert.Assignment_Alert.canvas.dtos;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.assignment_alert.Assignment_Alert.aws.AwsSecretsManagerService;
import com.assignment_alert.Assignment_Alert.user.User;
import com.assignment_alert.Assignment_Alert.user.UserRepository;

import lombok.NoArgsConstructor;

@Component
@NoArgsConstructor
public class CanvasUserDTOMapper {

    public User toEntity(CanvasUserDTO userDto, String domain, UserRepository userRepo, String canvasToken, AwsSecretsManagerService secretManager) {

        Optional<User> existingUser = userRepo.findByCanvasId(userDto.canvasId());
        User user;

        if (existingUser.isPresent()) {
            user = existingUser.get();
            String awsArn = secretManager.updateToken(user.getUserId(), canvasToken);

            user.setAwsSecretArn(awsArn);   // Still update email/name if possible in case those changed
            user.setSchoolDomain(domain);
            if (userDto.email() != null) {
                user.setEmail(userDto.email());
            }
            if (userDto.name() != null) {
                user.setName(userDto.name());
            }

        } else {
            user = new User();
            user.setCanvasId(userDto.canvasId());
            if (userDto.email() != null) {
                user.setEmail(userDto.email());
            }
            if (userDto.name() != null) {
                user.setName(userDto.name());
            }
            user.setSchoolDomain(domain);
            user = userRepo.save(user);
            String awsArn = secretManager.postToken(user.getUserId(), canvasToken);
            user.setAwsSecretArn(awsArn);

        }
        return user;

    }

}
