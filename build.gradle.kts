plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.3"
}

group = "com.alex.alekseev"
version = "2.0.0"

repositories {
    mavenCentral()
}

intellij {
    version.set("2023.2.6")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf(/* Plugin Dependencies */))
}

sourceSets {
    main {
        java.srcDir("src/main/java")
        resources.srcDir("src/main/resources")
    }
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    patchPluginXml {
        sinceBuild.set("193")  // Supports IntelliJ 2019.3 and later
        untilBuild.set("") // Unlimited
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }

    buildSearchableOptions {
        enabled = false
    }

    processResources {
        includeEmptyDirs = false
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}
