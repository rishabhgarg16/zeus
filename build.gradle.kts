plugins {
    kotlin("jvm") version "1.9.23"
    id("org.springframework.boot") version "2.7.6"
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
    implementation("org.springframework.boot:spring-boot-starter-actuator:2.7.6")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:2.7.6")
    implementation("com.google.firebase:firebase-admin:8.1.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    testImplementation("org.springframework.boot:spring-boot-starter-test:2.7.6")
    implementation(kotlin("script-runtime"))
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
            version = "0.5.0"
        }
    }
    repositories {
        maven {
            url = uri("https://hitcentral-590183692348.d.codeartifact.ap-south-1.amazonaws.com/maven/hitcentral/")
            credentials {
                username = "aws"
                // if getting 401, run
                // aws codeartifact get-authorization-token --domain hitcentral --domain-owner 590183692348 --region ap-south-1 --query authorizationToken --output text
                password = "eyJ2ZXIiOjEsImlzdSI6MTcxNzk0MjcxMywiZW5jIjoiQTEyOEdDTSIsInRhZyI6Im9nZ2QzeEk5RmlSMzk2MkFhNllfZGciLCJleHAiOjE3MTc5ODU5MTMsImFsZyI6IkExMjhHQ01LVyIsIml2IjoiNnA2OU1CdUJOeXFBNEN6biJ9.FaNOUXDr4A1StvrSOGVS-w.gJQDcf5HRExrThe_.EFMis-HwMs_5vsLtPWq2xm4rHA8FNmPNbUz8DXGLjkzDv8xsAVq4rvu8duGhfRuEftYrT6XgOsHjgQByqeywbHNTPJURd8sfokWJnRLFZERY5S6_ACyS46WE8Oa7Ypj4G-osYg6rqTr-aAMrG-s8jLDg5S5SVncGU8LrNZJultu6XlHmseuTgW0pKkR-QhRe7lfkStf-4LAszPPSwMSq7vAEhXcA4PC3HTRqyq1sRw4QpGMCnGMmLxYCio_EK9XDOR06tOy95KjNb7JFel-hvR6YyF5Vti8x-iF6bkbPI9qAScUsS3YACLthO50tEVc3hDsAExNe4L13lSPBiLWGIcHNDDRfp3ORmvWI7phJKlf1vs1EMm3uTAYF5a9huptsg0qKYKLY_MoutCAkLh_zKDbx5NDYQnRujjOt52CR-X6qvcTZ4DjavQ8OQazn_kZ76iZtDSI_OnXIw39et_ijm1zYK4qWovYER0mvG84obX_EmN93bImNf6RrmIWbwpL6fFnQzmUZHzBCZKEvMaJCBnVErd0J0_AGyH_-1kaJT5XnrG_5Hq_pZLtNv2INbiZ0aHCUq7qW3sDSC-v0LdV3QjIQ1ZaaZj9rtrair1Gx436k0HdhYjwUJqdBtTk-xYsRXmrtc9iOkcqsFTrLMa7yj9ddClH7ARXrFsljXMGFTEWOe4NVLqzI39wCfIQfcWJAedOWel0yQC1Tv4EV2kpo5u4Bb4hvZ3k4tyLa9xrTcSFFA_LivW6wLZ_wGK_i4vyH3sGL61DjESIG7yCf6NokDronNBPjSKlDVTXlFLqucix4v7vcZ6a3YqiXzmHmcQAqICdLjT11GhTifGgz4DYavTQGWycd6djYB1pBGxt0MjPQ1CA8m8NSXBnbt8y7sxa8mkVjbCdl-kRuIfnTEpwr9OJlfVQ-povdZNBB3zAjyML6RBPQvf06ztJRZeC0.WOCtMr9hTG4L-ubiwO7Tzg"
            }
        }
    }
}
