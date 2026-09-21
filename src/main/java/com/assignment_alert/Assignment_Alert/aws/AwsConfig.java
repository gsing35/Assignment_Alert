package com.assignment_alert.Assignment_Alert.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

@Configuration
public class AwsConfig {

    @Value("${aws.region}")
    private String awsRegion;

    /*
     * DefaultCredentialsProvider walks a chain of sources in order: environment
     * variables, the AWS_PROFILE / ~/.aws/credentials file, container credentials,
     * and finally the EC2 instance profile.
     *
     * This deliberately replaces InstanceProfileCredentialsProvider, which only ever
     * checks the EC2 instance metadata service. That worked for the maintainer's own
     * deployment but made the application impossible to run anywhere else - a
     * self-hoster on a laptop or a non-AWS VPS could not fetch Canvas tokens at all.
     * The default chain still finds the instance role when running on EC2, so nothing
     * changes there.
     */
    @Bean
    public SecretsManagerClient secretsManagerClient() {
        return SecretsManagerClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}