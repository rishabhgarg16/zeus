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
            version = "0.8.12"
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
                    "eyJ2ZXIiOjEsImlzdSI6MTczOTk3NTY3MCwiZW5jIjoiQTEyOEdDTSIsInRhZyI6IkMyZER0S19EczN2VlhiRGl3Y3JFOFEiLCJleHAiOjE3NDAwMTg4NzAsImFsZyI6IkExMjhHQ01LVyIsIml2Ijoia2hIWTVkWExNZ3ZaRTZWNSJ9.ZZmCAZnKUbMCeimuWTf5tw.YKipqKf-ncauus9V.qgfQoRCGV-DKH0X4i45Gm0wUaSoDNZsX8GVS9cL3k5hsH5c5d56186dcEX1TT4JzgPKPqlCIFzOLbDP2vwKs0g4ximOBbdCkgdQDnXHVr9tl2x143KJtLUs9PXNr-qbOSM0-UYmA94zPGJaNlgl32MgYUCsFFXkxcAw5QcJL7dQWE4sgKXUBsuGNJ20VjEIFN6RU8Gt4h1mHWpQqDueV1LbpFlhwvnT0S-0nXxMWMZZYY5ScOXXJh3fvNL9k76OxJ2lxoog-9oOY3EPgtehN25lxRDDXRHumM-PwzH_9V5kVJHKNGYnV7ItyGIewYb3cp1k5SCxlitLw6aLBf0KBAfK6V3PoWk3wAE1eakQdVc-D2g2vnPu-JluF2ITYGbbZ3cq0epSjtLhaukBf3Ob0MCvfFu0zSlhvXqt4X-fBiSHc4Z_wc2PwwCVhJsxmNAumCWkS8Xjll-GQPrVwOI4qS8jsvbPer5X-K7mrHYSMAWfnkQPMSLSsAcUD0sSqNdPxbpNPMkTAWsHPAsxjbnHOJ2TiUfTTcy4BB4_Fn8YuRytVe7PeVZ97ix2l4nv_1L4d-v12khnpAXWkorMXGt2E9TsK_tRsaeJBPJ6LQDjEserEymCbIQ_BJW2nzXYI_UqwtEFB-QniT7e5yOUlRlTTAMoF254gPX5bXILbuX36rgo1pENk6svZEXzBB29w62vA8DmeUTOacq8wJ_3OUx4C2Y8QnSMXrS4cV7DDnBrGCkiGSXD24GcVCtjddeAapCqZcSqugbO0vNONVaKuUkbSP1yXzhUkYXS9k9KqM67g5vFZgqRXluD3-FmIL3zqvwueaZTiLt1Y7DYuL4L_gAZUpPSPLevXWX-ICN48FcGHoJzQ02KqJaCJpw03r-9O06JqzmKK2GWKVtylPNc6PmywB8MD7o2hrLfAdtg0ImgUQyPOFkC74DMLuID_3JdsN4La53UkqR9rXbH9OyzbrQ5ju6WEyzeUcDNQPVJuXkYWUI3sEdOQMtZG-Q.oOaln9R7vzME_B_HXhzs9Q"
            }
        }
    }
}
