plugins {
    kotlin("jvm") version "2.1.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "fr.xamez.FFaction"
version = project.properties["version"] as String

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
}

dependencies {
    compileOnly("dev.folia:folia-api:1.21.4-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.shadowJar {
    archiveClassifier.set("")
    doLast {
        val foliaPluginsDir = layout.buildDirectory.dir("../run/plugins").get().asFile
        foliaPluginsDir.mkdirs()
        copy {
            from(archiveFile)
            into(foliaPluginsDir)
        }
    }
}

tasks.register<Jar>("devJar") {
    group = "run"
    dependsOn("classes")
    archiveClassifier.set("dev")
    from(sourceSets.main.get().output)

    doLast {
        val foliaPluginsDir = layout.buildDirectory.dir("../run/plugins").get().asFile
        foliaPluginsDir.mkdirs()
        copy {
            from(archiveFile)
            into(foliaPluginsDir)
        }
    }
}

tasks.named("runServer") {
    dependsOn("devJar")
}

tasks.withType(xyz.jpenilla.runtask.task.AbstractRun::class) {
    javaLauncher = javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    }
    jvmArgs(
        "-XX:+AllowEnhancedClassRedefinition",
        "-XX:HotswapAgent=fatjar",
    )
}

tasks.runServer {
    minecraftVersion("1.21.4")
}

runPaper.folia.registerTask()

tasks.named("cleanFoliaCache") {
    group = "run"
}

tasks.named("runFolia") {
    group = "run"
    dependsOn("devJar")
}
