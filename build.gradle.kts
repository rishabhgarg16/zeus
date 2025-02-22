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
            version = "0.9.5"
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
                    "eyJ2ZXIiOjEsImlzdSI6MTc0MDIzMDM1NCwiZW5jIjoiQTEyOEdDTSIsInRhZyI6IlJSR1h1R0VWYkxpQzJHc2tjYV9GY2ciLCJleHAiOjE3NDAyNzM1NTQsImFsZyI6IkExMjhHQ01LVyIsIml2IjoiaUtLWU5mZzhzM1RSQUFNRiJ9.Tjk4ULV-FEkifokvJI3QXA.P0IlV9RR3IqBYqF0.uw8xsJr7n6mClzQhIPo4funNvAkcZWxzsKtREXQfax_ZUcjwqHirNnoOZ5R7S26PQqdvPu9CLwZXgQs7fMdl4DBxY9uSvjhlSAFuUeofUlT1Q6sXw6xwjNnj0mi3npNnGBXuiZB4myJ8UJPuQYU56nRGJ1_cwC_dR_VqiaIJJNtaQ0eYf5qf87MJwo2ciUXDieCM50V_VMAbw2hZPnnzCbwfoERoBFlRbjEfwBqYLjBEE5ywW9upk_b2nPPLNsQkK9YJTqidaH0rGln8wdR_57-9KswSG8kU0pCMs1w9VwUajX0Ge_2l4HnygPtYrAGEgvXzf6RUDwEFtLDCILwAmWr_6qo1Y3l7sYjuVmSa7WtGb_tV1lxWkE0dR26B66V9g-KiNPc1YpRwe0ql9Z4p-4APlrDrFAHhzPsWmNSWrECsItGe1ZpJD1ujkEc3OqtqSrXOMDwzM9V9rLDyNv4fjnJ_AUhc9giKId86pWI8IVK_c6fK_nJQXT4o8OvpIg_qHqiMoqU1xaQ6SuwzRA6xGP2BRm1qMB-9fFitZTvsxDA8YsoXL1x5W7ycdkgRzvpvEG1Db0WvPNpOhSuRllgPmdm4Fl820gt1iqEXPppqg4Y6rSvcHxdwiUD7cJJclMuTlVvKzaeHjsV4MtK00FE-tehC_9HnwlNyOkHJCF0LEm-IWRt6eOzFr3SKiw-_BVMYbLuKER5xNH_EsgFtYUubvh1pegTT7HahTRHYBMbYhjofrNvSsbnPloL1HUlK0_pGKpcR4HupwNMHJ4LOrVTCYVjJRH1gkieAJh7COaSr60JYIjw1VeFH4idIi8vrEn7bOYDgztYhlZVtQA6URCgKiyZlxOWoODKXH8IWmawuqzidwZ_CZj_7BaSsmlkUXIwQbdKjfONWybw4RMcEWeuiCDlFbqL2BlI6aX7WBhVsvYwV_pGbcdnVlh7KA_RGsfxB24AzF0sMYAqmSzc3BsrlfH0PgsCtsa-2xAWQNKqAZ90AQ16xLZEbIg.BMlMg28UCpd_rFvZiU8PYw"
            }
        }
    }
}
