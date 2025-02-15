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
            version = "0.8.3"
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
                    "eyJ2ZXIiOjEsImlzdSI6MTczOTY0MDI2MCwiZW5jIjoiQTEyOEdDTSIsInRhZyI6IkFJWFRnSThBQXRlX1dfRTVyUnhGZFEiLCJleHAiOjE3Mzk2ODM0NjAsImFsZyI6IkExMjhHQ01LVyIsIml2IjoiOTI5UkktOFNWbkozYkk2XyJ9.FOK-1kzs_OQKMXrmwtyfGw.4-J27TQvNCP7MXN4.bdSfEbNXUswBjWKbJufCucfXeM68Ss-o1KnCoB7CsaSrSzOnDVsDDrSzgAmzf2yzXP5Qu1_0up9W9ve0_8GWxazUka_f0b_00MTujZ6qRATJnif_OOT6ceIzzq2SVSJkI0xHCAVdpNsf31zrV4lHhY8lI1MXsl3hp-rSiKqNT1AEHhqEDd3zQ62V8Y9WlK135pa6Fnejz7GeNrK8Fph5flHor5XhMWS2E7BqsrwfTZmlzpiVMbcpBCsvZT3cCOwqZRcb-5jYduT6loiG0bq09UjoTiklDMRiaVKu9YhwWSBMzm34YkP9sf_peGrykt9HiAYxp6zoZbmL2_jvsHHwAe0-by8xeOTvjA-i2XOjWgIbLJlEwXiM3hdeMUpAPgSYpYBVLCOME4ckcptErkxtyblOThCsuSNp5JptaFNyxc8SJTk8fEE1kFwfZZ8HL1ffRfgdp8dI_LdO-0IJgNqwKXZ7fgh01l4qx9zflK3HLDxR6lRvqLJmiGDHA1h8UuIYXTZKmgF-I3NWhiW7nVulUQQxPG_vMZmXJYs39Nouz2H1J2fhrGNq93IgY86BR-b_gdSTqQad3ikzjQHrNyS3Rv-14I0OHhjKH_ouLveyWx_689FXU9DiyOivlmjBGdZEwWTnusDBidFKcFsV7zqReXBpA1eTUAZvMmsAfVc2z4GnQounrtj1aVfiyZwnSTfeAd-UlIeuiWXdAsKUl-Un0sXC_kil-KtT0XLTiMFv9SZZ5J1gqBJBU5wmKb6xy07UJjrTQA4Ze4uK3PeVYj5q3Ae-qscMiVeS2Gk_nWXq5PLSzOFTy7U_8vCrIz_MsajA1lwr_u4vq-WKSOvrblvImcQIJkuowiotrDIZfGM3ibismNq-dcFpJpilFkZq13cBzBOUAFWMXk1_drt6t0KJL7RpMTePgsPzBcTJxsb2M61AiqZAVaBON7EO2T6MnjhNbwq5LfK6iNTjP-Mj1lVpBauEBw.H730qRV-woV2DUE1MyTmeQ\n"
            }
        }
    }
}
