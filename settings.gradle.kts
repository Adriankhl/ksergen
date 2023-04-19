rootProject.name = "ksergen"

include(
    "ksergen-annotations",
    "ksergen-ksp",
    "ksergen-test",
    "ksergen-test-dependency",
)

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
}
