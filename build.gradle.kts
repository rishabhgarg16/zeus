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
            version = "0.4.2"
        }
    }
    repositories {
        maven {
            url = uri("https://hitcentral-590183692348.d.codeartifact.ap-south-1.amazonaws.com/maven/hitcentral/")
            credentials {
                username = "aws"
                password = "eyJ2ZXIiOjEsImlzdSI6MTcxNjc5NjM5NywiZW5jIjoiQTEyOEdDTSIsInRhZyI6Im40ckduM2NzRThLTXdsNlQzbERBc0EiLCJleHAiOjE3MTY4Mzk1OTcsImFsZyI6IkExMjhHQ01LVyIsIml2IjoiS1k3ZmlkUlZQOG51Mjc4cSJ9.VbDT-y9lCiA97cUj54B-Hg.EOwJ_isv0Rb-GC2r.6MAPF9jL9mOKxN1hs5K6Oq0cUOeWSk-m3WbE2rw6Xb2F44y55_VROLfZ94RAvIutrefva-GTNN9LlelzKVc-9OaXqOaasYhOiXGBkpv7c8ZTdGL69AquyNuey0p41OZTkEGza81M2h8Jkvs25hbNJXdrvMA_3k9_y7faUFGcjwqYbQpgjnwpk33MCCUtv6x_iF0VTg4cp_4XtLSXqTxdzuQVo4Je9lWlsOGJtuhy6XXphsG5KYFwL9E6R35SqvooJ49MlWtNQhkID2UahQNMvQd_e2M7tUPcibwsqD1PRN-zQyzJ_uq77t1hXXZI2frFWX0sQke808KHqHb2992z7I73USRqGeToRQMgW6NC_6OpWXXdKQ9SV3ZjqYIibs6H2FP6ok5-ARPcABcPt61L2aiEFtRd7f50fqwD46AnhJMhVk1FuJyK_8XMJRCxD7Fya_AWp34YjLKjPeGtLxaYnmO3aYYQKfuhqjrQCt_BlK1M-2rUAtmXzS3Q9v35YWnSrLPWRQyr1LH1lWOo9ErOg6GGPrYZ6nPcYPO3PRF9RC5EaeRvzke3dxkzOBH1V7Z0b0eRsTvZyTdZA0I0rngCa3sfhixncOmrR_IUYyzs8_2Vlsj7ghZsCmVgHYa7sQPP4Psew5QKV8N-J7E5kZemSdbxfinOZ4TjPbbjCWKeyzyIKoUracr-KU9IZ7LN-_ng-8cGkWqHCDYmz7IEEt7Cs-kRaxm38kiYIMVq7GZZxmlmm95rQaD8RBSUwfb_9aZR6a-wMD9JRobExfn4d9XDwKGBcCHpMLRcGPNpWm9BoZtc9FvZqy5wem-2AZQXIYFdyZJFCfSuzpqJDjAx1foJrIy3-Fb_rCUke0uCw3m1Z2BWz1rSVzc-ff6pwikMK2F0AQPFxrOn1J9FjJ7cleITlXxO0PA.ZSOGP7Mn3Y2QjurbmKS6SA"
            }
        }
    }
}
