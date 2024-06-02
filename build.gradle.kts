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
            version = "0.4.6"
        }
    }
    repositories {
        maven {
            url = uri("https://hitcentral-590183692348.d.codeartifact.ap-south-1.amazonaws.com/maven/hitcentral/")
            credentials {
                username = "aws"
                // if getting 401, run
                // aws codeartifact get-authorization-token --domain hitcentral --domain-owner 590183692348 --region ap-south-1 --query authorizationToken --output text
                password = "eyJ2ZXIiOjEsImlzdSI6MTcxNzM0NjUxNiwiZW5jIjoiQTEyOEdDTSIsInRhZyI6IlYyYmRVSjNFR3FuS1NoYlpfeVFtcnciLCJleHAiOjE3MTczODk3MTYsImFsZyI6IkExMjhHQ01LVyIsIml2IjoidGRfb2lydF95dTQzRjRaeSJ9.nmkY0kCcV_H9UNGIdC319g._4wU-gQOcoTqy1Do.NVdWHaTnNeQ77OUeqiCKhug08FDALl3MUeVcl2BA83Z5GNIOdO-Ko1lITAjPZNuCVPi-DcBQJNuLvWpsjOX7rKVVirXnc-E0E2OrHqnHOjwXZaRiiQyFN0F0IqahjuE0kdsK26cre2UPVIo3flblJ3w1d4gAU0cJtcWM86trpihrnaoMm_Gwapw_bt0KDGLXPb1_IHP3KEcl6e0jVmb7FM0Mlx7ZxdHxIIa51gRXr2K7hVOhvREqAWu1AuLW-Z5mqQLWcW2U8IrDJq2CL-S6jLa7DYqe7X_1yNrsD5lphPruE1QORCMR6Rna_1bmXRl79JbLIvxo-V1JMVuum1_l0lBBTLo4a3d99u_pnWpEBb0lC8T07nqdi0OZtQG0u4MgLMHz0bK1NoxlClObH0u5XFG4VGkrT0TEHbOvK70Isrl5pk_WT6yyjiqqdOr_r8h2BVC-KsxZYVBCKWkJ5eZxgzINZwWRGY7GOPOcdwCIdPNSICnvVLkUaYppMMbvDltJkBU_C256EClwF7kTap8innh-w8ZzVhTwAiru9EuJkD9QpD6L9XPL43IOoLG5xGKa908bX6OB1ItN2igErAssdoI4LnQG8FuF97lKmKzuZh0EAT9oH4mdpEN5BEicRAn17EGamuM97bc345Kd9oK277nH0gg0fY2ipNKUsa7FEShnPDu_Gxk5au79BcM1wgtoW6Xjq3lCJocBVkYW_azbJVHW5tyba6WyKou0nyEMgCMoa_Vni9qdGbWLDwOlFObzp-h1uDLkLkmoXqAbzVoZww286tDbNqq9BtIQ1FlBySX3xOeN6mixqtVRRXyeHGgu1uUL9WsX1V5ePl-6xlickxnlYvhka1PAIXqWDRgn3Ou5MdjYrDeTjWBwQcFKTZ6vkNmrZl9a9xCC2IbkqdmwxvjNRgXi.MtQLlHA_1Gjubg3TDI7nOg"
            }
        }
    }
}
