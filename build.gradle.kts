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
            version = "0.8.15"
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
                    "eyJ2ZXIiOjEsImlzdSI6MTczOTk4NTgzMCwiZW5jIjoiQTEyOEdDTSIsInRhZyI6Im5VQVEzeHpCNG5lelpqdHBBMWVHQ3ciLCJleHAiOjE3NDAwMjkwMzAsImFsZyI6IkExMjhHQ01LVyIsIml2IjoienZocm9Nam1PLXkxN05BdSJ9.FbA1iQe7ivc2oaA8TviaVg.OBuib6hQ8uCQ2AXa.4Fd24wO7Lh9jDb70pqYVRUS-KQRDfFb8uqhQqZ-7pRfh_DZwgslMUyq9Rb8S-LZOMG7LXzoJR5ptuz9NEIjg9eOVSshQN3_aOjaX1qrI6oLxFq78YQPIqps56nu6phSlZRgZtuwHUE022Ijtna2Nd-YvHtk2kmHLQb6ptxs72W13L3Dm25fL8_YFj7gL8y_f8NSzC3b8cxzPaSuJU7QT2Kr0jJSZjbO1D1-1hIUfOP6ceWZ38fpWokxVTH1_ZABjQGOiGiniu5RF2Aj8iWL2X-76ag_MpKtQLZf6s-dp1L0Bz9l_TfHhQW_cxSh2WRPmHC4VWIFoIDKX7aIjiGc_qQCqXv3HCQ73HLtsBxgiMax9uUAEdeT4V56SiQsxn95gQJmhmDc7c-qKCumfia90Loa5oQ_7NvKVculKTm9YUi_7VI2w_K7spQHV96F1mIohKGPep7qJjjEHl9P2OZu1s5loemOUNgK6wEqgbyRkN4Ym4k-LoX_MuV56_Q8ddUKIemfO7gKUOC7Ql8OzHOMb1s3qLs6wlgBzOpBseMmNDcfQk1qv1-7f_B5Y-COnHcA8bdPZp0J-J33WBYrPntOMJFWfU3vNzCQffCV2tFRnblLEjpheIn1iIF5MLTvlTzrrmzLfx2gIS8kPCRWmTA2zvwm3TQSTli_PAlnbmsGH3EvgyQfdxbO1QMXWkvKJ8eupvo7FVZyRhi8P58iYb22YUb_ELLoPbVhMcJzo-0kON73FPN4xLzXsNdksCx5VP8sLyH8ObTC57INNwkY8NyqbpTGtL6DXvkEmBZyJ7hRzCRUJRO-PP3QCMRB1SY-FNQQUWgnMwogKkROmpOt10I9FLCYH7gaYMWbsHWqaRaLXK0GVXIKPtyy9LHPq1u7NdFyQaDCNl9bcfIr-297ZpMdxSSltpbXF2Hd-kbfAkY8v-9fQLfU1M48PN9zM7cG2gOTIBsz5U7-0-0RkbWpCUqXtjAPf8YDyPZIOs9XcoWKKlRwfew.0T1NjzApjx1_a_vWqpvDOg"
            }
        }
    }
}
