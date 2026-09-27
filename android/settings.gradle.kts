pluginManagement {
    includeBuild("build-logic")
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
    }
}

rootProject.name = "mahin-android"

include(":app")
include(":core:common")
include(":core:model")
include(":core:datetime")
include(":core:designsystem")
include(":core:security")
include(":core:analytics")
include(":core:config")
include(":core:media")
include(":core:network")
include(":core:database")
include(":core:datastore")
include(":core:sync")
include(":core:content")
include(":core:notifications")
include(":core:testing")
include(":domain:cycle")
include(":domain:fertility")
include(":domain:pregnancy")
include(":domain:content")
include(":domain:account")
include(":domain:subscription")
include(":domain:reminders")
