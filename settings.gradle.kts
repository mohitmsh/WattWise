pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // MPAndroidChart, used for the report charts, is published on JitPack.
        maven("https://jitpack.io")
    }
}

rootProject.name = "WattWise"
include(":app")
