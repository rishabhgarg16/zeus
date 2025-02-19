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
            version = "0.8.14"
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
                    "eyJ2ZXIiOjEsImlzdSI6MTczOTk4NDA2NywiZW5jIjoiQTEyOEdDTSIsInRhZyI6InlHMDc3bnRVRXlseGN1YTN2VlNlNVEiLCJleHAiOjE3NDAwMjcyNjcsImFsZyI6IkExMjhHQ01LVyIsIml2IjoidWZ3ZGx4bUtCaHFrRDFRWiJ9.G2r4ne76fL2CsnNRWBuB7A._BVJ239cNg-71pgb.BDQ5bpZYfY8cirtHcE0CyECju492sU48rvxxd3mxFoFCZYVpobexMlg45MixvbcPlswW0vm-VV_Zd7-ZgrKHsh0zEmIHUIYRw3TKW-Lq_dE1-CQTH7xXT-yqMKA_PRy-laUlMqu7I1Pf9tTpBU1x6dqnZ3wjm7Lqc8XezTwzS_sw6-Z8o7pIymIyGDF0D5IyNYeUEXZTszkcZlWfgweMs-XpH0lklGLgxbOIqHbVnGHMvCZYfyF_ToXjr0Z1b_qapvLTPQQ3NVrd9XKMPzITWxvCEAOEd0PrjZrInml-C02RugM_QvvWleoLnihJHrOMW_AXrY5g4MbxOgE_cLf5TkksxAO6dtL-a9xiK0WjfoGrhcexFXzrI1EtSCx79XWdS32i_sWIqrRK_Hl6iiBiMjux4-s8Fdw6a7KqyZx1K4gWjXDubNEOtpgmmOaK-DrldNuwi2K60Dq52Zu3Y0eZsapgqxX3rv-nY4NhAhb4HfyVHXhxTC9DN3FxYxsy4tIs-912c3HtnP2-4UdqC3rgdGzvFRMCZ86oHveGqcaSUNxrxpm5Df8NtryRoBJfNOsZAfWaemWAlHX4ZMpwqTL4FZHOCXM08Ko23ITWhgS8dRckYdNSsT_ZAEJRxBkVjUbpMfAOLyHLPuIhibXpxrNg6FrGaQ06uiS9YeI9fg1DFqa71QbznQs0d3UXrDFirwT3x6UJaUSJ4_UtMmIUt6ZSRO7NQ3GsbUdttR9QCqFo9rn1OXKO-F1PtA-PteChk7kN4Uum3n88VRoW_btrZpLHOTuxccDROmreeq-olUMQ6K0zeq-oOk2LgRZRfX8CWcepoPqMdu8jJcMoUGdipoNwNqiB5fonM_vGp3hqr2Sz9vl4atELhfg3rmwLy4wmfV5FwmuLa8ma6FsI_cZcLiXRXHXBTYcrkfrHmENgb93uQQHNHNPi4PPK42equGJotmkLnsQfkQu8OHSG8-tLudJmW1ozeE74h15Azf9qQ_Blcg.WUvxrZxxnyoHRQ-jZZsCog"
            }
        }
    }
}
