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
            version = "0.7.8"
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
                    "eyJ2ZXIiOjEsImlzdSI6MTczOTU1MDc5MywiZW5jIjoiQTEyOEdDTSIsInRhZyI6ImZxQzZ2LVJvcGFheFpNY3RROGpSdFEiLCJleHAiOjE3Mzk1OTM5OTMsImFsZyI6IkExMjhHQ01LVyIsIml2IjoiNnlCcWE0MTBmRTctOW03bCJ9.aTZ6K2dQ_SqPr9_-MKI9kw.H6waQO6VRhkFK6IW.qNfq-Zr_eof9ounPeZzlHgCVvfS4p_XRElgJ4kizBcol_2WtX5b8Da7QbAjVYYrXOfsC_os162C7CTfNajax28pNOf8uWa5o9d1dvAGC5A8dBtWu_U-m8ffQbRgXnPgQ3PWAVq_G3wRmPaPA2Xpx6FLPqtwTO9wfl-YFR0nIyQGmSFToT2CIFsfGlTue30cVAhNjlgVC7mrmAutQAO8t8XMgLzQlDcO-hOncOa6RWuoIGTjYrWShl8zfgRxkymwo_pGPE7of7es-KLVFr9pEplyEUcxZe3FM9JB1fW0Op0SzQyUSy7NZZH1hGIEDWcGKD9Dx1HnLLbLOMwPQvQmj2Ivqf98fkH-Y5oqO8tMQPV7zUYeJ-w563CJQrt1evXd56UVKhnmk36J7qw72Oz1QptvHMLJUcStIT9ISN7UMOIgJ4QXMcLOmWU3R3hhhc9cnbsCIYeHrtYLGurzRGXzcDamC2Tida8-CoE0kGPFO4VesjxGTNKgCtLK4U69s0Ad9_n0FbyDxfVptt42y4_ePo7BX65KMr6QoGPSTrQZYKZhPUy6_DDIDz4pu0CtJabaR_DY-0TOOnR8zO9Zhm50kHLxja_HIhGgoYLhE1DVJ334w9nDvi_tceG3SELjW8cMNq5JODOY2E9YHODAiIrPQ2IhWdzyU5GYNQScOVxC9ahTEbUCu82UDqyqO4CINaE9I-6acjqgHSYzzJNwhBf9eB6saEjJcwABaaQBUrE7UrZY9_lhdMzk0VPDZXrwPhWIuCcyFeK1WqnW4yvvmqL1Zv_fzRAjprEU_WrdJiDVwhJtAvW-4S7etTeY_SJ0GUrlDnkAogKVjgep_TUdfe62hSXbdBZsQbbV0mvaMwQGtBomSt5V8il968HXvUYgYb-E-Y2sXdIIiiT3H0DIs5eWZ22WywPT7G3h8ZbRqwL8fYTmCFQo4NFAq6nCQN77DWvceJMxQgQGM7Q4XlhwZEyDGCg.7r7_KkeAe9xyuLuf4gUVOA"
            }
        }
    }
}
