
import org.gradle.kotlin.dsl.*
import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.plugin.SpringBootPlugin.*
import org.gradle.api.tasks.wrapper.Wrapper.DistributionType.BIN

val springCloudVersion by extra { "2020.0.1" }
val kotlinVersion = "1.4.21"

buildscript {
    repositories {
        mavenCentral()
        maven("https://repo.spring.io/snapshot")
        maven("https://repo.spring.io/milestone")
    }
}

plugins {
    idea
    val kotlinVersion = "1.4.21"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    id("org.springframework.boot") version "2.4.2"
    jacoco
}
apply {
    plugin("io.spring.dependency-management")
}

group = "com.spring.reproducer"
version = "0.0.1-SNAPSHOT"

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

repositories {
    mavenCentral()
    jcenter()
    maven("https://repo.spring.io/snapshot")
    maven("https://repo.spring.io/milestone")
}

the<DependencyManagementExtension>().apply {
    imports {
        mavenBom(BOM_COORDINATES) { bomProperty("kotlin.version", kotlinVersion) }
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
    }
}

dependencies {
    implementation("net.dv8tion:JDA:4.2.0_227")
    implementation("org.hibernate:hibernate-validator:7.0.1.Final")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.cloud:spring-cloud-starter-sleuth")
    implementation("io.opentracing.brave:brave-opentracing") { exclude(module = "brave-tests") }
    implementation("io.zipkin.brave:brave")
    implementation("org.mongodb:mongodb-driver:3.12.7")
    implementation("org.mongodb:mongodb-driver-core:3.12.7")
    implementation("org.mongodb:bson:3.12.7")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.data:spring-data-commons")
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("io.rest-assured:spring-web-test-client")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.mockito:mockito-junit-jupiter")
    testImplementation("org.assertj:assertj-core:3.11.1")
    testImplementation("io.mockk:mockk:1.10.6")
}

tasks.withType<Test> { useJUnitPlatform() }

tasks.withType<Wrapper>().configureEach {
    gradleVersion = "6.8.1"
    distributionType = BIN
}