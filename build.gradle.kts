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
            version = "0.9.2"
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
                    "eyJ2ZXIiOjEsImlzdSI6MTc0MDA3MTkxMiwiZW5jIjoiQTEyOEdDTSIsInRhZyI6IndHRzZDN2taak9BZlBydEhfNkdYQ2ciLCJleHAiOjE3NDAxMTUxMTIsImFsZyI6IkExMjhHQ01LVyIsIml2IjoidHM1QkVSYzNWd1FhVTBkNyJ9.G7pGA4PF1kCl7qMUenbSUQ.CQ-LLOllg9VIcXPQ.PUtZ8VXseKFdm67bWq6Rrl4G2yPcrvpq4fWpbfZBxOlihE69uullDAuWEb5dShHp8DqOsdbNbju8g9d3tv6YoB_zBiX_Zb4NrE_-0stkrek7ZcpBuLSdN6vuPk6pZmIVeYz0DpKOLhyGIPr1A4HRVPbOwPsLMmpFzGqDjgSrW2FSIsbHXbSiQ9XBgrJm82-1jQfrqQBjMucsCl5qMabShtacsijA-vEiFYC06s2IJ4egfgeJJInQqq06K7fLv8pTZCgVgflwRiCcTZNhS250khRqAnwQjKwzwH2tNpq_GgcP_Q7kxRoJNboF1GVdNKUcCHS01ZYfEzjkt6o-HHLdJItltkxgK6MVprLd2rtT2Au18hatDNwT6QSu3idqW6UydifC1cI7gReu9l4ioxsq8VdYLTMnJQQS4N7XdItGhrHhEPDRo5iu37rve29Mjrr5QRXu5vCK9bNXCg7JGXHoxkvxoZcVjZVn4jJWSxIotrEsSEgnQi2BSaMFQicw0luLvBpJ-CoyVS6MyvignOo_BkdFWN5HjzgTTg0RS446oWuDN4dTJc58nzVsKq3kk71MgoMIPbShuhJMcovhR4XGNZ2mKArLxKJDUF5DdGNCh3iFftPmS5mXgtc3-AN6v6Q8uBIAf9tz8r_XyeSrWUkg3uJokhhKoKFzo8veNC-IVViHlVsI7qxhv_WKURbao0YKIGoBWG7if6vxsKcMzQYMpX4I2lMMN3XMiWyGbYm5MyZFPqCpVfFj7b6Zcw3P5hvL4F7icAZrzRBcY96-d8HBt-YsqmHrjxe2uWH2oopTdjOrayz1L0Zu4nvVw7HvxbcqZ1bTK8M53V-ARxU9xVtl0362_HEFQ6ldeXgW3koiYV-Ty4h17CLcxyQXP9gWuSYH6Ao0p0ua69gCwB1FM5FDf_Z5l3yOP8q8hcCino6YzcxorytiWCh-sLYUX_WadwOqRIRfiHmEMaqa5JgbXVgkYQtQfa_4e4cLRTL0YhH3_zCVsxou.sZPoq5Gke28y2pfqZX_N8A"
            }
        }
    }
}
