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
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.6.0")
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
            // if getting 409, bump the version
            version = "0.4.2"
        }
    }
    repositories {
        maven {
            url = uri("https://hitcentral-590183692348.d.codeartifact.ap-south-1.amazonaws.com/maven/hitcentral/")
            credentials {
                username = "aws"
                // if getting 401, run
                // aws codeartifact get-authorization-token --domain hitcentral --domain-owner 590183692348 --region ap-south-1 --query authorizationToken --output text
                password = "eyJ2ZXIiOjEsImlzdSI6MTcxNzAxMDgxMSwiZW5jIjoiQTEyOEdDTSIsInRhZyI6IlBFT2tuXzd2Q2ZGVVZWYmxQaGwwTVEiLCJleHAiOjE3MTcwNTQwMTEsImFsZyI6IkExMjhHQ01LVyIsIml2IjoidkJXR2FXRDNnWndybHZxRSJ9.Gj1STXmUObp49_gUveJLZg.zQlgwnAiGhzUi7D8.JoXCOLGS98WOG1cMtvQ-d76r2c8mo2466Ea8Ij6dpXkOa1Yh36tHIftqekOjMfCCArt3bzaviqVOWfwu5vg48YHOLJll_BWj0YRh8HSfve1oyQmoFKVpUNL72kc2LL8s_zwrUbBZ6w_oLRNBWh_5ozP01esIRbxULzWGfUB01XJR2nxqCrOoO7zkLpm8T_JKpcXbIR1ufWD8jYisvx3OeSfJ8dXqlBeDOokQHDRzDgBDgtNEJtpBFiLAaOOZIxrpZfipUcw8FYcKbK1ed4PcfN4Zpa9RKAhFbdMTmPtYPHBwQqBoIASwgbIpE7DUKdTBzhgmNZpzYPzxmc1Azr3BtQ8N2xuAYOE4fXuZhdihN-XDWfaMzhn_WIhJS_k2C3UrLU_ovob0hlU0UIjoh_8FrNpPYNQxBrwab_KXPUwkA8NdWI59RJo4yHt0UcKmDb9L04p7g_gektxr_zvMhuIDKi1KjwkfiA9iAU7NVTlrKNGSaVjG1bw2tC5SQ2JyRld8-aLrTE5FdjATNxPt_JNNeTU34TaYoWvThU0cZvN3d8yOjCdRiWU88bSs3YLfAo3dIEcLWknX6d62RrjAzc78IumqPEkoE3DW1hRnrgBRkm1dnP2B7d-6byk7Ul6WUn8X4mz3-_ruOfw6HbAGTFwJ_Z9KsAgZWqOeH0sxY0vp-GF9yuFAj-cIbmwNZ8uDjHEhVfqEXDmh_mvsr5VABtW4ZA5mj3K9gI5xH7W-UJO5lq1cSijTIwgYHoDptdWcF9JwsyDnbcAYUZ8_nSeAg5EBhk6m9aSl7qxMcUzVUAENeKsBFGgHM9Gx9r2gVWiGxLwNYHacLgr937lGyMHfgpiEPeH0g_lSVJAjkchV1urB_dHdR9jRmnlcBRwRzP_kX5o8qPHytWXZyzPNsvQUNdXQxkCpbbO86s4YJVnaGl3qeqa-h6Cfm-RCsZ2w.7vFVJ2LBj6wu7MT7AHmjqg"
            }
        }
    }
}
