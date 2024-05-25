plugins {
    kotlin("jvm") version "1.9.23"
    id("org.springframework.boot") version "2.7.6"
    kotlin("plugin.spring") version "1.5.31"
    id("maven-publish")
}

apply(plugin = "kotlin")
apply(plugin = "application")


group = "com.hit11"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter:2.7.6")
    implementation("org.springframework.boot:spring-boot-starter-web:2.7.6")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:2.7.6")
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


//tasks {
//    val fatJar = register<Jar>("fatJar") {
//        archiveClassifier.set("all")
//        from(sourceSets.main.get().output)
//
//        dependsOn(configurations.runtimeClasspath)
//        from({
//            configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
//        })
//
//        manifest {
//            attributes(
//                "Main-Class" to "com.hit11.zeus.ZeusApplication"
//            )
//        }
//        duplicatesStrategy = DuplicatesStrategy.INHERIT
//    }
//
//    build {
//        dependsOn(fatJar)
//    }
//}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
//            from(components["java"])
            artifact(tasks.bootJar.get())
//            artifact(tasks["jar"]) {
//                classifier = "all"
//            }
            groupId = "ai.hit11"
            artifactId = "zeus"
            version = "0.3.4"
        }
    }
    repositories {
        maven {
            url = uri("https://hitcentral-590183692348.d.codeartifact.ap-south-1.amazonaws.com/maven/hitcentral/")
            credentials {
                username = "aws"
                password = "eyJ2ZXIiOjEsImlzdSI6MTcxNjY1MTkxNCwiZW5jIjoiQTEyOEdDTSIsInRhZyI6IldCTXFlcWdSMUZpNjc5Z2tyV2lrR0EiLCJleHAiOjE3MTY2OTUxMTQsImFsZyI6IkExMjhHQ01LVyIsIml2Ijoibm83Mkp3eDhTSFFCbW51OSJ9.nm90zD4yesDZ7LXmshhDFQ.3OfGEHm7mxWCgXHc.izGCsVAUShhEM3JEhYF_KCVcP92NKhlUKHFZ45AbA0HffT4m6fk2Aw9cc_oGlrX-xau770b50aX9-Ia9SBWq7D3QTnIdTaRRlX788FOfnvUiRdUHjW5ImkMU0kywjEBwXbK0PBL-dBPYpVic6EVBks_FhKoDE102i9MXAXSE7DBoP50EOf0PWPXsn8OPjadd_2_gJGxOkLufdDJDWZRO9Z4QVQ14-1j_Wsv0liMPGovQIgeoSoYsz_tr4V4tGqL3FCwaRJJmEQ97J28qtjZn-1Fxwc3Yu250onte30xWv6E_qKJUT9XkSud9nBowmCgxK287PH4eJ_fAzk8oRL6-E6lNvHPShUlwU9_wbNIiq-7H11V7yI1A91gAEtAfmPxE3WqxpY4LR2aSZCMzyy2L1t01qH7E0ay-KtcWXL19voiv8OMMEWuwEJBStMYFvSsm2BgFAf7F2NRZAUkDx8NzgdZ33tv7yyzufCsvJaS8g9HLehTibJ0YozxwnesqAZhzQKNqsu45xofKfdvrY3OXtBW1Qd8_XqvAtGtPsvwt0EVp8I4QY3vImp6u8cUgk2lmIogo7IcWcuSEGgL-Ywu8W36be5zeujKEpTcI7MuutlZrXVyZJyRznV0zsPuqBUbW_QPmA5Ynbg2LgOCLtYwZIkF0TUcNpHc-0xz_XCd1ttF2p5eGHTGnjl7p_u7JLx6P8HkSVWwkHnkKTNJSEX79KGrU21doNqHZTqZ9n4H7dwGc8oJ-QqzdDYfYKqbglHhppwKsunvzU4cIN2qpQZCdPoANs0aUnF5ZyrVIn_TvTY83m31c0nkg5-RpAYHLeuuKtPI1Kfmtqwl4dIZz56IkgtVkJf5x8zj8yxkpYvDVDSZmNw0BNryPKmpOF--yfNJdp6VmqrR7SN5joIfzfslqeDCNSwEy.PHKxz4VdRs6TyzZ3a6gDPg"
            }
        }
    }
}

//
//publishing {
//    publications {
//        create<MavenPublication>("mavenJava") {
//            groupId = "com.example"
//            artifactId = "my-application"
//            version = "1.0.0"
//        }
//    }
//    repositories {
//        maven {
//            // Replace with your repository URL
//            url = uri("https://your-repository-url")
//            credentials {
//                username = findProperty("repoUser") as String? ?: System.getenv("REPO_USER")
//                password = findProperty("repoPassword") as String? ?: System.getenv("REPO_PASSWORD")
//            }
//        }
//    }
//}