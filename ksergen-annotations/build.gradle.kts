plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    `maven-publish`
    signing
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
            groupId = "io.github.adriankhl"
            artifactId = "ksergen-annotations"
            version = libs.versions.ksergenVersion.get()

            from(components["kotlin"])
        }
    }
}
