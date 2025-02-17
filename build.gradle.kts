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
            version = "0.8.7"
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
                    "eyJ2ZXIiOjEsImlzdSI6MTczOTc5MDU3MiwiZW5jIjoiQTEyOEdDTSIsInRhZyI6Ii1XMFBweTkyeTMzeFRZVUdXbl9mc1EiLCJleHAiOjE3Mzk4MzM3NzIsImFsZyI6IkExMjhHQ01LVyIsIml2Ijoib1lTdXdOTmxGR2F0MUVtNSJ9.lAfkadbnx5MJ9CiBgOzchA.2xuFwHpW5RU2MF7f.GCNB5eZEnni0hZxGHEgG7Few6Ab5GY8NQBgzVLEsJ1y57GR0AiapbHIJIF_5TMj43Nih3H4wv-NVpFph1MplXl7gkImjomTH7kxRTHw__r4vcm_ONbAuWP6zX8ZtQoAh0CFOLQx8PzLqYgK8Xx1L40np4Qq2d9pJwClK6DAzmrTHDpQ94ANhD-igsDhYIePVICR6UKj6-kVMwxdwvrRZvTTXxNE1wydH978cQfn-b6sStqQkX8YcfOXNnyKQEv2HDXChuKObUD3oVn7B03broUlVcyLe_WIVqtW4USAHz2qaajwxCQ-1vsXiRDjKJvXQWCtFGu3kmer70cBEY8rhZwbCpgAg1L9z6aw9U7yUnfTfTy-YWSINXX0no7SISdIUmQ8-E4eNdO0EhqA9I1i15LjOedovXhqkodI588oYxg4UYOTs37yN1SKwK37NvHAwDh2_4CVSzXywz4Y0BJsxn2blWZY2lrfn-hFWTeGGnFiDYpKaIJEg1ugymolBU_6WrK7A0BH0L3B6tGPC9FeBPFh_pIC5YqJ_yFCx_obMnpfnSlSrLiNySnEkfXtXW1BWh8nznOjv_xuz_XNYeD4awlDQXSGP0W_U7aFi0Jv6G7WFykSQjOduR9Rx-u85Z_DKhm1ffjrHYo4zeNjkeI8fKhZ97JyhVolkp6s66FuZMkZMruKU0Lc1_UsLY0gjtRDSeWVl1suRGQek-pr4FAlgq4Idg8AOXPfGgdZQeq0p_p1_UsNGj3o_ouVr5OKogvGHNZCWyhah6-LcgyGaFJ4g5zYvodjiDNunT2VyAYUF4kCd2gqCum3bp-v5lWdrYrSjavsfT9bcNGBGH85_01U7jQDe-KGGHIJSRoD_EY0tahVAVHL8mAR79LR1LUu2JnAzGuVUf1OsnsxTxSrP2Pybm-MS-PLMb9alY4nLHGwNidiawSv_TExKHhFwpN5YQTqbSJuxdXK7Db9jBxY.16R1JaSXqdMi6yaWmjuGqw"
            }
        }
    }
}
