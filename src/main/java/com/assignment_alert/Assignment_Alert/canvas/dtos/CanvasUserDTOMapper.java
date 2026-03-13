package com.assignment_alert.Assignment_Alert.canvas.dtos;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.assignment_alert.Assignment_Alert.aws.AwsSecretsManagerService;
import com.assignment_alert.Assignment_Alert.user.User;
import com.assignment_alert.Assignment_Alert.user.UserRepository;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@NoArgsConstructor
@Slf4j
public class CanvasUserDTOMapper {

    public User toEntity(CanvasUserDTO userDto, String domain, UserRepository userRepo, String canvasToken, AwsSecretsManagerService secretManager) {

        Optional<User> existingUser = userRepo.findByCanvasId(userDto.canvasId());
        User user;

        if (existingUser.isPresent()) {
            user = existingUser.get();
            log.info("This is user canvasId", user.getCanvasId());
            String awsArn = secretManager.updateToken(user.getUserId(), canvasToken);
            log.info("This is awsArn: {}", awsArn);

            user.setAwsSecretArn(awsArn);   // Still update email/name if possible in case those changed 
            log.info("User.get arn: {}", user.getAwsSecretArn());
            if (userDto.email() != null) {
                user.setEmail(userDto.email());
            }
            if (userDto.name() != null) {
                user.setName(userDto.name());
            }

        } else {
            user = new User();
            user.setCanvasId(userDto.canvasId());
            log.info("This is user canvasId", user.getCanvasId());
            if (userDto.email() != null) {
                user.setEmail(userDto.email());
            }
            if (userDto.name() != null) {
                user.setName(userDto.name());
            }
            user.setSchoolDomain(domain);
            log.info("This is user domaain", user.getSchoolDomain());
            user = userRepo.save(user);
            String awsArn = secretManager.postToken(user.getUserId(), canvasToken);
            log.info("This is awsArn: {}", awsArn);
            user.setAwsSecretArn(awsArn);
            log.info("User.get arn: {}", user.getAwsSecretArn());

        }
        return user;

    }

}
