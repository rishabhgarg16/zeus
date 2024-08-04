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
            version = "0.6.10"
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
                    "eyJ2ZXIiOjEsImlzdSI6MTcyMjc4NDIxNSwiZW5jIjoiQTEyOEdDTSIsInRhZyI6Ik1JTUFrNzVlX2o4RHhnR3kxQm9WRWciLCJleHAiOjE3MjI4Mjc0MTUsImFsZyI6IkExMjhHQ01LVyIsIml2IjoicS1zZ2NrVVY1dndhd2FteiJ9.i9jJ-qVpnQZ59H1k-qBFPg.HhPQW6oAFGzVdPRG.dQsVMLYp_0GjVr1qyyhruvdjfTE3WFWVpxHEMc30fnXL0el0f8ZLDXxxhgzM6GtIKveQSqg3ZIrE6k1wNE2lK2LOpqtLTSfg4LCJ51phkw0SCR8nifHnYsel3qvvF1oAmQXieZK90qJcn8I9PfozTtEEnYcPnGC4EW_zmdpELz-ybUjCndT4f-Bnpwyo4P_UGxHqPARzhmMk4kzFayIbNjouNmXoiGsRWOz1ac9p_orW4XN2GAcvzQyszxcuBVjh0si_CdmPnwTD9wMwBeCLppzQDlwRSL3SE4mg-qaqnph8q_aJIeS0ttemvJn9ngLANMlGW8_QzGHgcyTPQmwTLvhJTvXJpY2kGxLSZfe3JTntTJKaw5vrRRYlCtfEoto6wvNnRNT6Z5-OM-DR4baHfRGpt1sTJu_fOT09Bsbf5ZierFY05daz41NtjwJVEjWbvoLBQGF5XO2zHxLEhCeBVBNg6AqditAAXSk6vJFJPxz0enXKE04p-AupKJbD8MVj08UJnh61WzvDPtqPcplfTbpubvWCOBQTpJoL8Aie6YLKy0IuLGPkMz9uJRoBd1xWsKX7mH7krUGpUSKby5wRVCt9FSvqp_mn9-AhTn7UG0bReL9FPp0yrxbq57WBA3w85XgTXPr-4TrXcClIUOZVBfTXdEr-mU69OlKkgxKmhZydOF2nEunl4Lq-_XfdJPF10uCBDeHMOY45_XXleiP_AhvdRRZIqPy-kzP9exSgPeQab_-sDygx1BW6m-F1KNABmk70FXO9YCfZh3fhCoHMdH8MM-_rLgttYEq34Y-4s2NnljP7mzt3y3QOkC00viGCalrbXXu8jLRCLvhxkAVZttR0RfnzRQCHM2W-kNWo24v5BctWylmBivKw647BRMkk0BRi11hekrAfpbiPPEzOiGPDLxqntNVAIHrOXGd8fwo7pxMCvJBkkvdPDqZAYuIJ_VTG16K2-4ZD_Z8FJVfsrwTokXmBTREmMM_LOu302upq49JSQXg.5sNoR5lG4vxv_k21OBfavA"
            }
        }
    }
}
