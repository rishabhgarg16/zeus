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
            version = "0.9.4"
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
                    "eyJ2ZXIiOjEsImlzdSI6MTc0MDIyNTc1NywiZW5jIjoiQTEyOEdDTSIsInRhZyI6InFoSDdOMmhER2lsTGQ0SDNad1pIcVEiLCJleHAiOjE3NDAyNjg5NTcsImFsZyI6IkExMjhHQ01LVyIsIml2IjoiZl9BMURaUW9BbkhxdC1rWSJ9.qhV5sNu1KMyFxbh2tAALLg.GooofyrfoaZ_TsrO.ICejhROROQR62eF6S_4z8-Q3jCYq_ttrQkeFAX7KS0JLgswZpMnQ5gd7VOOHiL3fWc-a0kMFk9EKx6XPjaNR0pfVaaVMQ4QeRwThw-cHQxUmN5cbUnLT_FddbzotTT8n-_BMcFq5Z9sUhWedr1iGnSxHWNJOTXsVolIAQv04JwMGVgCpBs8XhoTj1_6JXP-v5U3pPjbWoYrkI-NdZ3Qbkg09GB6xwFZmJe1bnyZRwnlaE1ny4Awo8Yn7A9NWHu-aMWpVcAO50wdE5FSR5fd71k1OZ2VFBaw_mJ2E3Zz_U2C7VnZ8k6-TZ_oRVaAb4YNjRP9iw4Xktun0-aXaDJJHeb-YUw3Upf4MhWXt2rVEz17PdVj30lb653UdtmAsjStvMNU5xA-X30B5icHh6vUXKHe06-YK0rAkeW7MvKI-24-NiQesKZgA5-0_Kw4BseZeQDzKJn-V0RzjkEC0QkPg1xjRmHEERgSdDmx35Ru_c13sXdTl9SSguNkbMIvEcYqNUMBdUmjvWcBvoIO6HkoRHDZj94X7doSjGZTw_gmHRULfhafH9CN5RdN9a9ugizZw7bddyIeNp7kR2eO8aVzQblR7SJJl4efeP0-VxWpPrFysD_YLjWtAmvuvjRdSg5UpTxRk7ighE29xn1s5x_k9D40BZPVyZyRfkkn4lJp8se4NIeY1-zIyz8K1dXeT7h4iKth0hWblf3AqTS0WyzfYo_NieFZozL5-hTSikF9BoToivxOraotMKNR_W1OOnukQO-OwyFO6OfoqLaV6Sb3_JpSX3Y7DsvSJWTbdlX3Ym74l8anFFif7sLn-kEYSipiZvEr16cJiYWngeud3emZaVtyIWgmqr6KkIa6HF4EWQ1O_Bh0ZAKzfCzhN6FWxHqjmPPn709XTMgHyJhHcjRh738x1QxTzrc6Vmt7OPFweqHBVUzkWW2_dJRawLw8wuo2td51lOT4OJ7zpaTlsUi3cn3SWXVh508FYQxdyNCfNtoSllg.fq2Je9kLFzkxc13ZWwDAiA"
            }
        }
    }
}
