rootProject.name = "e-commerce"

include(
    ":apps:commerce-api",
    ":apps:order-api",
    ":apps:like-api",
    ":apps:pbo-api",
    ":apps:commerce-collector",
    ":apps:commerce-batch",
    ":apps:pg-simulator",
    ":modules:jpa",
    ":modules:redis",
    ":modules:feign",
    ":modules:kafka",
    ":supports:jackson",
    ":supports:logging",
    ":supports:monitoring",
)

// configurations
pluginManagement {
    val springBootVersion: String by settings
    val springDependencyManagementVersion: String by settings

    repositories {
        maven { url = uri("https://repo.spring.io/milestone") }
        maven { url = uri("https://repo.spring.io/snapshot") }
        gradlePluginPortal()
    }

    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "org.springframework.boot" -> useVersion(springBootVersion)
                "io.spring.dependency-management" -> useVersion(springDependencyManagementVersion)
            }
        }
    }
}
