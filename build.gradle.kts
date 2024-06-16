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
    implementation("org.springframework.boot:spring-boot-starter-validation:2.7.6")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:2.7.6")
    implementation("mysql:mysql-connector-java:8.0.26")
    implementation("com.google.firebase:firebase-admin:8.1.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
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
            version = "0.4.10"
        }
    }
    repositories {
        maven {
            url = uri("https://hitcentral-590183692348.d.codeartifact.ap-south-1.amazonaws.com/maven/hitcentral/")
            credentials {
                username = "aws"
                // if getting 401, run
                // aws codeartifact get-authorization-token --domain hitcentral --domain-owner 590183692348 --region ap-south-1 --query authorizationToken --output text
                password = "eyJ2ZXIiOjEsImlzdSI6MTcxNzU5Nzg1OCwiZW5jIjoiQTEyOEdDTSIsInRhZyI6IklIbVpvQTV4UEV3cU5QdHFEZi0wYVEiLCJleHAiOjE3MTc2NDEwNTgsImFsZyI6IkExMjhHQ01LVyIsIml2IjoiMEloa25iVHQ5T3p2MkItaiJ9.E_0mUNFsI6pZBoWrDe0cQg.iBT68neJtNeQjmT6.F3Z8cqd5jeQfSsV4RQ0xM_mpC-GOx3DosFGGBBt0gqy2IDI7OR9NzWhwvgy779PIp5jvqMRo21QGNSUIokbTEVVS_F3ADd5Kgnr8lVCzekFCv9YCDbbzgvmgCn7F-ybEZX-RKPmFUyikv7YHtLON657BpNe7eeTp6Ms7pYnQglahXOLHfo5TzkHF88cOZBSW3aqQoB7OyP_vEg5dagOH9f4zF8v96P--RyTvrmAMPHEEaPHNrw8ioiUFPI9AGpyDhcCFdMNPAW2m2w0BUBJJbv3QBbP0dPjT2cgnJJkCd2Ch5P5Vd4ieE6cmSNAnIiKD9uI0KoqPg_FNBg8tMQFsmaEmPY7D46sQomcqKP4G_37U1qLRNXMN1TZFn3XAIOzjPaz5EXA_LmQZAgFxnajYsBBZHx945aNyyg21vGtWo7RRjnu8XlM-i7oJ9Ko2OWZNbCzN7CDEn4EWXlLGSDVb_F4ruuZ9NoRhM9jwWKS_r-aeUwxmKA2K40AWOG1M-20vHymPwJGeGGSQ1G_31krojV8EsJkoKlNjQ10QRCA5346OC7OF_KNN2EjReW-TI0tiv0vME9BQlWcXUT2IMsq66sCQ4BkSmojLB239ns4tG5XgxmHylOcgj7STKAXD_RhquE5WCMkAiOrhVu-9rBW9KAS7la92cAXvDRv7gdee3A-BishWZVhXqEO21EWkqadFM1NvWtozqUHfZZhd6ZRUcmaTkQQN-cqE-9X4CJvSXrIrMnRAEl2kFNEeCiCqfTCh-6RXxRwB7-Ue6M1AIurPIZ4w3IQwqhJDDk2fKX0-M6mb0U545mQfg-GwUmhcvcRW-XoEhoPtT5INjFRdmSFOErRUQfpKYqxGzi1QEN6y6lun4S4x4RRzAyLgHcxmnReb5yjvj0Nxtv-ocpG5oLapRmcqmoJkDQ.8oB7galwhGFUpTkS11qEmQ"
            }
        }
    }
}
