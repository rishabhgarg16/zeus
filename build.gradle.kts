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
            version = "0.9.7"
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
                    "eyJ2ZXIiOjEsImlzdSI6MTc0MDMzODg5NSwiZW5jIjoiQTEyOEdDTSIsInRhZyI6IlN3aWx2Z2ZMSjdNLVN4NmkyM0NTb1EiLCJleHAiOjE3NDAzODIwOTUsImFsZyI6IkExMjhHQ01LVyIsIml2IjoiNUV2bm1kc3lGZnlLUzlHQSJ9.6v9PJuHjHYa5Gy_j96rehw.dOp7GskZMswuggTI.9-f-tlj3IzBKjs4Map0TIoydqVToyousfOOixj59oDFJt43-tCQ0bCjh-iHnSCnmRY8M28uoasDdzCh5PiR1VbrYOTZ4kn3CtqsJzdN93thrRnmsarYwazRFUAet_BqAFphSkIypg3aut2RtKesoVPxjbr-UloseVeNoZHzaf7Fd5UHffgMKv7FW9DIffY4TE_n0T6ZiJLiEBtDO7N7_QlpIFaSPF92A3tYPSYeDj7SwxB1mtyhbEs0FUzCxuND2HiTG4nOvDJt_wrosUo9uPiBmzBZY9ygcs4tY59G804zGLtFPimq2KF83npLytR4niyTn_MhdI9cU1n7ejpI8DsuIPS0j_s6VHCZF5dIP5Eb1WaNO3W5ldjOXfN5Ubmp9RlXRM-CFYGKnQscmny0vMjeyTPhnCd3MgtP86nzB7e1eta9FaiUunsr8JKRPjxq5JXfQBB19riVC0tUN6f34d-j1EW5mdryT-FReBYeYbOMgNxlpvoZWys-UtUFGvUG-wV_6WO32Q2mSVl_dLNvYEauqxjCjJi1IjGIxGbgIp7asxWXMGKa74GSt7SyQvOKzLF982z6Dd4fJ98eRIW-ajsRipNit4aIkK3C8dITlTmi2mKf-7yeoN05_19bxRiV9T-LlPzfdzC2Wg2wy78ZVorN6yJiDn-y3A4Fax2ZA0-8cowIrwDsSjOwVc5eLWhOgRo7zohgkLGWFAR0_fi5Xnn7r_Nv-aW3c79f9WrMT22Az7hYtLuEyr6ZLvx1Tm5hUtHA1_-2OcBegAYcCBR4uc1OrhQPDZqP2rIjoRZsgKYvH65_q9I3QAa_9ggbndnjR3Q3kU9doP0XBPlgsfj854g94bK4AE49EXOFEidP1mEgk1lwPTqRZYkl2K_qLuDnpuc1A6yEbJMocgyuqWYqZph_Iour-wjbkmvkA-oWuQyHGqpDzdIrHwwf4l70tNUQOeh3EUTZVDrQo-PlBMJQW6IYgqBHHciK57bNdnFoijcXq5As.VOyTin7uUxZVSpzpBGmRDQ"
            }
        }
    }
}
