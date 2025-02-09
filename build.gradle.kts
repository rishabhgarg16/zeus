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
            version = "0.7.4"
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
                    "eyJ2ZXIiOjEsImlzdSI6MTczOTAzNzA3MSwiZW5jIjoiQTEyOEdDTSIsInRhZyI6IjRxU0xNSmdpeEFHdUZaZFN4cDJZN2ciLCJleHAiOjE3MzkwODAyNzEsImFsZyI6IkExMjhHQ01LVyIsIml2IjoiWEsxd0ZJQjF6bEg2Q0k1ciJ9.F1htJ1SDThA0ATtHnpNUdQ.VWotuRnNRevLzGMg.ugKsyMx0QgG7avQgeBF4xuuGK1mwuVUGu6Pu5kNAtqx5ZrCEwKrgD-TsQW2XHl6o4B8KbRIBLw75ty9CxQnuF-rQDP_9iGRIycz6Yj5OmFNEbPbj6XtwAPDk_7SN6yXxuFGLil5N1B7DRSyZbO4rI_UC6uAJir0xkFkDOin1R3OpkX32JWnuVadbILoyywvPaSJCuqmo0d2HQ4ReYGrA97T9juX3QYPMYXa1J8WPVVSSWUzVDWwO30P3UsIipulMn6gT-7dp6W30TQbFjVgNUpOUQetMa_to_Q9Ne2-HHUDiOgErVm4rQ4jFzirqX6U4_KR8cXtqGF1OOcPn-K4XGnO9y4xEhy9GavxBMB3yT58y-CSujj97B-PvE3mPJa3NodRBgDDlLxqJ40SiUMqtqz0yK_lRJAAyIZoccUcfywLE_ypIq7lV9S3wrYDgoRXiveuxhcj_sAekmah2lcBdVNU30vLQ4hmS_DGRwZT9zn70F45bxHyUzd1AQvmz6CbagBy8i9vwFyLoxztQEvuk3ZQYIUwl1sVhefEz21LFasqM8VcMlQaXPUG8NehGah5gu3OL3FhFTCtUJXm0gjJAOsfT0Xzf6X6Ha8uMo8pvDseD8Eg2yLjt6qsdfyGC1knJCeARj6uLJoqQHz3wS8WeGEVJAIQUZo_BmSAGAKzZ2w1-ojR_x8--VVVLMcX-8AsY8UaLuYw8_kOxTWMajrFqA8m-FweRJhNraK8DYc9r4Mol5ke-yzwWVCg67eeShgM23_M6fdA--_R8wkQAAbt3y3aP-BWexFsTQrcoLulI9ZK2W6Y2EvXe1TPvYbG1vcivflCCvVBE_VxVp4uoBojxhO2IZ9i9GTPl5k5HcFWPf8qOLhyJ2e2WqsDGJuanjqxWxlCS-sUAEtw7iCvD9FSsLdzow1aj1-opUYrAq7GcG8h2_qjR2NpffEs_EemYjGxoK1K156XokcqyWRwZCvLKPyPE9NcxwlJlHWWslumCVC9HW4h21fZU2BJd.1kRfyxvC_ra5TuWPeHWnMQ"
            }
        }
    }
}
