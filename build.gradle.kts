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
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:2.7.6")
    implementation("com.google.firebase:firebase-admin:8.1.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation("org.springframework.boot:spring-boot-starter-test:2.7.6")
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
            version = "0.4.0"
        }
    }
    repositories {
        maven {
            url = uri("https://hitcentral-590183692348.d.codeartifact.ap-south-1.amazonaws.com/maven/hitcentral/")
            credentials {
                username = "aws"
                password = "eyJ2ZXIiOjEsImlzdSI6MTcxNjczNTkyNCwiZW5jIjoiQTEyOEdDTSIsInRhZyI6IjZXQS14ei0xTnBRcFhKYnJMYnVWVHciLCJleHAiOjE3MTY3NzkxMjQsImFsZyI6IkExMjhHQ01LVyIsIml2IjoiWHA2UWVZcms0RmF2YWlNciJ9.OCPlDSNmcjtxW8WdV-24fg.ANVJYOupVq4bgck9.8OZmVQx6ievsVkWZKMuTOszOmHGN98mZDGEyrXTQgEfcOiD8oY5uV22-IlPoUOmQZW8m7aaCeJSH2JPP1OJywpxXV-N6M4Zxn7kaug_SiW1CTADc7FC1Bn40H2O5aD90Gl7GAiiNGKv4J24HTgleUo5oURzr64Sb8RUGJtioJFOeEwWB8NDT3f62hj9s0n2b6OtKHMohSw83tQpWVPhIXJ6q5ZCwWLhIToQ2RbHVzOby0osfYYFW3FhKngYXHY7LiL7RQAO0bFVQoSC6y40Yy--WqO4BtBu9dSbJqle1RwUEYcjWGH6dj6y5FQguL5S5FZAwuIb9Tg6CvHNZUeXfvshg3587mIXmFjcBXTMbmVCrlSGkZj2qL4mEy-VCNrmXQRVmPVNm140XbJasGOZXTQpaQfaj0q7bs2r5Vb_B5aRpqhv96yZbyYVnVzq9Vr4iwSnB-wrGyBEUV6s9-31-8MRS50kSw4XJOL7Q6I73Q6yKuZYpmWCCfheUo29o1Wir6HtIfF1x7JQjLaR7KyANZfBCpKSROpsJv5_btiT34rJvspe49sdKzx0A021XZc3jnHaWX7RdzmcTnD66A3LeUBYpDomh1VhQeVKIYDAf3zI4yQnlRrWMBlxC9tkH49FJjKwp3zY0VXZIlzodA7Met_QyMZCiVpUdza1ScChWK6BSTxNQMM502Dm6kM9NfYFrL08Kj-b50ZzZiYiNIsIdpeRkw1uVmNPYX5F4nQ-IvKJFfws2qh1gamInBDtugWzkxe1UU89HWlZsmz0U8jIoUPeHp-EP0v17NQw6OxNES79erftobF7-qp4TElv-KWQaRRtWgC7BDCYdMsWQaxGBmd0e4wshG7nmytARc38_UYXtfCtTrTRRxZPFVMbD5JLSFTWNuvD-qLTY9HiSR1tRac_7Iu-mriuW.bvEceTCwgPUzJgQglO6VeA"
            }
        }
    }
}
