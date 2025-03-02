plugins {
    kotlin("jvm") version "1.7.22"
    id("org.springframework.boot") version "2.7.16"
    id("io.spring.dependency-management") version "1.1.5"
    kotlin("plugin.spring") version "1.7.22"
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
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("javax.validation:validation-api:2.0.1.Final")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    implementation("com.mysql:mysql-connector-j:8.0.33")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework:spring-messaging")
    implementation("com.google.firebase:firebase-admin:8.1.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // Kotlin dependencies - make sure these are explicitly included
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.7.22")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.7.22")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.7.22")

    // Update coroutines to match Kotlin 1.7.22
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("redis.clients:jedis")
    implementation("com.auth0:java-jwt:4.4.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation(kotlin("script-runtime"))
    //  AWS SDK v2 dependencies
    implementation(platform("software.amazon.awssdk:bom:2.21.42"))
    implementation("software.amazon.awssdk:sqs")

    implementation("org.antlr:antlr4-runtime:4.9.3")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
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
            version = "0.9.12"
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
                    "eyJ2ZXIiOjEsImlzdSI6MTc0MDk1NjgzOSwiZW5jIjoiQTEyOEdDTSIsInRhZyI6Ik5oMXgwQmRJVV9yQjNPOC1GdGdrM2ciLCJleHAiOjE3NDEwMDAwMzksImFsZyI6IkExMjhHQ01LVyIsIml2Ijoib0lvdnlCa05RWWVSMXc3XyJ9.o5rgdG4TVKZuA5wgUCh3sg.CNPGLCuRSxLl8NPF.5a3_uG9mXalulS5mU9zgv6YYDmfyNSCWoomfiEUMa3rUnHRRcLnf12cXHseyPxealUvOzKR3m8rD7bc7n5tAUsUbaK5mPuTP3GFEDTJvwCuTB1yuSQ8guABKWRhWHpH88RnrFzIkJDK6iA0dIFnmLzFXM29EVjbfHqg_aKxdZEXp75m-8KoPK192N6zo1P1NBxrX2WtGGOiExHTzi4MyeIFZgt6etVNCMg71gfwhc1hIeSCKwBFDkOMlUYDA8PU5b7s1YGpwWws6akz_dd6F6_IzPanUG5MQEZs9gNIMJ_TUVMulHJW_p35XwQ5PUe_tK4wI9oq0LmAjxjclK4gaQxvO6ue443_WbfLSLHJLQH50r38NRjzunkvNw4xzaPo8qfr0aGsDMRPl_jTvYBVT1LfBG2jVgslSANSH-dd0KnUjUp6pG-6Y-tfS3oWeeL_sueGFBVi0fm3vbpzkizcD_8-cTplz2uWdrilH93S3nU4DnLVygbCL9w34a9geGKye3FnYCY_Y3rUq3C2NoSgcIBSQgCkrazIepuSrF05iaxziC9pOQGeEGdsRONP48lCXS5Q0qYv9UV_mU1QVSZ4nyCqD7Z8_HtOFlJP3T4o_AHOEoxDn0VAj7CHWtDoJZO_CVSWp7kWzVRFqWpn9rMouE25T75eGPW6ZH8JSmZLigdQpfRno8dc0O-OoAtqsiWi8ftE6c5zmw-8AbhXk5UHxvo9cTqSPnN-6CD6utXbWqc2wT-08XwvdyVApGKpDmumDelZJ8mqivFker9Ndu9EGYxAMkDVf5UlD1ARLT0moJb2rAYUly3F894XImNYYZIXZRVJOnbEpWm58vhoCiVEkUFUHAFPy28l9uuNVMTZ3tuvRTPwx8F1jAA1Xf9TXqXyDwtq6arHFt0FMxiHHhUCyFkGXPCfDSi-mj8OPBewcUKhC5glJYpsMS22ZFPJgE3J3leW6h5L9GhpLb-pJlgrcdQJudxkgibBRFwWFuBDAMEQjGppeZOLhhO9N.K0k-uf_yYxQ0fFR7nS7VXw"
            }
        }
    }
}
