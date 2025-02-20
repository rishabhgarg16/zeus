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
            version = "0.8.17"
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
                    "eyJ2ZXIiOjEsImlzdSI6MTc0MDA0NTA2NiwiZW5jIjoiQTEyOEdDTSIsInRhZyI6Ik1LWUwxMGtJeDJYSFVtOGFCUjZPMVEiLCJleHAiOjE3NDAwODgyNjYsImFsZyI6IkExMjhHQ01LVyIsIml2IjoiclVxWDNsMDhHWWlGZERZbCJ9.i1hWHT6KuTJAGf2Rzsibig.Yk05jQGHxLrcG5Vt.eeGahOGHJacGfBVzGj2lLnIByGCYC9TiPKtoKP0ht8JnLGTp8DluOFcEHORSE5UjMfLfkWGGT4cLl9b76s1iUB9L8zcCMI2HobZU585mS8M1CPGIgtEQYOKrmd0BAOoRNpEvqTW9OnIjHPXSQeOGJcwaTHPEf7MkXWci0Ex9gwKTBBz2hNjCmpAhMnTtgnHkgHQbwpiE2Y9L5s1Hh9znE7lDB0ttfIzcLdGZrsdx9SpOWx4U2M54dn4E45DPgfwsaF5J1mgxSvlxDZP5yXcRIowGOM1WxzlXDVJl2BNOZzoc5VZ56pPx0xwB8J_cHhDp-IBc6WoNvY52HopFW29xa3Ifa1gswZxX8ZhuRFtC-ed34xavCV8BbuP6DPcNe9hmnFdyKFx3Cf0FA2YZVypi7bdcc0XfoIySGk4YReJFfcfe6vfMUqv8CUrWLOsqJ1r1J5g-YFWJr6KQgdypaIfHRGZsjbqBdpOjQXMxA6gf3gHPO5razUUazZZf2OyOqZverpuYxlbFV5idSMybv4A0TZEzpjYs3nK64wmRhKHSNzZhWaBgPYPqU2KnXklkpbAKEdExXYf5dEZT-b1BNC_oqgSb4zmcOHLG-Y6IKiFknIogwFCk1PoIgDJjFYiVEbzGraOLOodoYQFfBqkyhXLuooXLPFGuRD45vLvHp-X3IURS_OBFS9ccwjQAQjAvKRwaL4UMl8b8iOWVjGktgur8KySCuXgWI82EzfIrT5Wtokc6hMP1nFolsYibf9BA5tUUGrO-QLPYSFaAZCBwUoJTNF7_NY9dF2C8LG-lUJ2igXpRGGhHS6B7uANsCkZ_GLZ01ReNbCg2ly8r7Znmn8fcI5UoQ8kl7vS0uHgzopuAnBS9aOg-JqRtg6llyC_JQN_y_L7DJ17vKDQG4-SHovOr8ysi_Pp0bNoy6ZW4riVOa3wQ-FfseVdVB9fUxrmfbz_SmaUxWrsWDz2Q9Iuvb2slJoDRACDWYo7St3Hsmh0LNS_7.Phf1y8WdvmlWqGfWrIuweg"
            }
        }
    }
}
