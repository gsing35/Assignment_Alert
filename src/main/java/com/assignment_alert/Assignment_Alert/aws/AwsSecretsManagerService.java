package com.assignment_alert.Assignment_Alert.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class AwsSecretsManagerService {

    @Value("${aws.region}")
    private String region;

    private final SecretsManagerClient secretClient;
    // TODO implement the methods needed to store and get secrets from secret manager 
    // all the basic crud methods for the tokens, dont need to make controller bc this is util class that will be use dby other servives
    // Make controllers for when users would "access" it
    // Some good architecure and method info on recent claudes

    // Take input from frontend and try to store token in secret manager, if secret name (userId) exists in secret manager then update token
    public String postToken(Long userId, String token) {
        try {
            String secretName = "User-" + userId;
            log.info("This is secret name: {}", secretName);

            CreateSecretRequest postRequest = CreateSecretRequest.builder()
                    .name(secretName)
                    .description("Canvas access token for user" + userId)
                    .secretString(token)
                    .build();

            CreateSecretResponse postResponse = secretClient.createSecret(postRequest);

            log.info("ARN to return {}", postResponse.arn());

            return postResponse.arn().substring(52);

        } catch (ResourceExistsException e) {
            return updateToken(userId, token);
        } catch (Exception e) {
            log.error("AWS Secrets Manager error details: ", e);
            log.error("Exception type: {}", e.getClass().getName());
            if (e.getCause() != null) {
                log.error("Caused by: {}", e.getCause().getMessage());
            }
            throw new RuntimeException("Failed to store token in AWS: " + e.getMessage(), e);
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

            PutSecretValueResponse putResponse = secretClient.putSecretValue(putRequest);

            log.info("ARN to return {}", putResponse.arn());

            return putResponse.arn().substring(52);

        } catch (Exception e) {
            throw new RuntimeException("Failed to update token in AWS Secrets Manager", e);
        }
    }

    public String getToken(Long userId) {
        try {
            String secretName = "User-" + userId;
            log.info("This is userId from getToken", userId);

            GetSecretValueRequest getRequest = GetSecretValueRequest.builder()
                    .secretId(secretName)
                    .build();

            GetSecretValueResponse getResponse = secretClient.getSecretValue(getRequest);

            log.info("This is getResponse.secretString {}", getResponse.secretString());
            // Check to see what this returns make it a substring if it is like 60 chars long

            return getResponse.secretString();

        } catch (Exception e) {
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

        } catch (Exception e) {
            throw new RuntimeException("Failed to delete user form AWS");
        }
    }

}
