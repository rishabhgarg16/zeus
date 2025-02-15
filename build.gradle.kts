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
            version = "0.8.1"
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
                    "eyJ2ZXIiOjEsImlzdSI6MTczOTYwODg4OSwiZW5jIjoiQTEyOEdDTSIsInRhZyI6IjdQZVpYZVhsZFF3OXRJRC1WV0xpaGciLCJleHAiOjE3Mzk2NTIwODksImFsZyI6IkExMjhHQ01LVyIsIml2IjoiUUtYWW5xOW55NUMybXN5aiJ9.jIs_LfjxpyxhjHLSAox6gg.spLaWWZMaqCwLYFI.1rLrtHk6Kgwe7jUSOjdlmOJlxssy_xpWnQwPW8ZCF0BsOdJDDvkAh32IGccg3FYMBPk4bCRrKicY1IFXuJVleEznPoVsZCMOLOiazilkdtB-VLk7aXRGxyKfogBDvykppeCtS-gzJcHb6H9_lIJJe-CsKj_wmmicV6c8kMr_0wFZ2bZ7B5ZkqmruGqnRMz1ekNWiH9z-rxTFUizWwARpeFQ7fZrrXEgcQACwTg5fFP8WgxfcJscqsXXOLH-ZkqQeLJqLcml9JgscWND0l2LuXk8S2xbnOrAH7My5y8KdJ_syT23QPs5hD5AJsqLbvutWjsnlC6L_InFb9V34uc8NW6Lviag9xa6gRcJxfXPp9_Yd5lui2eIY8q9UcFJcXGEDH437Obkw52kLTfWCD966QLDZXfcyEPuIE0ZOPzer1z5YNa6RjEjoWH_GAm-P51D4jtHXqKwqx7lBMxoQZWr1cKHtmnISgzaDvnsyTRcd8hs_TRGmKLXaknLUSTG4E7Oj3xqhcLC9CXx_U-v8zppDe9WYvh3DhtiH6dL7CN7QP-Rqlo1l7OkkGvKEg-ZGRpunESeRSg3g1dX6TPQsPZWp7yNhXIpxxX6Qbci5AkYHwKiTJyhtsHwHr10f7VYc87UXsv3ZaKkyuJEP4amAFo6sPOpEKaL2vbzCW0dD4mwl8CRN6UV3Wb9_MpFuKlcKTcIhaHcujgBgxbBe96N68LM0vcEP9YZZK8hAtJqrIsi_XuQSTdc7Gs-tH0YYzju22xi67FIm6O3kqACuKrODFNRay8bkGjpa-Xr_dWurBnV2-M8UzahGGSdcd1MrvCxZIacXWYybkU72uM6sb0MClH8d6-LlRtkQ7Szi6SkgdVcPaBZBEYAHTfTFZbtzqM6952Q2vgxDhpXQjOhhcwMQhByywb6AuqwiLiocsjlRbjr6NIi9MYZItExXRGDI39ugdBO8w0sP1An3AwDdYHZ4GOihEw.mKFrzubzlPSFtKw1NoIcYA"
            }
        }
    }
}
