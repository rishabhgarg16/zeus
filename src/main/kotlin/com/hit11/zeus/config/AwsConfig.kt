package com.hit11.zeus.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsClient

@ConstructorBinding
@ConfigurationProperties(prefix = "aws")
data class AwsProperties(
    val region: String,
    val accessKeyId: String,
    val secretKey: String,
    val sqs: Sqs
) {
    data class Sqs(
        val queueUrl: String
    )
}

@Configuration
@EnableConfigurationProperties(AwsProperties::class)
class AwsConfig(private val awsProperties: AwsProperties) {
    @Bean
    fun sqsClient(): SqsClient {
        val credentials = AwsBasicCredentials.create(
            awsProperties.accessKeyId,
            awsProperties.secretKey
        )

        return SqsClient.builder()
            .region(Region.of(awsProperties.region))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .build()
    }
}