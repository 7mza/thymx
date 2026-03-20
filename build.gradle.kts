import com.github.gradle.node.npm.task.NpmTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    kotlin("jvm") version "2.3.20"
    kotlin("plugin.jpa") version "2.3.20"
    kotlin("plugin.spring") version "2.3.20"
    id("org.springframework.boot") version "4.0.4"
    id("io.spring.dependency-management") version "1.1.7"

    id("com.github.ben-manes.versions") version "0.53.0"
    id("com.github.node-gradle.node") version "7.1.0"
    id("com.google.cloud.tools.jib") version "3.5.3"
    // id("org.graalvm.buildtools.native") version "0.11.5"
    id("org.hibernate.orm") version "7.2.7.Final"
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
    jacoco
}

group = "com.hamza"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

val dataFakerVersion = "2.5.4"
val htmxThymeleafVersion = "5.1.0"
val hypersistenceTsidVersion = "2.1.4"
val mockitoAgent: Configuration = configurations.create("mockitoAgent")
val mockitoCoreVersion = "5.23.0"
val mockitoKotlinVersion = "6.2.3"
val openapiVersion = "3.0.2"
val thymeleafExtraVersion = "3.1.3.RELEASE"
val thymeleafLayoutDialectVersion = "4.0.0"

dependencies {
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")

    implementation("io.github.wimdeblauwe:htmx-spring-boot-thymeleaf:$htmxThymeleafVersion")
    implementation("io.hypersistence:hypersistence-tsid:$hypersistenceTsidVersion")
    implementation("net.datafaker:datafaker:$dataFakerVersion")
    implementation("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:$thymeleafLayoutDialectVersion")
    implementation("org.eclipse.jetty.http2:jetty-http2-server")
    implementation("org.ehcache:ehcache::jakarta")
    implementation("org.hibernate.orm:hibernate-jcache")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$openapiVersion")
    implementation("org.springframework.boot:spring-boot-h2console")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    // implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-jetty")
    implementation("org.springframework.boot:spring-boot-starter-liquibase")
    // implementation("org.springframework.boot:spring-boot-starter-restclient")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-session-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webmvc") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
    }
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6:$thymeleafExtraVersion")
    implementation("tools.jackson.module:jackson-module-kotlin")

    mockitoAgent("org.mockito:mockito-core:$mockitoCoreVersion") { isTransitive = false }

    runtimeOnly("com.h2database:h2")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.htmlunit:htmlunit")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.mockito.kotlin:mockito-kotlin:$mockitoKotlinVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-liquibase-test")
    // testImplementation("org.springframework.boot:spring-boot-starter-restclient-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-session-jdbc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-thymeleaf-test")
    testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    // testImplementation("org.springframework.boot:spring-boot-starter-cache-test")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = Charsets.UTF_8.name()
        options.isFork = true
        options.isIncremental = true
    }

    withType<Test>().configureEach {
        useJUnitPlatform()
        jvmArgs("-javaagent:${mockitoAgent.asPath}")
        maxParallelForks = 2
        // forkEvery = 100
        reports {
            html.required = false
            junitXml.required = false
        }
        testLogging {
            events = setOf(FAILED)
            exceptionFormat = FULL
            showCauses = true
            showExceptions = true
            showStackTraces = true
            showStandardStreams = false
        }
        finalizedBy(jacocoTestReport)
        extensions.configure<JacocoTaskExtension> {
            excludes = listOf("org/htmlunit/**", "jdk.internal.*")
            isIncludeNoLocationClasses = true
        }
    }

    jacocoTestReport {
        dependsOn(test)
        classDirectories.setFrom(
            files(
                classDirectories.files.map {
                    fileTree(it) {
                        exclude("**/ApplicationKt.class")
                    }
                },
            ),
        )
        reports {
            csv.required = false
            html.outputLocation = layout.buildDirectory.dir("jacocoHtml")
            xml.required = false
        }
    }

    processResources { dependsOn("npm_run_build") }

    register("npm_run_build", NpmTask::class) {
        val isDev = project.hasProperty("mode") && project.property("mode") == "development"
        inputs.files(
            fileTree("$projectDir/src/main/resources/static/ts"),
            fileTree("$projectDir/src/main/resources/static/scss"),
        )
        outputs.files(fileTree("$projectDir/src/main/resources/static/dist"))
        args = listOf("run", if (isDev) "build:dev" else "build")
    }

    jar { enabled = false }

    jibDockerBuild { dependsOn(build) }

    // withType<ProcessAot>().configureEach { enabled = project.hasProperty("aot") }

    // withType<ProcessTestAot>().configureEach { enabled = project.hasProperty("aot") }
}

configure<KtlintExtension> {
    android.set(false)
    coloredOutput.set(true)
    debug.set(true)
    verbose.set(true)
    version.set("1.8.0")
}

node { download = true }

jib {
    from { image = "eclipse-temurin:25-jre-alpine" }
    to { tags = setOf("latest") }
    container { ports = listOf("80") }
}

hibernate { enhancement {} }

springBoot { buildInfo() }
