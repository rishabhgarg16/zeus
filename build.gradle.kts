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
            version = "0.9.6"
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
                    "eyJ2ZXIiOjEsImlzdSI6MTc0MDMzNDY4MSwiZW5jIjoiQTEyOEdDTSIsInRhZyI6IkZvRTFRR1liZUZQZ08wZ09OQTlQcUEiLCJleHAiOjE3NDAzNzc4ODEsImFsZyI6IkExMjhHQ01LVyIsIml2IjoiMDlJUFZvNU5ydzJhd0VudyJ9.7_53GA5JgzRSNQHNWKACvA.d1pYEqthltOz6IAP.EnRgLBdzGxB6L6IwGdk2nHvpREkvloxxSy-GRzT3Iiy7DsvNBlXy9jyXs2u7xwcllxgWpHodsM9oWHylyIDhE6D8t2KtIm08v3N6DhwAn12433WqU54bWDFRrecoXNMor0oTUD2K70Gg0JyhDaCPkRlwBsoeeKv_1vNftUq9pcxe8Q8KxSC_-BuslO4X6K7vSyVc5Zm-uxOoIj0OkepfEalvXv5hs51GWMOeGYjFB6uxz-Iy0bSAM_jyiflvIN5ivfv5tOD-WOxnA3sOP_0Tq0vGR49kv50BQTrHw-rOejMubDfyl79xMRtrrE1oXiZbloL7ea0QtgEsZQND52424Cdm_XuYgcF9rLj_3ARKGikYmQ8pKutVDMzWDF0GEth_-HUvlQNam3N1FKPrhh1fdlh8INyoaQbRxN79I1VqX85kFNFvhy0S9O32vWGfsP7DEErmcGs15nLxJeJ6P_Ya77hOMA_-RxugdUeeWeBCB1_RDBzUAdEdPsGE3gJEwAE_aQNAb7wkisX1DRSMP-SiylluieyCMHdPYENiiMFcQ07Q969xekkmwfmP0eNBdOF7JA5t6L2Y3LZ2qf3JlCBcDhgPgZNA6x93WC7osjoVTifyhJ8g1Kv6N6p1sT6YkSg4yTehlBCHuGNzZatb6MmztfyH2aDoRkUQo5ku5YbQJbzVWuLO38WVThCWBrvge9uhvCqIyhpqpOJXvke7imxnCf2DPA9nTUWjwTBr8JL6MefMRRZqWiFZHkG3EThOL9OHDr5LYuxmc85lvxLWgn9bLr2qI8_eF4mdhUEwckd5nY_qjSZ8T79bi7N_siQKy6cbBpbvBS8XGsq9ACVwLb1EOK9kPDJvcmFPkJAT0g2xmwkGaa3D-UmiSVkJtUhN_1efilJA73EKGnPf4BacEDqk32_PoydDmOwPcUM7FPJRWxkIt6iODktMzBv4CqAQtoLXwBldHWGw4AusR94zY2ltH6OAPaacZhrt-MvBtzbpinTVjOZ0.bPUnWLB-N0QYLSluwGgKhA"
            }
        }
    }
}
