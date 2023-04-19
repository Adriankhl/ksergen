plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
}

dependencies {
    implementation(project(":ksergen-annotations"))
    implementation(libs.ksp)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinPoet)
    implementation(libs.kotlinPoet.ksp)
    testImplementation(kotlin("test"))
}