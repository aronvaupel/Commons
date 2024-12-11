
plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
    `maven-publish`
}

group = "com.github.aronvaupel"
version = "4.0.9test5"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.module:jackson-module-jakarta-xmlbind-annotations:2.18.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.2")
    implementation("com.google.guava:guava:33.2.1-jre")
    implementation ("io.github.cdimascio:dotenv-kotlin:6.2.2")
    implementation("io.hypersistence:hypersistence-utils-hibernate-63:3.8.3")
    implementation("io.jsonwebtoken:jjwt:0.9.1")
    implementation("jakarta.servlet:jakarta.servlet-api:5.0.0")
    implementation ("jakarta.validation:jakarta.validation-api:3.0.2")
    implementation("javax.xml.bind:jaxb-api:2.3.1")
    // This dependency is used internally, and not exposed to consumers on their own compile classpath.
    implementation(libs.guava)
    testImplementation(libs.junit.jupiter)
    api(libs.commons.math3)
    implementation("org.apache.commons:commons-math3:3.6.1")
    implementation("org.glassfish.jaxb:jaxb-runtime:2.3.1")
    implementation("org.hibernate:hibernate-core:6.6.1.Final")
    implementation ("org.hibernate.validator:hibernate-validator:8.0.0.Final")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    //Fixme
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.1.5")
    //Fixme
    implementation("org.springframework.boot:spring-boot-starter-data-redis:3.3.4")
    //Fixme
    implementation("org.springframework.boot:spring-boot-starter-security:3.3.4")
    implementation("org.springframework.boot:spring-boot-starter-web:3.3.4")
    implementation("org.springframework.boot:spring-boot-starter-validation:3.3.4")
    implementation("org.springframework.kafka:spring-kafka:3.2.4")
    implementation("org.springframework:spring-orm:6.1.13")
    implementation("org.springframework:spring-tx:6.1.13")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

fun loadEnv(): Map<String, String> {
    val envFile = file("${rootProject.projectDir}/.env")
    if (!envFile.exists()) {
        throw GradleException(".env file not found")
    }

    return envFile.readLines()
        .filter { it.isNotBlank() && !it.startsWith("#") }
        .map { it.split("=", limit = 2) }
        .associate { it[0] to it.getOrElse(1) { "" } }
}

publishing {
    publications {
        create<MavenPublication>("gpr") {
            from(components["java"])
            groupId = "com.github.aronvaupel"
            artifactId = "commons"
            version = project.version.toString()
        }
    }
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/aronvaupel/Commons")
            credentials {
                val env = loadEnv()
                username = env["GITHUB_USERNAME"] ?: ""
                password = env["GITHUB_TOKEN"] ?: ""
            }
        }
    }
}
