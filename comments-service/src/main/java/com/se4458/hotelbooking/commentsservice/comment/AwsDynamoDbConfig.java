package com.se4458.hotelbooking.commentsservice.comment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
public class AwsDynamoDbConfig {

    @Bean
    public DynamoDbClient dynamoDbClient(
            @Value("${app.aws.region}") String region,
            @Value("${app.aws.access-key-id}") String accessKeyId,
            @Value("${app.aws.secret-access-key}") String secretAccessKey
    ) {
        var builder = DynamoDbClient.builder()
                .region(Region.of(region));

        if (StringUtils.hasText(accessKeyId) && StringUtils.hasText(secretAccessKey)) {
            builder.credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKeyId, secretAccessKey)
            ));
        }

        return builder.build();
    }
}
