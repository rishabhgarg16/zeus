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
            version = "0.6.20"
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
                    "eyJ2ZXIiOjEsImlzdSI6MTczNzAzMTU4MiwiZW5jIjoiQTEyOEdDTSIsInRhZyI6IjFheWFLQmU4TE1ZNmRRR3BOOGZ4d2ciLCJleHAiOjE3MzcwNzQ3ODIsImFsZyI6IkExMjhHQ01LVyIsIml2IjoiX0NEM3M2dWdtQTZpSnlBayJ9.VwvExn-REFVVUaI_G20vXw.v-OpHbRLQgM4w0Eu.ehezS8ebaATy9sPPj6aT0I0CLAqTV6KqjtiTxA2OhwJAhOHggoYmg92NEuwvBD-AVJLBBQkGILlf-SZYVLhfDxIKM8XOB94gpKN69H-g3bVeG0j1GtMDk8HDehYLMmrUj203V4qN0jlsO14ZNWp17hjYub3dNCioWk9fmth0UwOkPqAX-fwybMNGi0ZP8IpHLKBfwXp-d2Rinh0EKMf6TXVYfDyCco_PBYNwe8qNorc627dh8TnJs2yr0a532sPdD8B0jFyU3uU1_C2d_Yfq8_n-YbAJ4gCndWEHTRC6EKP6QohCBlpANtG86VDZIVVnHO6ssbBhRMbgYtE14UWIKwtmzJZjaAMmO0U5Xi9aDhizGLED8Xzh221ztpgEYSY_QySUn0z2Id7AlH8gqVlqe9afS7PRN3EzlodX5Fq4YXS9ndhdOvY192ZCfpUMVo-0239MmEOsUacGnmXyp4gtnLMQuMXZIOttVf3w5JaTBygYIW9VVOhbeknv0tLwmSATKDsd6YXPkMJ499TluOFKiqxD8wUYwhe-PcAN65HE7yTtIhRyszzaTvm3huX_EU5eSeUw8zXAmY_zb2xEfZJWEDWaNeBH-PYMrM-e-DHC2TD29VC0DMXPkSuq2yuTHCM8K_vS6L5OC8ZnzorgK8p7fHv3Q_jmeTTGa1MJ8L0m30gTK_EezLiFQX2J-dHBTapisbjmBRLzI3JoNfGrN9aUnhBSfTAnoAAmAI6tKoEJ7NLSWuhvwMSB_5QjvNN3_iAAqCfk8w_qeHm4MbF0NlOMmeCKyE48-gCeEzF6dLdbESxyR5z8U11bNG3oCVWl460JJKW94yjOowPvRevLOeqEWR4g_QjF2Fc4Xtk_zg-p8eMNVXgFZR6YCsWqEnXMXIWn8yFFb1qyb4o7EUGmzVY0e1riHpW_7WrFJMQiUnYUFh9Dg-D_hbBsiSCWaHm2k6sRQ2ucaNxvMLPaVllfaTw.ZIuDPPnRxEOv5iPzYeSrKA"
            }
        }
    }
}
