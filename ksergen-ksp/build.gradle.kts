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

val javadocJar = tasks.register<Jar>("dokkaJavadocJar") {
    dependsOn(tasks.dokkaJavadoc)
    from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

val sourceJar = tasks.register<Jar>("sourceJar") {
    from(sourceSets.main.get().allSource)
    archiveClassifier.set("sources")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "io.github.adriankhl"
            artifactId = "ksergen-ksp"
            version = libs.versions.ksergenVersion.get()

            from(components["kotlin"])

            artifact(javadocJar.get())
            artifact(sourceJar.get())

            pom {
                name.set("KSerGen")
                description.set("KSP code generation library for Kotlin serialization and immutable data class")
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

    repositories {
        maven {
            name = "OSSRH"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")

            val ossrhUserName: String? by project
            val ossrhPassword: String? by project
            credentials {
                username = ossrhUserName
                password = ossrhPassword
            }
        }
    }
}

signing {
    sign(publishing.publications["maven"])
}
