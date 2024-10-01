plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
}

group = "com.ecommercedemo"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.0")
    implementation("com.google.guava:guava:33.2.1-jre")
    implementation("io.jsonwebtoken:jjwt:0.9.1")
    implementation("jakarta.persistence:jakarta.persistence-api:3.0.0")
    implementation("jakarta.servlet:jakarta.servlet-api:5.0.0")
    // This dependency is used internally, and not exposed to consumers on their own compile classpath.
    implementation(libs.guava)
    testImplementation(libs.junit.jupiter)
    api(libs.commons.math3)
    implementation("org.apache.commons:commons-math3:3.6.1")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("org.springframework.boot:spring-boot-starter-security:3.3.4")
    implementation("org.springframework.boot:spring-boot-starter-web:3.3.4")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
