package com.assignment_alert.Assignment_Alert.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.CreateSecretRequest;
import software.amazon.awssdk.services.secretsmanager.model.CreateSecretResponse;
import software.amazon.awssdk.services.secretsmanager.model.DeleteSecretRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.PutSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.PutSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.ResourceExistsException;

@Service
@RequiredArgsConstructor
public class AwsSecretsManagerService {

    @Value("${aws.region}")
    private String region;

    private SecretsManagerClient secretClient;
    // TODO implement the methods needed to store and get secrets from secret manager 
        // all the basic crud methods for the tokens, dont need to make controller bc this is util class that will be use dby other servives
        // Make controllers for when users would "access" it
        // Some good architecure and method info on recent claudes

    // Take input from frontend and try to store token in secret manager, if secret name (userId) exists in secret manager then update token
    public String postToken(Long userId, String token) {
        try {
            String secretName = "User-" + userId;

            CreateSecretRequest postRequest = CreateSecretRequest.builder()
            .name(secretName)
            .description("Canvas access token for user" + userId)
            .secretString(token)
            .build();

            CreateSecretResponse postResponse = secretClient.createSecret(postRequest);

            return postResponse.arn();

        } catch(ResourceExistsException  e) {
            return updateToken(userId, token);
        } catch(Exception e) {
            throw new RuntimeException("Failed to store token in AWS");
        }
    }

    // Update the token from either frontend request or when trying to store new token with known user id
    public String updateToken(Long userId, String token) {
        try {
            String secretName = "User-" + userId;

            PutSecretValueRequest putRequest = PutSecretValueRequest.builder()
            .secretId(secretName)
            .secretString(token)
            .build();

            PutSecretValueResponse  putResponse = secretClient.putSecretValue(putRequest);

            return putResponse.arn();

        } catch(Exception e) {
            throw new RuntimeException("Failed to update token in AWS Secrets Manager", e);
        }
    }

    public String getToken(Long userId) {
        try {
            String secretName = "User-" + userId;

            GetSecretValueRequest getRequest = GetSecretValueRequest.builder()
            .secretId(secretName)
            .build();

            GetSecretValueResponse getResponse = secretClient.getSecretValue(getRequest);

            return getResponse.secretString();

        } catch(Exception e) {
            throw new RuntimeException("Failed to retrieve token from AWS");
        }
    }

    public void deleteToken(Long userId) {
        try {
            String secretName = "User-" + userId;

            DeleteSecretRequest deleteRequest = DeleteSecretRequest.builder()
            .secretId(secretName)
            .forceDeleteWithoutRecovery(true)
            .build();

            secretClient.deleteSecret(deleteRequest);

        } catch(Exception e) {
            throw new RuntimeException("Failed to delete user form AWS");
        }
    } 
    
}
