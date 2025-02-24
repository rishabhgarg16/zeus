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
            version = "0.9.8"
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
                    "eyJ2ZXIiOjEsImlzdSI6MTc0MDM5MjI4NSwiZW5jIjoiQTEyOEdDTSIsInRhZyI6IkZqaTJSYURmZzRnb1RxUmJ2WS1xaEEiLCJleHAiOjE3NDA0MzU0ODUsImFsZyI6IkExMjhHQ01LVyIsIml2IjoiN1VfU3ZYbzVyWm1IeWNkViJ9.rBXbRjM4049aTOxFDgaUzA.9Kliq-r_kVvY-lHQ.YTsaRJHolAUMdmuAN9NQCjYX5C0HnZri6EyqlsJxzKrfdxqhbcn9sa9bibDT3WozWA98FIs6Eyd8PIRpWNhWKgPHDMxuxD2AeP0YAOmpqUJkZoTMMEIZVdADXrqGp9PG7AIrnT_w3041TNYSQ5CZsooYRmzn25X9EgzVTlX-zqAxFtlRu6AirLw0sSqQf2hNb_ubQiUtaJIzfe787K_jL0ZHO6VwdT1wdjd0sbjzIwQcaW6j9gXZsJcjJkO6V56H79pxpGxDlLV_3ZTHT8xIQAQBA4XAhjeDEuKqEvEbJ8ypKOdLSJkIJsbakNg5t4RcQk6DP37Gm-IJn68gbbkZ2aGFHoGEW76SXnWa8AGNKyKhJYBhMgwab4D0mb9oMaWHn6e_SlEQIxk_sUm6dzZcbm50uW2NsXiSoXwxqvlyg3V1uZrKF2TupWeid0Wypyj1336If_Ya5gGqtcILaiFPW1n1tESqtdhNOvp4juXyqSEwBNOo4O5yz_vXsDw6WssIKcUd1_z4J_e-6lljAksiWdeUTpGP3xzKeZunNIzHSxqh_WWg7ov8_pE4WPZ3pa5B72o6GhX-SMQs91lB9ieaa82_14ywddyGak3LEiewfBGYn2-VAxjk_x-NCGVP6eLHe0DP-NlCn-WnxGZ7KLCDc1bjoOs9hpIrWDQALPs3faeZLZIFQYCv9mNstxgb0ytSaMeFtTnmLEwarR62t-1M4p_mwQsi43K2yKHr0-B0fzOmfJi7qoD-O20MhMP-PMN3r4XH24rU-Ik7UGIHTY49eRFQi74ba_LYKGYMLgAOwW9PtExmK1nJRfIASGdvN0FoG9W_qdX-SrGAhtUEWIrcYQGgzXEwDXbzbcvMY3aiv5avV4UMum4YVB-NF66jsr4eEKXWX0t56Kp3Zjh_vJNKhonPtgsqisKlUy24m03C3Ln9m5BCbvJMY652qtkOo8r6PXcO9xf-cKI_6v8wXNEIjAeEwg.bfJuOUscbM9rBafHjykcJw"
            }
        }
    }
}
