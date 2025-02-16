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
            version = "0.8.6"
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
                    "eyJ2ZXIiOjEsImlzdSI6MTczOTcyNzQ2MCwiZW5jIjoiQTEyOEdDTSIsInRhZyI6Ii03NkJqRnhlLTVHVDhaUExxb0FsdUEiLCJleHAiOjE3Mzk3NzA2NjAsImFsZyI6IkExMjhHQ01LVyIsIml2Ijoiai1ZdGhxdDF2Z29PVU9hTiJ9.XFmcMMlMGtKVMGIeoUITPQ.CztMgDBkcsGMqkz3.my30kmneorOyKPRiXR6htJwaT5GdAm09IGwJD1mJIHDE56ViWQNSHL1bYT_mlXw4BLkO8MhM1Nk5EXrTcXoZRp8kYNtxZa5-4o2qKIxDNx6bsJmLoDQa2o29-YlkEicQuq1ciN8vZV6H1IkhnBf8kxNon6g8PwIdNKJ1DecDYqswDr5OM_jcFVQ77Y3Q-BcUKPhXgUnbT4Z0HGm6gnsZgyos-4T7MHZ-zhS_Fuilw9uRoNbccs1WaKaxhBLHFEocx2AMZtE3gdUvCuVF7y3CjB0VsEqCKA7wXzuKnnpaEr2UQU_zXYUXSUkaQ0eZRHI92DMVo7Z1rpNpZSVHBvV0jmVVVT2jn6JCcYNj0TwQbwSQVAKPxJt3heiGY0NZRV7WjRtxpihThYffP_ZDeCaJ6e3fRffPX_VdAavM7wu1d-MmAw5TdQ8Y9P18jhCN2ULFCLyrlVMALEG_qxv_ikK2OvG6Fpju31q8up3tRHcz5MZxRlGxhpVZD6gVbFRE0fvYUQU4dR41V9b7EQsbskQYDnjugayt6xntmlFm-hZni0FdBLmRQIWLDZ9nSmYvqqpPPlxSENN6g_pmRHhZItoiZCxpm6D1HKz2ej0K5q4ycBuwxsBmS-IqjLzXgNjTLEp1RCA0Y4yDrq-Hz_iJbPBYl15wjGK9Fug37rNTO8tRuFJ-zHnOYq92_-mbGNVygwxjtt6IBgFp8ASjdu3Y_8h1nI9SWkXk5X6rxBcp-ncX_PUIf82tkA1SY2ulKcxDYScqhPJ3bd5RbIwzHNusMUfVTvMtzDfUeB1Bw-gn3BT4yC4gh4jYoMbUscuc7tJWr-E5LV5l7BCw8WdC-ryceNo8Ta83ei4xmcQGuYMHtP7Utknyj1sGBvS6TdS_kuat9bOs1vm3fq6dvFmhm29VGY0BtAbwaM8_6QNSiC91vA5uE7XPuXVvMi2LbayFi0hvRFgIoCBisf3OrKqzmGyGpcM.xhSVLEkalnq3CEiMazK3hQ"
            }
        }
    }
}
