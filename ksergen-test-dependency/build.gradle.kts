plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("org.jetbrains.dokka")
}

dependencies {
    implementation(project(":ksergen-annotations"))
    implementation(libs.kotlinx.serialization.json)
    testImplementation(kotlin("test"))
}
