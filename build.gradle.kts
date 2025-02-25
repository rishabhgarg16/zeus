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
            version = "0.9.10"
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
                    "eyJ2ZXIiOjEsImlzdSI6MTc0MDQ4MTY2MywiZW5jIjoiQTEyOEdDTSIsInRhZyI6IjJCZjJ3eWdnQWgtb3dDbHU0c09fVGciLCJleHAiOjE3NDA1MjQ4NjMsImFsZyI6IkExMjhHQ01LVyIsIml2IjoiUkNGbmNHY3RjM1VGLTFmLSJ9.nmWygt5RClbuln7VH8BaDg.UC9A_0IdDEkBmvB6.RSHelhGbJdgYKbWTD08mGgc20fX2EnQ6kGAzry0gLKHGQxCgT9Q6Xsa_NJYDqHUDD_v27CSnkmXWe9_fVW7yT7OIp70WKfyscHbOs7XK_bydDKwLI_24BaWkrmxxPF1Yesb7X7GcexZX3XdE4X02A9eA69rrpiwbPJMv4-tMBPJiJQjB6vQvWPwcsFoBBU4_wNTbArJRgne9IBIcoYrwtgvneWnPB38IeyPqZNP3TLNUM4yi9yCKoqbsOjX-7cl9VVmDBk6ct2bibReSvNjB4ImbhnzXWZ0IW8Y2dU7xPQFZTy7P_2mdxrExWB2nVU734kZFpSaIh_5A0woKqqeanDn5Wr4Z_iwSORZtaCrMciLcXy_zlZ3Ce-sKRhrPebKUoDnO2M-F8_QnH1nc2WyzI0YrhiApv6O_VhzyB7ynLscbhwry0ssNhk8dfDYiFES23zrIXi8PZ-HqpWhgHlXnJmqp9TMVFWdVHRmx3odXjHz9OjcM1ZO_6J1n14RLjCmM7Myb2ybCqb1GF-ntaegzKUROy60bjqmxSUMbbf_SzgS3A_so4CPoNLgiBMTurh7WVTrO8oz8c_ZIoMvGQYRO-3VXM88mlSVsLzs6iok9pj_lOiumtTDFcQv937M0Ehkfykl9wdEjtzv5jdQUsN9o-Y8_0QCMLGdnUh_NGyVwfoF-wxkbBZix2DaidnmAqWZosz5xf6HuLkNo70HC06pKkiYGLFOFGw_NGJbHyoYvHzt6fBurmK7QONIAUXkAw2rAVShfuoDhQkbwMot2Flvg5H1pvZuT8wSSQINGg_IgIpFMkwqrc114xHj7frosFzLs3mF1WJHNT9lUGy54R1T4ihUtQ5qx6VR5lStsu2wVQMt7duuOUVmOC3aYmvmht4A7BUak1oJttFQCujdMeNqDJPvTB80A7rD7VqrDUMtGbW05o29LUuNMcEwuxfe6VbHpskw0wQIB20n6JBm0LSYn5Q.abQYXhgufSCMcOZBnp86iw"
            }
        }
    }
}
