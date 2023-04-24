plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("org.jetbrains.dokka")
    id("maven-publish")
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    testImplementation(kotlin("test"))
}

tasks {
    test {
        useJUnitPlatform()
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.adriankhl.ksergen"
            artifactId = "ksergen-annotations"
            version = libs.versions.ksergenVersion.get()

            from(components["kotlin"])
        }
    }
}
