import com.github.benmanes.gradle.versions.reporter.PlainTextReporter
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.dokka)
    alias(libs.plugins.ben.manes.versions)
    alias(libs.plugins.ksp)
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
    }
}


tasks.withType<DependencyUpdatesTask> {
    gradleReleaseChannel = "current"

    val showUnresolved: Boolean = project.hasProperty("showUnresolved")

    fun isNonStable(version: String): Boolean {
        return listOf(
            "-alpha",
            "-beta",
            "-dev",
            "-rc",
        ).any {
            version.lowercase().contains(it)
        }
    }

    outputFormatter {
        if (!showUnresolved) {
            unresolved.dependencies.clear()
        }

        // temporary fix for: https://github.com/ben-manes/gradle-versions-plugin/issues/733
        outdated.dependencies.removeAll { isNonStable(it.available.milestone.orEmpty()) }

        val plainTextReporter = PlainTextReporter(
            project,
            revision,
            gradleReleaseChannel
        )
        plainTextReporter.write(System.out, this)
    }

    rejectVersionIf {
        isNonStable(candidate.version)
    }
}
