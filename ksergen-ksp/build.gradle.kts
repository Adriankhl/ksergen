plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
}

dependencies {
    implementation(project(":ksergen-annotations"))
    implementation(libs.ksp)
    testImplementation(kotlin("test"))
}