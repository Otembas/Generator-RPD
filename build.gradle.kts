import io.gitlab.arturbosch.detekt.getSupportedKotlinVersion
import org.jetbrains.dokka.DokkaConfiguration.Visibility
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

val apachePoiVersion: String by project
val appVersion: String by project
val commonsLoggingVersion: String by project
val datafakerVersion: String by project
val detektVersion: String by project
val dokkaJacksonVersion: String by project
val jacocoVersion: String by project
val janinoVersion: String by project
val jxlsVersion: String by project
val kotlinVersion: String by project
val log4jVersion: String by project
val mockitoKotlinVersion: String by project
val xDocReportVersion: String by project

plugins {
    id("org.springframework.boot") version "3.4.4"
    id("io.spring.dependency-management") version "1.1.7"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    id("jacoco")
    id("org.jetbrains.dokka") version "2.0.0"
    id("org.barfuin.gradle.jacocolog") version "3.1.0"
    kotlin("jvm")
    kotlin("plugin.spring")
}

group = "ru.redactor"
version = appVersion
val sourceCompatibility = JvmTarget.JVM_21

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
    implementation("org.apache.poi:poi-ooxml:$apachePoiVersion")
    implementation("fr.opensagres.xdocreport:fr.opensagres.xdocreport.template.velocity:$xDocReportVersion")
    implementation("fr.opensagres.xdocreport:fr.opensagres.xdocreport.document.docx:$xDocReportVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.codehaus.janino:janino:$janinoVersion")
    implementation("net.datafaker:datafaker:$datafakerVersion")
    testImplementation("org.mockito.kotlin:mockito-kotlin:$mockitoKotlinVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:$detektVersion")
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-rules-libraries:$detektVersion")
}

kotlin {
    compilerOptions {
        apiVersion.set(KotlinVersion.KOTLIN_2_1)
        languageVersion.set(KotlinVersion.KOTLIN_2_1)
        jvmToolchain(sourceCompatibility.target.toInt())
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

configurations.matching { it.name.startsWith("dokka") }.configureEach {
    resolutionStrategy.eachDependency {
        if (requested.name.startsWith("com.fasterxml.jackson")) {
            useVersion(dokkaJacksonVersion)
        }
    }
}

sourceSets {
    main {
        resources {
            srcDir("config/${rootProject.name}")
        }
    }
}

tasks.withType<Jar> {
    manifest {
        attributes(
            "Implementation-Title" to rootProject.name,
            "Implementation-Version" to appVersion
        )
    }
}

tasks.named("jar") {
    enabled = false
}

configurations["detekt"].resolutionStrategy.eachDependency {
    if (requested.group == "org.jetbrains.kotlin") {
        useVersion(getSupportedKotlinVersion())
    }
}

detekt {
    config.setFrom(layout.projectDirectory.file("config/detekt.yml"))
    buildUponDefaultConfig = true
}

tasks.detekt {
    reports {
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.file("reports/detekt/index.html"))
        xml.required.set(true)
        xml.outputLocation.set(layout.buildDirectory.file("reports/detekt/detekt.xml"))
        txt.required.set(false)
        sarif.required.set(false)
    }
    jvmTarget = sourceCompatibility.target
}

jacoco {
    toolVersion = jacocoVersion
    reportsDirectory.set(layout.buildDirectory.dir("reports/coverage"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.jacocoTestReport {
    reports {
        csv.required.set(false)
        xml.required.set(true)
        xml.outputLocation.set(layout.buildDirectory.file("reports/coverage/coverage.xml"))
        html.outputLocation.set(layout.buildDirectory.dir("reports/coverage/html"))
    }
}

val coverage by tasks.registering {
    group = "verification"
    description = "Runs the unit tests with coverage."

    dependsOn(":test", ":jacocoTestReport", ":jacocoTestCoverageVerification")
    val jacocoTestReport = tasks.findByName("jacocoTestReport")
    jacocoTestReport?.mustRunAfter(tasks.findByName("test"))
    tasks.findByName("jacocoTestCoverageVerification")?.mustRunAfter(jacocoTestReport)
}

tasks.dokkaHtml.configure {
    outputDirectory.set(layout.buildDirectory.dir("reports/docs"))
    dokkaSourceSets {
        configureEach {
            documentedVisibilities.set(
                setOf(
                    Visibility.PUBLIC,
                    Visibility.PRIVATE,
                    Visibility.PROTECTED,
                    Visibility.INTERNAL,
                    Visibility.PACKAGE

                )
            )
            skipDeprecated.set(false)
        }
    }
}
