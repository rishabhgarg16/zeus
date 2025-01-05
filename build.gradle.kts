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
            version = "0.6.11"
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
                    "eyJ2ZXIiOjEsImlzdSI6MTczNjA4MDc2NywiZW5jIjoiQTEyOEdDTSIsInRhZyI6IkF0Rk5PWFJLNHZtajBsTUp1MkJSQnciLCJleHAiOjE3MzYxMjM5NjcsImFsZyI6IkExMjhHQ01LVyIsIml2IjoiS0paYW54eVgwMGdjalZmdiJ9.4SiBADeaXsGwM59oxpNzIQ.CVRsKo_5SC2plJg7.jQeyBcdo6VRaGpim2s3ZzqKM9Ug-SsEBc2p0ZaT0UGeV7O_iQwawMJugvSpmpTExoZXhZDNVJTfgYB6aG4cZP0Y3_2j6mscq8iiDDxp87JndCh25KmA2QdwWrz3ITOlt15xIe9G7cxzNIWeG1X4Rwv_khv89i2yP5W96fDURHb2N8EN-yA6YpBo0XsWAirmt40N4DLMGhfMztl35k7WqULNOWpIyh6pqh4CYSd7aLCnDlHRRKhNS_ASEm_QfDTPqgv-lu_8rdlsLskebV8xDmJVdZYhJ90MRo5djd69ajZHaU1zqeHygaNFcbkmMNxhGkHQcT9lD4yE3W2M1S4fEHXj-xjTLw9dJzioPTMR4QznyKd2TkI9iLIBP4LZw8CZ2uPoWkelgMYRl0JL3iWYuFxPEtRzCMAUpJgMmN4h9PHfZz-Y-6aRHVnFSDMd6KA1TTsm1iG7okorVvrRMn9ByCuMYKr8cL65m8fYBzMrq_LChvWUa54BKpDWedgBN40i9dwdofTS3SbQ8piqXlRsqc6gxN-jJrFJaii36_KyKQTrz_yoZK_JhR-d6tcXM8RYDB_fVr7xioxZFS3y-WKIfzFTMsj89PQrtlXEraXTrt2QW2_u4uFWmLF4vaNjtmGkWYux4W9alpf0DXR2J3pDZ_xLj1SbxJVx_p35oRdd7FkswJNRCGsl-Xrs5uM6XJlyYGNeWa6QAyhkxCab2EbtyRT3BtXFiU03_sZswQRLR7gAgWHp5sYkrRBGv8U9Jern22afzPu__GGT9DgivcCOXQk91C7aijC_nPavXV0mgCm7sFnaCyothB9Jdik_2VPTfnzn4inLLT9id6zGiffj_CAql-FqgG7T940Vw-DKjuivVif_2o3LbopkzpukQJPFuUB06BgbyTcjUwvgmRC6rCX9rX8hcwhCIqqFxLi6AP9wBNhOUnBK0GPrbl25u094YvJ5ZLmkPTEyjslK5HfqQrg.E3X6SCZxastOY9CS1_tqMg"
            }
        }
    }
}
