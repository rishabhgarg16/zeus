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
            version = "0.4.3"
        }
    }
    repositories {
        maven {
            url = uri("https://hitcentral-590183692348.d.codeartifact.ap-south-1.amazonaws.com/maven/hitcentral/")
            credentials {
                username = "aws"
                // if getting 401, run
                // aws codeartifact get-authorization-token --domain hitcentral --domain-owner 590183692348 --region ap-south-1 --query authorizationToken --output text
                password = "eyJ2ZXIiOjEsImlzdSI6MTcxNzE1NTk0NiwiZW5jIjoiQTEyOEdDTSIsInRhZyI6IlVrTVFIbkpvdFNtU3NMNnRpZEhuUmciLCJleHAiOjE3MTcxOTkxNDYsImFsZyI6IkExMjhHQ01LVyIsIml2IjoiYzlVRlhaM3FYeDVsOWtzLSJ9.ufl6P5Iq9AC5maHTUXrPvw.EeiWQUiy50nGoaoC.AxsIkcJZGH5Fh99_j1vknS-KyxrL7qj1JvNoBZvhSSpFS0WX7wkVOoVHblGqAF-ccaDliRUY-zteXeOFMTdrC1kR8wMzKO39HVeq0gLgYzgwXAoJDAiBnxZTXFccQTk6P9gnNw1EdRwLiedWvAr5MrJHAHKPbxA399ItqISDr2Xgt-sw2v8p1HF6nQS4A6KbSwBn4O4RsYcvixQ27MPRuxdYsulUmosRTA2YPYxhQP2u3vBYQF1w8sOA8sbT0L0Sr3et4ZeGn4lwTt2CXJcVd-htpAg785jrKQszQ0TMagAnPqWn5-SVz5wHx1bsMt-0S6dtF5ll1ZbKrAKLmuCfBEQHj8by4v9PbqNpXfz8_PPUiqAEHPVu6mAgNSdvHC4nVB5OmabQWKZzetI_hlCmdUlyBLkOYUij7N5wUFaoVQn0yj0SUdb5G6LfEp_C2gZq8GzrYP-7ZKG2UpXnjCyRdVEL2gtBk_yCkEPgRcnam5mBF3WQHtn6Us83cnY05Z_sUd_TLxH689u2MHuJf1kswBfG6pFVuSUwYh2UewiQFwvoBHUYb0l2y-n5IW5JNyLgE7EPleFxtBsVw6QAPkz_K4KgS61qU2HuSE-oZCI25FftrysLqF3dKfBgyHzpN4eOUSL1gIdCOOao_L9K-vpdxxUZ7zHXK9zka9NLv_31a3CqRdeszCRJuANaPgHzmIKjju6hWOZuJvHe1y6v8hItDl3Hryk9VJo4RcxGnOdFZBRK8CArqS8YvonmchSvVaDFTru6JdSDEm-FEr68eqBxOKTMZ2ghY8S106gEXZpY-qH70v_ThIaWk03pf7jgJtaP5-S6SOjtLQ21SU7w2_ywBbReTbaUu1udmajHL0kp6kZI-2AAESe3E4fBWGJbJlY5nkZYvNfKDc54JAIxkIT71ABY5v0WrpOiVwyUL5KAQ7dh1C_ChRgH-h8JNPQR.xajjn3zeJY2G4VQk2PD2Tw"
            }
        }
    }
}
