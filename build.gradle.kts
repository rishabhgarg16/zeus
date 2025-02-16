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
            version = "0.8.5"
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
                    "eyJ2ZXIiOjEsImlzdSI6MTczOTY5Njg3OSwiZW5jIjoiQTEyOEdDTSIsInRhZyI6IkxXUU44ZDlRbUlYN3F0b1BrWWR4OWciLCJleHAiOjE3Mzk3NDAwNzksImFsZyI6IkExMjhHQ01LVyIsIml2IjoiTDVUb09VN2RYdTV2ZjNvbSJ9.J4Sv4mx6CyHk7-x_wYgxew.1XS-PQHqyRbKk46b.dLoS0vAXas2zrVBPgXrTL_TkCFkGmI6BuIYfV3V20M7RndBPeb9h0m1G7qcOEfzpE9DJE1egWpeHmCJUkJMxo2t-1djkDU420PQ_Chv55W48Rrh1azfH30WYAznPkq7wknurHkbf-lrHf7Em_A6JgWw8vTUyoD4oyyI9lMa2hgLdZJ2RBs0vmueoZ7eBAYkHSOysbn7io_88wzXTFX1eqTb1vYU92V61_J38dOf9dt1rRh911Km9wDOxFzRcPbv4DYmqe0AuPo3I0mYbUv-TATaY5SC2VrobW8Y6-22x1afG89-MEkmzZu3I_KI9riubR1sc8H4DzaRDWiwbTIZ_yYMKGaqY9U7_OUFqzLVVWMrtecUMRTOoj3Mt9UydQp3jpVu77xrtxgUWlE1qcAMyhTBxdPk7yW6xcMSmhoW1Sz-TiTpuaadf3pW8XNuQ8Oa9rwuh06ULltMLIQte6vfv5W4CflnhV4KG_eahGLObDnGxftT-9XfrXIN-MqrvTwdtptptsV9G1X1NnVAh0WXSGs-90iGeFAojc-1Ct-elttO6VL9w-qeAqScwS329sU5Rk6oCYHbrOZ7J0JA7Pgtat3oD2Bdw72IZulctaXL5DAjfHwhk-KXUKNXbZyN4T4ZQ6X4ayuRzqNx3BEGXiOgBP0GxI97GqnQLW2B39Yz8W18n5eL6Nzy3hNTvr4CE8VHzGa6Qrr2arx1em1mSeB-zVfEvVboeVcjjMMUq2fLSUf2wntNV0q3qWzoeclKgZjGuwg9MVs4ijQCwBuKWGdIe9L5LY0_yFy9l3VnY5WBNg6dKWjJB2GquFEdIhJaXOKDzLm5EuyYzwbCeUXWuNmN784Jaivdhe198tq2Svy3A8niaCtlUkYEioEhgtorRtwf28_cxDzACZY8cdIczcSHwOK8qUiSyTJALP7yQskHFBfhUKRjlE5E11-z5s7qal82G4t5aEuQet2ZhOOzE.lGHyvrCBo6J23AhZYBGkuQ"
            }
        }
    }
}
