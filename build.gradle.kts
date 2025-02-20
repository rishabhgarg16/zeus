plugins {
    kotlin("jvm") version "1.9.23"
    id("org.springframework.boot") version "2.7.6"
    id("io.spring.dependency-management") version "1.1.5"
    kotlin("plugin.spring") version "1.5.31"
    id("maven-publish")
    id("java")
}

apply(plugin = "kotlin")
apply(plugin = "application")


group = "com.hit11"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter:2.7.6")
    implementation("org.springframework.boot:spring-boot-starter-web:2.7.6")
    implementation("org.springframework.boot:spring-boot-starter-validation:2.7.6")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:2.7.6")
    implementation("mysql:mysql-connector-java:8.0.26")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework:spring-messaging")
    implementation("com.google.firebase:firebase-admin:8.1.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.+")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.3")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("com.squareup.okhttp3:okhttp:4.9.0")
    implementation("redis.clients:jedis")
    implementation("com.auth0:java-jwt:4.4.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test:2.7.6")
    implementation(kotlin("script-runtime"))
//    AWS SDK v2 dependencies
    implementation(platform("software.amazon.awssdk:bom:2.17.290"))
    implementation("software.amazon.awssdk:sqs")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(8)
}

tasks.jar {
    enabled = false
    manifest.attributes["Main-Class"] = "com.hit11.zeus.ZeusApplication"
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            artifact(tasks.bootJar.get())
            groupId = "ai.hit11"
            artifactId = "zeus"
            // if getting 409, bump the version
            version = "0.9.0"
        }
    }
    repositories {
        maven {
            url = uri("https://hitcentral-590183692348.d.codeartifact.ap-south-1.amazonaws.com/maven/hitcentral/")
            credentials {
                username = "aws"
                // if getting 401, run
                // aws codeartifact get-authorization-token --domain hitcentral --domain-owner 590183692348 --region ap-south-1 --query authorizationToken --output text
                password =
                    "eyJ2ZXIiOjEsImlzdSI6MTc0MDA1MTkwNiwiZW5jIjoiQTEyOEdDTSIsInRhZyI6Imp4M2IxaFotWkQzdWhKcTRQajFSRFEiLCJleHAiOjE3NDAwOTUxMDYsImFsZyI6IkExMjhHQ01LVyIsIml2IjoidG9HVXJxY2MySmVGYVBOTSJ9.YR8q19DVLl8xIpRDKZziJA.Lj3yyXv0OvvDV4Nn.CFDCMofD0jdrW9aZE2JKpqp5VO_asBbtU1c2YMYYKqvAhMBApWolXNTbSWueOMYo-fCSiBue2dedY7Ag-Sn8et5kLM8_OvbHAmnAnLM7jfXxRB1nukVdzcM58gWQLICuts4zE8DKwUMFWW7xWEIwedVaYFw2zOVjPeM3OaLLC3I3nt80ZWwBC13X0Xo0pMPcwU1WzUk3qRmxWZtX9vKDGz1P7f1X3RFyQu5fe_oqYHolqKgSo7eUl-Bpc9PdyNbmRbTFrZ5SWdSUiKlYbSSvL0lGoSEKu0BDpSm_2Fm-k786LNOof3JMW-OY1-h153F7t4Dj2pwl44Pf1mdxNcqNGzjnC7KV_MQAECah3upP6CikjMd5d_1av2T-qRR5iP5hUNC7k_cS4ZbR6aQo9RYOn0EMEZ4cHQZuLnj2ApT6nIEhNo1pvIM3nkSCn4jXqQOfgDNGfiOWYSFL_Cgg-5PUmmk_YiV1O8trc5CQfg-SWcvbcTP8P9RYREoI6BympaAWDIwXoiJ0amjx-HKk6_23iDCHPz9DVvyYWlABPl34jXlGOzWqg4aIGftdQHRPXW-rq2ikrmQPAl4cnHI0jzONLJ1SWwc120c-_86YKw6q1UBjUGPYLiYaS1JzcX2M_WTcMeFwOAo9BjPpeneX6pDSgfTtjmzYbkG1s8LQD7U_hApPmJ-V3A5fgGRbn1hB-lHXvv_dm1Wcf0kCB_IZ5SDtJ1sP7aeeaJ_dTY6Nw_wF-qYUp7Nho6zge8nLPgb7DC_4VdrUtPWDFkhJw-_comhMX7klYv376VXMFW3NyhBeX9giVys7KShhebgkfx7CuxBlQqV78DmkilB6_-jCnBp0939NTqzawIVQ_IicHN9tjL6dO0f7Lma1jjB709_AHTt4j2bbTgH0LQFWkJ6iK5WEegJmS2UKgRbZJwg9bqOiGdtDeiV6jO-x1hRQ3s0B1P0WvKZ3SNHcNQO49PPn_Ix99tM344EgroD7XTN7e57yulxMtcVB4g.CWbqxQAKI1U5zCY5mG-bnw"
            }
        }
    }
}
