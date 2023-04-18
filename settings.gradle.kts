rootProject.name = "ksergen"

include(
    "ksergen-annotations",
    "ksergen-ksp",
    "ksergen-test",
)

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
}
