plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
    id("maven-publish")
}

dependencies {
    implementation(project(":ksergen-annotations"))
    implementation(libs.ksp)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinPoet)
    implementation(libs.kotlinPoet.ksp)
    testImplementation(kotlin("test"))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.adriankhl.ksergen"
            artifactId = "ksergen-ksp"
            version = libs.versions.ksergenVersion.get()

            from(components["kotlin"])
        }
    }
}
