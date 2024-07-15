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
    implementation("redis.clients:jedis")
    testImplementation("org.springframework.boot:spring-boot-starter-test:2.7.6")
    implementation(kotlin("script-runtime"))
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
            version = "0.5.1"
        }
    }
    repositories {
        maven {
            url = uri("https://hitcentral-590183692348.d.codeartifact.ap-south-1.amazonaws.com/maven/hitcentral/")
            credentials {
                username = "aws"
                // if getting 401, run
                // aws codeartifact get-authorization-token --domain hitcentral --domain-owner 590183692348 --region ap-south-1 --query authorizationToken --output text
                password = "eyJ2ZXIiOjEsImlzdSI6MTcxOTA2MjgxMCwiZW5jIjoiQTEyOEdDTSIsInRhZyI6Ikw0X3N0eVYzeDNPak1jTklFa09FZXciLCJleHAiOjE3MTkxMDYwMTAsImFsZyI6IkExMjhHQ01LVyIsIml2IjoicWZ0T0tiNVVYVW1SYVJxaSJ9.bEpH-2ck07erA-MLlFtBhA.686LFPa2gAbzA0EX.ShcUqTgRTZ53dHf9WPvWHLCl5cGGu8V3nOzG6S_MLitEMFYOmcuVwShoiiYFmCnX5hzUfoq9l20QZrwdHs427S9L1S3Ahi-xZjGY9U7I_6KD40Q3aOwKsp3BMsmvrg9VOPVzf4gq_WCM1lBFO79qdwsEUxL-FQf0tZVj8MMQbfZcnHRKxwIFbEJpMoFllhhXFy-nCXmLJ165r2Enhz5qA-fCe3zwVYJLOzdlIZPkj_FlaJHqp8oxUViJcu2Ik9Op7R8cCYHr2c0vLmSYq-rQhRva0MfITNU1Zdhlwq_WN7SzSmbW2Xatjp0s_5Nd4i-07ujKd8i-YgwglyE8aZGUVhHtCYBmoft7mIpVoV4VWhqwvXEsq12_7Hq-GFAQGeuO5yvKqEfxzPkaDTHS5kLxd5RV4c9yu46yFQSBIOp0HZ4sAMHrV3vjunij5bbY04NB3WTIwJTM2uXc9BEPtPtFRrwKvCLkyBkp0g3WPJ1tE0OQtjxx8bf3HRFJWFSkTNnmJjJDxY0_NTQIHhvaoeMUmPvGU8vEIYreR97qMw6jy1boVUkbcCyjZGmPQihfr3jV4ayiY0xfoY_6hwKYIHpg53bDJtIbDDPqXIGvOMW5z8jG0ivXOHM0pgj72wUEHh1OkUUuwWFFYqGBIZAy-vxXfNIm-w2GMk9JH4yDTAPKoekkpoKvFqBDVtJcRauuH8yH4D-gYPP1Q4onLqpklVcY58nhVEB8MR674taCGFuBQQkaMBnQQt0FkWgw5Kn62toLl0vkX_g4RYmfuQey_M4aVOaWTehamAPfCAcREPA6xxvZrp0LcP7caKFZ_jqwJgdJy8DyiiqZqQu0bBC_Ht9DxXmvTc5MG2XJ-6miFD-0V_4uWwIxKl6nEMmwRkysS9EECAz3xhz4YXPFfxX7SoT_o2TAhLTv-QIK-PYS3LHis1hCH1X6dbDswVGcg1f3fnbxaKHXHT4J4sqWX4GAAgeT4wZRtrGppQGqn-ltXrPvr-IMYCCKMauktXZBWw.-n0SyC2YXMz43B01Bxsr1Q"
            }
        }
    }
}
