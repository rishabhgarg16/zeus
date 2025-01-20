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
            version = "0.6.21"
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
                    "eyJ2ZXIiOjEsImlzdSI6MTczNzA2MzgyMCwiZW5jIjoiQTEyOEdDTSIsInRhZyI6InVQM1FvVndTMGVlZC13Q1ZGQTdIUUEiLCJleHAiOjE3MzcxMDcwMjAsImFsZyI6IkExMjhHQ01LVyIsIml2IjoiUlFlYU1sczRkWTJYSlhkYyJ9.ELT28sEr8HHO6uVnTuz_Cw.PxBONLL-71OjdKfn.sO69diSjA-5j21YV8LgoXgY8t0yv87ko7Z2RKQ8XeuZIgxvsb7Hmohcv6sp538yDlHEZp7TUf4oosovg7twWvNUhbeUd6C0QBymctcOzIb88Jsm6EGxp5j1Qb58cfl8MQByojTkSECciPViryOF8vRhhaBbMrFlkkbEoAlDya7IMX220AlMKEqIRAjG73PSAwyRCRxL5PG79UxZ8NfgNJ4G4PzyQr9Lt9KUdEwNMYbASmubtF0niSSbMXSYx9EUA892EncPpJsXYyjdUjkBq1752zYdI5xnfrjZhJrWg4GteqU_ihH37UkTPjkQ41z443U2s-WO_kX8uL_XjVQOaqogm6SVmU3XbyjqqlTn710qAnqGLPEbp6vknHJim4NHNMjzo4H4Dqm7fyQCKAc8xDE2ed8Wsr3qERKHNl8Zr2YqYGM6vQU-SPYKdV6DKpfRKwlVVQVV2LmBZESkuCE5grdDRB_bOhGjw3V5cknQdXWGMTpjcOIlbVRMBfwjU9vng-uImFhvf4XNysa5dKHS2S5OWQVd_RJI9AXE7g6yZVjyh-YE6Qe5OaONud_rHM8PTa1bYBmh6asvhk5FDVHx_OtF2F0ZjOWagYveXiyvV0LKHBqeXX5HGPonFgw3Besrz6LVVcRQJlxrGJj7HPrYTyRERAuK8qqn8V11WNZzmr_4suY4exW5mMQfTMujLXX2HE2lFfa9g7sJ2jjHsnr0_GaVs5JkIh9453HLotTjF4z3sZNzvHjwhhFfS9vB3G-q_N7W1u9GcaMXNfTDvSLNq9kvWycZkdOn9dzjb1zUf-KB_ZXuxa8fEqG96cwgQudUhTs9FYYNZOkonbMNHfK-hIXjbT2_oLdVUhwWrJCCtlhbHR6eE8aKvkTZUm3pGfeW-Ctd_yNmI_9FkQF_tMBshdFE2nqFtVaUfQSm-7Dr5hkl4Zoua8vqjpsJjy7isJWawDoOzA_sdsJoH0evQRcVE-PkVIRnhPjQ.9_Ds1CUNCMzB5FwCdSyIVQ"
            }
        }
    }
}
