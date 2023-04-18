plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("org.jetbrains.dokka")
    id("com.google.devtools.ksp")
}

dependencies {
    ksp(project(":ksergen-ksp"))
    implementation(project(":ksergen-annotations"))
    implementation(libs.kotlinx.serialization.json)
    testImplementation(kotlin("test"))
}
