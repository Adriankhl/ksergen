plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
    `maven-publish`
    signing
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
            groupId = "io.github.adriankhl"
            artifactId = "ksergen-ksp"
            version = libs.versions.ksergenVersion.get()

            from(components["kotlin"])

            pom {
                name.set("KSerGen")
                description.set("Code generation library for Kotlin serialization and immutable data class")
                url.set("https://github.com/Adriankhl/ksergen/")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://spdx.org/licenses/MIT.html")
                    }
                }
                developers {
                    developer {
                        id.set("adriankhl")
                        name.set("Lai Kwun Hang")
                        email.set("adrian.k.h.lai@outlook.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com:Adriankhl/ksergen.git")
                    developerConnection.set("scm:git:ssh://git@github.com:Adriankhl/ksergen.git")
                    url.set("https://github.com/Adriankhl/ksergen/")
                }
            }
        }
    }
}
