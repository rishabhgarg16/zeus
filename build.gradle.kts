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
            version = "0.7.2"
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
                    "eyJ2ZXIiOjEsImlzdSI6MTczODAxMzgxNSwiZW5jIjoiQTEyOEdDTSIsInRhZyI6Imk0ZGRtTGZVcGFJZ01ZMjRBVnNvSVEiLCJleHAiOjE3MzgwNTcwMTUsImFsZyI6IkExMjhHQ01LVyIsIml2IjoiaXl6QXdWa0JYU1dxS0NMQiJ9.DQh_UFMgQfPTIG6_kCvPtQ.oLyz5q0nOboHzJKm.lR2Y9EZzUmtv4qT7q978Kfq4B_cQgZz0jZlQbnlIMihkfELwPdvbiWEgPY7PeaTTRe0RGSdovVt8dg3zL7cXGLf_nizwE5nKp8oGHjV5XEhn3fvZhcuIaLz8H7pstjormNjmQWklTSDKg4wxlU6ypzFfTxaolT8ZdR0AmV3dDu6lIZVuVL2nN6oE8ItOk1mIOH4BqGihoYv6Z4ESaZlMyKz-LKcGse9UeWl3zWdQ2DQ1cy_PehD7kw0kFf0nX0pWsfgz2W-w3TjDOu6SyDhH3vAf6fi7OXgYSP6KI7X9If_HSYJqf1SPNpwIeCc2jwWwe7bTruOzDRLCrJ9A3QhsTe3zuk16nTUQoOUWloLcGLD10hEu8FtVVMzxWGcLGc_T1mZ7xOfbWPH4DkQBS2rrcmhm9q_pNAPp8sni_Ws8fS9BU1mhfYenkgQIDJO0ajkQgp-BOy-1Oiiv3sWWLAHp-Ef9FdLGv8iU8FWZ5pDHHpHuyfh-YHH-XksOOvI5DY0Zt0gTlt57bwgLi5Iy1jRT17VgbfJSFi_PXbgKIvthPHtomO0R-RGih58zKLhxfLtOls6IsnLbHnnwsmQZGXmWLaKLz3Y-AacoDGHW_4fcf3sNEac5QD3YKKP7wa057ln_Tj3L1ktTyZ1NVrNbCip_v8Es5ovF8ui6T2dLUVRBpQ_FF2IaCkblfx3DUlYk5HO6kqkJyL0tScNtQXO1zOeF1sCl7qOhNb16Ow3jxQD2JdxboUVKa-9wHcsKrMKw1MLUd9qqq6DUPjyimThb6kdbIHTYBOs6D7u3LbAW8_E6yqtV00nKhCogvmmZuT8Qc6jhIqEw2wDizbbP9gjwJhzxFet3so0k0hImYJ5pVdYxmqwp8iaILzNzegjYUuBc0gt-LE0oPFD8OTGwhzJCgE6XwbU5vNrbNeMv0t9-DP79jbQI9IHsinj_lAzfdaTfu2tFBPfy2pnqxisQNP2rNOUI0nvLhRo._MdVgzXWxbqn33g3Vfaqlg"
            }
        }
    }
}
