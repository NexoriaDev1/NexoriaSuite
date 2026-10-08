plugins {
    `java-library`
}

allprojects {
    group = "dev.nexoria"
    version = "1.0.0"

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://oss.sonatype.org/content/groups/public/")
        maven("https://jitpack.io")
    }
}

subprojects {
    apply(plugin = "java-library")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }
    }

    dependencies {
        // Modern Paper & Folia API
        compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
        compileOnly("net.kyori:adventure-text-minimessage:4.17.0")
        compileOnly("net.kyori:adventure-api:4.17.0")

        testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        // Target modern Java 25 features (records, pattern matching, virtual threads)
        options.compilerArgs.addAll(listOf("-Xlint:all", "-parameters"))
    }

    tasks.withType<Jar> {
        archiveBaseName.set("Nexoria-" + project.name.removePrefix("nexoria-").replaceFirstChar { it.uppercase() })
        archiveVersion.set(project.version.toString())
    }
}
