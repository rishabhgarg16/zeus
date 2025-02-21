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
            version = "0.9.3"
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
                    "eyJ2ZXIiOjEsImlzdSI6MTc0MDE0MzkzNSwiZW5jIjoiQTEyOEdDTSIsInRhZyI6IjdaaW9MTDBEaEhvcG1qVmNFaUFROEEiLCJleHAiOjE3NDAxODcxMzUsImFsZyI6IkExMjhHQ01LVyIsIml2IjoiYVZiTkoxTmxoSTBCQnc1RCJ9.S33i8rlYQx9_RePMfXOh2Q.sFJykOI6EtXzOpic.L-6ji340XJ5RRi43iiDn0lF2H1o1Va6C5zwUjsm1AiRtVTN22k4v5chryk9JT8JJxfEDSZkUKTEAXPxbgCMSeBTU0k5vNbRnc41N2hyaiMDX0kD0WWH4BrFyuq0AJpCJic4Y7zBL0b9V5sNslwE6kfaWRhBQWFWwj7NAuJJIhyipoXLcpFQEGLCWmGKjnwerLsUNcoDNk7sWmiSMqzekKXQo9E_ygQMXneYlU4NFboTVFokVXRBkU3QN5wiUp5z-ns5B2N8p9_ZHqrlJn74pEJYW8Z0OmZYaXj7bWgo89Pa-bEi50tOrF5s7Fn_VAVaOYVln7Q3eXQsF0l7JZGXx9JkKqq-VDkizPVIKaGSkivylQ7XQ8O5k4AmfHRwT5KPH6ZLqs9JhwjRpalN4RB-1NwjhzTVoyHGZs9Pt4KqnFw3cgEzRde-BAXCGUmp-xK1xNex13oxJnpP9OFKePsXsx3ZqiVr6zqxB95-LDZhGVsF7mXPy2mCo9QVxW74dASurHiFHG1PqyMUlBExwj2WUyLzRnxxaEJdOcdaxPFae03Z99zOhv20fMpKXtqv2YVvXNuubJvwI5_BUbh4yu2FfbI3Oh5pfwKfR3FaXzAq81ydPQ0z5pYZi8cG03XSxIva9EnsJ-lbp5vYl6k-Lv8j4YJTyN-LgKKHVlZG5szxZwZS1RKx2z7e3ChHU90mYk1CXWkemQQwEXq1opvs1XPKYdJkOySnrq5hxhm-rBBb8FOgSHo9JIwwP-dck4HaXnhdfyLOmGJBatIZfWNygl1V9BI0w1gBmvnznELLcO9g63J7K6HWv9b7DvW4NgAxUvnNVaAWEoSarfTD8AOtTCKdPSpjNMxrJ7C2XV-y4kOFgCQSaUKoELOyvbciHEXrW2Aft4tcMhifcho-VNtY8m-djnp35HXRiWtLO4vfSkEGHHdafSTnAP6XIoKyNErGdarxFE2_X-w_h528D0DfgWW3DUwDyYUv76JgsgDNBNSiuniM_WuJGEjHMsUBbLg.vPTjJ6FuTSsWJQZ8yO3y1Q"
            }
        }
    }
}
