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
            version = "0.6.14"
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
                    "eyJ2ZXIiOjEsImlzdSI6MTczNjY3NTk0MSwiZW5jIjoiQTEyOEdDTSIsInRhZyI6ImR6TENHdy1ZbFo3dndRRVBReGt6SFEiLCJleHAiOjE3MzY3MTkxNDEsImFsZyI6IkExMjhHQ01LVyIsIml2IjoiWWF5bXgySGFRU21CMWhIUyJ9.dp9xodzMa7LDop0naMJnpg.XicUpfPgS16L2RqQ.1h7lox2s-1s847u42rGSPHcdRZPm_RMUWt3FJfhF2QurKWKuir9qzhmHiU_aX48DuNzRw-n1Odv2gHGz7cFrvVnmqvOyf9pnMjG-zAdUbyFgl-qL5cVASDIzjY4d7HU0aD06sblqCytyc5RYS1luaAcGimQXzmZMHw1QW-oZBemQu5nAdP2IUUsLnVZjXUr2Xlqk6w2MaN4nsQ84U9sJm2tCc2cneE87cosyf-uSAHCiGZGjj6P5sVH6m2ZC5_Y4Gbzg9wGADeEnei2lEeTI7kw89zPvDfxw3eUOWSYiU1TPHmZg-8anO55s8L2M4MhuM0o41e7mBuSKHrHcg8udsk5MNJbfThtFa-HFsS5PZ7C7MnnhORGo5pauCjVDClOqa-Nmimv9gS3jINegu0dwSuBCqlKB8yzQPbr-qlWAhLRz13MLB8sC3MNjadaRpfGxpomxMPFmBks4BzvltONO58lfNl3Mo6HQLLkoCxckFYlusa_3nljNmUrrSlld9l5tv6oSMITMaSne3n--fGvWws58E8CurqeMLQGOy1GgGCFx6VG3etZeFQ5veCTsYJzdp08_g7StAHkvIY3ea89-AQ5JCKGARj63ks72wD8rzqzLqMFr6G26pFaZBVoZGWcDaFsezjhcIi4Sp_aPcfTfoeETO5C0xPe3VP0VvoquxDzpokjy0p6WhE7DpSfxbBOyu2wB7P2k6UCszkUjJ8KX6lQFrCQpFoXc9fy6-rvnQubPeWwTPATimbyfkc8nCFPSALtucB5DiCrOytYb0S1JTskAD47Be0ODXiIIH1B1DUNeFse1pYUiHsasmZwhiV2g8xvsj70OfPZVVbE65_wPctBOlAdBjBDHDm0Yxutsw0s6YtqtKgcfZJ13tOm2Zh_4jbdPBhgi1R3SJBorQba6GT_BCMGIe0Mkl2LRAEbCfyRLHENDT2gYLoAe_s7jj-ceaWliMREHWKnKxV_OsQF-CSH0E7ouwclRoek_Ct_vu1dHprRB8j8.ARl1yxC-5EYSmOwVA3h3Tg"
            }
        }
    }
}
