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
            version = "0.8.16"
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
                    "eyJ2ZXIiOjEsImlzdSI6MTc0MDA0MDUwMSwiZW5jIjoiQTEyOEdDTSIsInRhZyI6ImUxazVqcGJWb3ZCS3BzRGFPSXlmU3ciLCJleHAiOjE3NDAwODM3MDEsImFsZyI6IkExMjhHQ01LVyIsIml2IjoiV2JEeFpXSTJzbDNFNlN2RiJ9.AwoUeFlwvFWE2kO6YGHJlA.ZrijBJSDjn0ufYUO.Zk7RqKeBjGzB9zD6Po-ER720X_sL_pO5Iu1PR3cKfseLwtGWnxpTF358U9pgBpnxJhExNPIUTgI9P0zzdmq5LIX1-TQ7M_qahOBKIbnnnt0RblzBI3R6N8lf3HEIXHdx4acIqsVO_FR750HLGqBzY7Uqe11MRg_dmWbWs3tYPbn09kYwbLpKNcavtNqI_y8bXIPVtwnPMFWIfRcU3nTG2S4SFXnlpDj67LhunVywDXrsxAy5tfvWMSftoSyTCJr3IQOhPRuSOGvj6xMZmAZmz-PAvET6Vuk-nqzbarKvaa12QK_3mIuEBsedhmUwyJO3yw0ar7J_9ZITib-utrVfmEFfQmDOTnXpsekQQI-jI-MPH0MyBwfAl8_HF_82CZssTm1ucCV6YnAKg7RGbNg6LjdXjdPHWsC-8eHEpI2vBfds41Ef6tydRlLikdagCcogyXd0GnsaiLNMJUja_k5XuUGCnvGGm20qkAAq0zwLu6AsYLmxE-O5MTcp-iH1Vp-HOGQ3gkHqWW_1FgSTMBvrDMU6TjlQ95NEEI3OkCbe5nHM5yToP2FLQ4qoqnpqkqHywl9kd0QbdmZyHAULWCgqSsT9YTT524Ieia6SeDXVaueNPC8HIr8Sj5GTBy-DtZoNS-CWoh_VFBjGyGxyPZKTWxM4mgEEfibPtWBzcrwdkW6Zgmqv9c-uafA-F8J5xi2WyiFloWEL-SZnDVTzdxxQw_-remg-2voVKKhbOl1RYCyxTVbm0_pMB9RmZr8jvseMUHAQT3MrxGoCIVTS_pA8BF9Id9NGVSEnRhzDptjMc1B-80BlZ-VKUQEQ4yB8NtFFipJnVH1bkQKtrFlaen5DkbxMLesraFcRutfH7U5vflbdUoZxud_uzT9g9u1sLg5i8S0e_NsaQvKIu7Urj_dXFgogztCo4DxWtcbGs9iF1HWJZtgKBagsSGwT4HPRfL1371w5nCft9wagmcUY4OhHndi7zSCpQCp7oKX99QZVoxM.8ipC2boC78GdSYVn4_M3mQ"
            }
        }
    }
}
