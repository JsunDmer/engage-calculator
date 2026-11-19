pluginManagement {
    repositories {
        val props = java.util.Properties()
        val local = file("local.properties")
        if (local.exists()) local.inputStream().use { props.load(it) }
        props.getProperty("mirror.url")?.takeIf { it.isNotBlank() }?.let {
            maven {
                url = uri(it)
                isAllowInsecureProtocol = true
            }
        }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        val props = java.util.Properties()
        val local = file("local.properties")
        if (local.exists()) local.inputStream().use { props.load(it) }
        props.getProperty("mirror.url")?.takeIf { it.isNotBlank() }?.let {
            maven {
                url = uri(it)
                isAllowInsecureProtocol = true
            }
        }
        google()
        mavenCentral()
    }
}

rootProject.name = "EngageCalculator"
include(":app")
include(":core")