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
            version = "0.6.2"
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
                    "eyJ2ZXIiOjEsImlzdSI6MTcyMjE2Nzc1MSwiZW5jIjoiQTEyOEdDTSIsInRhZyI6ImRPU05ZUENVU1JwZDRvNnIyclFMNUEiLCJleHAiOjE3MjIyMTA5NTEsImFsZyI6IkExMjhHQ01LVyIsIml2IjoiUjNqWEtaWUFNYm94cWlqOSJ9.TuGcKTf-T6wUdfIAH8GGfQ.DqiFF1tjKXPbFWve.CjYQwQ34JiUkyFhEZT8SDQhJzoi-agu11yb16JmR6Dvzu_NnS6xgAciB-s2CJ9i0cA-WS-Vb-TgUiC50-8IRF4lHNO7rpcYrbW2YGGWJnjW2RfQ81sSW-1_QW1XZFQjKYynd_jZji9NhYRrKA6NZzPT3NSaJbIAnaXXgubCuH80Miruz2UNqU5KYlu3UM6xN0D3huwzsWxm5la0AofSMuCB8Xz5Od65mNw0puh23K-hBxcdlM84nlO5fmagTNkLN3kvsrEC6SRFmx7TMmWOFLH7PH0aVGuzv4_pIhn7nPwarR9NKXQ3kGhXZ4lsQGiAEs_-WzlMvXUlEi9SE5vFy-MBmeC1cuKpuy5mBW2I-ZaYv4WqaaSDk2uah_5HJobxbRw_5GdarpcErZfT5ffD7xqhmb0TxjUPVOiP6ZNnVm-3pKqQueq6bISC7hsmCJ15tq72mkcTLdc4H2-nwFWviKr--oY8RYpj6PT4l5JxQ8n8uwMPXgy0RBy5VdPSTy0R70J-kN5y1hJa-hdmwTjDTOHPsECxVJ2LWp9cCjkVzyQbodgMgT2JPoPLHdp-NbZ9J8mY4Ywqvk_MO13BPDY_GldV8V2ZVT8VZPYeRbudZh9Fy_u72w08zEJoF9jVCZ5qJoNOUVQVPFlBl5EAoW5-WJoB4DSV23DxOTXIl4RP0FHbRRMatPgLZbwaCPRcg599SF_PFY8u_PMtiRPR5-CXM6NS1FyF1UKjOW_Ovw10JpJXOqK8HvP0Uk-b6ZMd_puLBVSdgpd96wpmy8GRYjZzGhxgYMCn1__rGzbnk-5_nzoIfvgieagSmwH5V5QNkNEJKduDIck8LrXfGkKqKxxJd_lsxdypRZcBOdamJdek-G1BkA1xeewTYSLyulFmthOKZae0X03VoCbDyBseUmK4brUh5El0DLD3iwqnZk58Spg9-IjJkljHa_rMEDhY1ZdILgQ7D2Sx59zKTleSc_RsC5Q.K-DWQ4TmSWRsrxmbeeC1ng"
            }
        }
    }
}
