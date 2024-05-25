plugins {
    kotlin("jvm") version "1.9.23"
    id("org.springframework.boot") version "2.7.6"
    kotlin("plugin.spring") version "1.5.31" 
    id('maven-publish')
}

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

publishing {
  publications {
      mavenJava(MavenPublication) {
            groupId = 'ai.hit11'
            artifactId = 'zeus'
            version = '0.1.0'
            from components.java
      }
  }
  repositories {
      maven {
            url 'https://hitcentral-590183692348.d.codeartifact.ap-south-1.amazonaws.com/maven/hitcentral/'
            credentials {
                username "aws"
                password System.env.CODEARTIFACT_AUTH_TOKEN
          }
      }
  }
}