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
            version = "0.9.13"
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
                    "eyJ2ZXIiOjEsImlzdSI6MTc0MDk1ODQzMywiZW5jIjoiQTEyOEdDTSIsInRhZyI6IjNxY3VJaVZhblFhY2pfNmFwWkZIZUEiLCJleHAiOjE3NDEwMDE2MzMsImFsZyI6IkExMjhHQ01LVyIsIml2IjoiN2hxQk5vNjQ3T3g0bkxBTCJ9.6XiCPtwWHgpgJzf7EiCmeg.9qFHS5AUvUnxVjZF.n5XnhJix1NXk427uMfcfEbd3EZaV9MgeXlbphNl2urC2XL6E45rdh9uWRemUM5Oliy9jqNrubmCliewFVDCXw9IGD4gFBcRCLNMinPOHXxgxN54-EWJydFIc0FmS9pJxcGSNOuWaJkiTBabcoS6hyw2Fn1bCK0lasGIMPocE0E-UO5S7x9bCIxYdWgKixJF9dvkpynZMfclm96hwKQwLZ_mricuuxhCWTExtc5qa6h65ClR9YgmWnyFVbbvvw6mTb03ywuUVoeau0cxSw9lAs7LvFvA1Vfwvl_zsK4qEORLLoriIS_baS-iowNk5tSBNZS1USauVzOKS50QteG-O0Mznsy7msL96bWJN9P-R4oPkLhKtQ678IOPXDFYorHKNR00UBBeCpUQ6XnvGwRgGcxVeNu9bocIQ7GHiFvAMj8kCmJ6QJEbvR53DNHy4LmBX5ErExEKqfaqRul_ABtADU6-P_tRKwViaqAO8xXOlK8ctpgne4HYYdqTm1AhFfOTwFovODKjzNWWV6imzn3DUa8-p29An5hGll0NaGD5sjvXhyzSWHZR6Lc938BlQh9JUsv9lh4E_i_1SxfqSGwkjnqAlC9vjgecVmWgI010ZagrwdLIZiab21b90bushmhSdtcq0NaVDeUaCUL7bdqmxvXS8KGkDKjMUEcVf-_HyqiMygugC6E23CcJsRGQUcHxblX_5BjM3MesTCJH673TKFwqDEuBdP992v_rU42FdVFUVUmFSED371wOmM7LpRD96OvuG7_5JfTxoZCrZtqIW9JWgQmrinDxCc22TTU4holLknex5UvjYdSnor8wU7Z-LSkAdjuLCvqQPmCa9QZ-sTuQUj_HgMkrteF421k-JF5R30MMc4WUavcWKXn1y_hOERSkCdTvYL-tyJMYMcEtPb8LUm4q7bMAzbAWhRjC43F9bBhqj344n2fHMgB-kCgOYRv0wQUUb-QgAJNJhf3VPqaZt7XfT7-9Gu5IQjSR2taWAyyT6LemM9FEjsg.PdaU4n4aTtlwkIUDCng5fg"
            }
        }
    }
}
