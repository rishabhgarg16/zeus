plugins {
    kotlin("jvm") version "1.7.22"
    id("org.springframework.boot") version "2.7.16"
    id("io.spring.dependency-management") version "1.1.5"
    kotlin("plugin.spring") version "1.7.22"
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
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("javax.validation:validation-api:2.0.1.Final")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    implementation("com.mysql:mysql-connector-j:8.0.33")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework:spring-messaging")
    implementation("com.google.firebase:firebase-admin:8.1.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // Kotlin dependencies - make sure these are explicitly included
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.7.22")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.7.22")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.7.22")

    // Update coroutines to match Kotlin 1.7.22
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("redis.clients:jedis")
    implementation("com.auth0:java-jwt:4.4.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation(kotlin("script-runtime"))
    //  AWS SDK v2 dependencies
    implementation(platform("software.amazon.awssdk:bom:2.21.42"))
    implementation("software.amazon.awssdk:sqs")

    implementation("org.antlr:antlr4-runtime:4.9.3")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
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
            version = "0.9.14"
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
                    "eyJ2ZXIiOjEsImlzdSI6MTc0MDk5NDU0NiwiZW5jIjoiQTEyOEdDTSIsInRhZyI6IlFpVEU3TE13MEIyeTdsUTVLQWEyYWciLCJleHAiOjE3NDEwMzc3NDYsImFsZyI6IkExMjhHQ01LVyIsIml2IjoiWU50ei1BVnkzbjZRTlY3eSJ9.KTb1UT4dSjDBt0tBjt23Xw.R48cMtvkQ046L1Zq.-2SyrPbjFD9B47BokZJdrnR74WLGyntmlIQoHUZgN-76_3Mek4SXmBmkzCRHGNjXSUfXmupM5ekye9L_MfwjcU2-9SVlQxGtAuQHK_bZ514v7f4wM_yPj5R0qR4XFuHBqj0raV4rqQb0rh6XiNalQREi6MbwE-sIveja1r4OGTXuraQmzdPwnV4JC9BM7MmMBLXfIdXv4dYfmMvJac2DGuwN5VxsgirSChZiHu8fcwwKbf7Cat_dGpcjzNPxOcOf-QJWGqZVwIvzOn0u8EdBcfawp4IgSBXkWlHkVExhqMWvKdVxP-5OCLalvgqeKZk4UCf4_crr9-S_4KCg919lTuWZJbb3BAl4wm1FFhkOTFPW60iBtjEvf1jgLJG8sznslsJWhJt7aTNsvytBxPi4tNVijIvSbzKui_sqIlcGdjHrSMw7VnvLMLr2CtITzmxppmtq4o3wx9w1-YTkNVJCQY5mDbu289s8IMNCT_znJKIsw2AN76Il_HqCTPgJdKJ48ZaMu48zn-H_D4aKhcyId_jntI8ikKrotOv-eiI3kxE2pXdUXNTNd3JWprq1KRDWUjNbZdWwAEVyczjMuDI3JwIi94pc8Ck1xfnVCbKex4dFmjKZ64DA-hLMvPxOITi7t-1iZM9sscZPzqfaFOIBu7u8_InC3lemzI306gmUQkhMeVQTdZ5KgH3NFE6mcaJZyPvDdIc8fputAKiIgskZOhWj_AQfcBC3FoPUQbw5dN3KsM3sTgQT_64UWRh5TYl_SR-IXA7QoSOVzBU1DziVrL9SCyL9QsgajayeC8GUvMRLKIAuMUq9Hj8a62GF1yI8h7EJTfwd2MjjtRvb8jjEph0yz0wtZF0RkYKbefl1X-qbNNj9klaLqkmd2elVhF5a4Ho7y_1hVfSmzamHB0Jn2jfijL68HIKuQK579qz8kYgNsCoxtxmBpjmCn99nSw19JI8c3jo_CkuOm_cBmEPzNiWwOS59hH8cSLS4UHHwSOqD0WZa_HR8.qC6DJfsqsqgY01BqV4ZOPg"
            }
        }
    }
}
