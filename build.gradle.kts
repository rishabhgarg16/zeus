plugins {
    kotlin("jvm") version "1.9.23"
        id("org.springframework.boot") version "2.7.6"
}

group = "com.hit11"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter:2.7.6")
    testImplementation("org.springframework.boot:spring-boot-starter-test:2.7.6")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(8)
}