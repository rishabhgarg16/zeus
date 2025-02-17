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
            version = "0.8.8"
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
                    "eyJ2ZXIiOjEsImlzdSI6MTczOTc5OTIwNSwiZW5jIjoiQTEyOEdDTSIsInRhZyI6Ik4yb0g2TTJaLWhTbTdHUjZSckhyMmciLCJleHAiOjE3Mzk4NDI0MDUsImFsZyI6IkExMjhHQ01LVyIsIml2IjoiYUFNNXM0V3hIRl9keGJpeiJ9.B2GrXDsnCX2X4liKdLunCA.l6qjAmVTmzYz0szP.V271CcQ8Sg4yxzYCM_tn_9zwkdAu1m2MhIpY72pQ6zNmRNkWdq93VW4kW6edbnRXEIEhHx7mCQYAb2a3zrPU1XZdDqAVckw5jolHp03Wr7dSlpl0DuyQAd9RAISvi8yiZpaJtSskGrS3L3L37MXEX7hhOVYzqvu1nOYVtQX55zOQHNwfHTZqyeZkT_qoopY4Dlpn4n_klyruUtZW3T4NrhfAip-alZcFmMy4wmik0M4aHgenZMLTvbs3RFxfWEZLQ67jAVqYJW_lXUsJRnutUJ4WHgI1b22l-VVxpjlX-RV1Pph9c-d-XH31cG_M764ja33eXeXrlNNzoIl1gzjz0ZM7lYbUvyniSJUietJGim11fVOhf4GzKB-lUSud21-1hijOwCPmaqX4pwseTAXGk3qrsyKQi2l__DA2bPGnWE0rIOU52wd-NqK4fTJytJ27jVi08mtvzYbm-t9mS5pMJUGBMhPaZbRgCwnmd21v3NahH8wC6jUvOKHBGwEbOH96uLZYMorCcisDrqGRJ81gff4OO3Wj5_575B-eV1U2PFBhxIsKHTFnvD7Fz-M7VMosOMHc3yRvkwk8bZSsnqUtLsWChvA4dmMwYlkLQgFhBAg0XsP4425FejLwpel1weG26U6-Lc6iH4AtJJ0Zp037IEiPsqk-BFewOW7xd6NLdgBa0X_3-0-7X_ba1KW2zvnF3TWke6_D5O6YyvtRH9ZUDZCIhfOtyxPwTxniZQ4SjmJsRNA2jHQ0R-6eTaHvrfCPXJ3PABKmZYLXKshgrvSgNGBKsYFmVY-ohqNamucrlVmxNuNns3-lstLqlpWPTvCTzoGj4QPqe_-cUj6FJNTM82BEgmafNF-4a-GyDSq39Exbm7JNw2uqfFyVrI7kKSHGP9sjnnBLCDhdXVxc3PPjD7iNFvUlwDGJ3Mq5SxBQLyU4xrzHHYdoDxmt3pRXUdIuVOAjchBY7QqV2pVQ8qY.Ziy9ntvcKsXli-vp3_f-WQ"
            }
        }
    }
}
