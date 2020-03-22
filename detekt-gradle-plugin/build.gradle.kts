import com.jfrog.bintray.gradle.BintrayExtension
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import java.util.Date

repositories {
    jcenter()
    maven { setUrl("https://oss.jfrog.org/artifactory/oss-snapshot-local") }
}

plugins {
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "0.11.0"
}

group = "io.gitlab.arturbosch.detekt"
version = "1.7.0"

val spekVersion = "2.0.9"
val junitPlatformVersion = "1.6.0"
val assertjVersion = "3.15.0"

dependencies {
    implementation(kotlin("gradle-plugin"))
    implementation(kotlin("gradle-plugin-api"))

    testImplementation(project(":detekt-test-utils"))
    testImplementation("org.assertj:assertj-core:$assertjVersion")
    testImplementation("org.spekframework.spek2:spek-dsl-jvm:$spekVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:$junitPlatformVersion")
    testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:$spekVersion")

    detekt("io.gitlab.arturbosch.detekt:detekt-api:1.7.0-20200322.191513-8")
    detekt("io.gitlab.arturbosch.detekt:detekt-core:1.7.0-20200322.191513-8")
    detekt("io.gitlab.arturbosch.detekt:detekt-cli:1.7.0-20200322.191601-8")
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.7.0-20200322.191515-8")
}

gradlePlugin {
    plugins {
        register("detektPlugin") {
            id = "io.gitlab.arturbosch.detekt"
            implementationClass = "io.gitlab.arturbosch.detekt.DetektPlugin"
        }
    }
}

tasks.validatePlugins {
    enableStricterValidation.set(true)
}

pluginBundle {
    website = "https://arturbosch.github.io/detekt"
    vcsUrl = "https://github.com/detekt/detekt"
    description = "Static code analysis for Kotlin"
    tags = listOf("kotlin", "detekt", "code-analysis", "linter", "codesmells")

    (plugins) {
        "detektPlugin" {
            id = "io.gitlab.arturbosch.detekt"
            displayName = "Static code analysis for Kotlin"
        }
    }
}

val generateDefaultDetektVersionFile by tasks.registering {
    val name = "PluginVersion.kt"
    val defaultDetektVersionFile = File("$buildDir/generated/src/io/gitlab/arturbosch/detekt", name)

    inputs.property(name, project.version)
    outputs.file(defaultDetektVersionFile)

    doFirst {
        defaultDetektVersionFile.parentFile.mkdirs()
        defaultDetektVersionFile.writeText("""
            package io.gitlab.arturbosch.detekt

            internal const val DEFAULT_DETEKT_VERSION = "$version"

            """.trimIndent()
        )
    }
}

sourceSets.main.get().java.srcDir("$buildDir/generated/src")

tasks.compileKotlin {
    dependsOn(generateDefaultDetektVersionFile)
}
