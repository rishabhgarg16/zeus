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
            version = "0.8.4"
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
                    "eyJ2ZXIiOjEsImlzdSI6MTczOTY0NDQxOCwiZW5jIjoiQTEyOEdDTSIsInRhZyI6IjJzbkdBVkI2UlNmZng5MzFCWVRUMmciLCJleHAiOjE3Mzk2ODc2MTgsImFsZyI6IkExMjhHQ01LVyIsIml2IjoiNmcycGZSM0dFUWs2eHZ3RSJ9.MQuE6o3eYVl1NyF8QTttyQ.FhJm5ub67kTEEI_F.d70cJAfWvNp3cM13HGQFt-tMijctus0y8VqUR-o-caCkFWS4QbyNsF_3_Q6py0yl9SmjzcBaDlMieKMfYZnlb47Sl8HupFoF-n8Xu_Ma7Q93eJEV0Exk6hyQ3u7TrdyPOLh8Bm_NgA-szCei5IZylaXLxPNzeGDLRt5ZJIVdqHSj9RhEifU8DTnH-SZ2b3PMwG2AaPvorq9T290225o1GJcaI7dOz9pKB3UYIxPiSpwOF2ycpIpgAKi-rBRKqUF3Z34-e96lBf7vQPlbcvK8z7sXwS-HsNGMOUXENUU3iX4PcF5sbUWw7kkgk8H9_IXzA5NE7HAEKhqV1hgEsTw-Nr9maL2eqfzBEJL9GaTXRfIF0oRfooMP3GGLKDzP3oibL30HC5edsuWvytR0-4odoXZcymXAYFDOxfhzMfrj5Kp6d2INbSXVmcraNoWp_xLRedI6ZuSzAgyHd3hdUUYk4EzR9YBEbx7ibnLnepsCGTn31FPJVLyoaAVXW97WWNp6Dp60gs0ksNaH_S8DG3vF8rlfe6f_gjYW2dsdFdHocNmaqvbDsabtqgdsVub3b1NGMrq122xE8LSSZTrUx-Aiej5OVWG1jJE4yCAPDpUa_3QSisntRRg25FzlwC4EvarvKfdg3ER9--OSjUgg2umlswOvOvYKpnBgFFVVtDRnkjPf9oJg-lw4vh3wSXVCY4a2S4uZWDVag0jEhfqBP6fZkhTTTo0Tqo46yjFmAx81MYdSbeYWNf66GPki2bcTuNx-HO5E_g_MDP1uUy7vPKqux0mLOr3xMj49v5JoM2-FyLQZjqtQQK1U2S2KAQ5N4rZ7bzvh_cpJiH7tHPIRFoFbAC6qT6YYJlqFdFWjTUC1V3V2kUJy3qK2QkOlFLRvvX4BON4n_NC-nR4bfYNNptfHCQp27uswi54TaeVfL25aNRlSt26izzN_66WMkJeCaoU-avy0a60n6LbhJjkCsvadfLk.KqRxvlpa_INzlIFs4tci-Q"
            }
        }
    }
}
