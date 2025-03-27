plugins {
    kotlin("jvm") version "2.1.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "fr.xamez.ffaction.FFaction"
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
    implementation("com.zaxxer:HikariCP:6.3.0")
    implementation("com.mysql:mysql-connector-j:9.2.0")
    implementation("org.xerial:sqlite-jdbc:3.42.0.0")
}

val targetJavaVersion = 23
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.processResources {
    val props = mapOf(
        "version" to version
    )
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.shadowJar {
    dependsOn("processResources")
    archiveClassifier.set("")

    outputs.upToDateWhen { false }

    doLast {
        val foliaPluginsDir = layout.buildDirectory.dir("../run/plugins").get().asFile
        foliaPluginsDir.mkdirs()
        println("Copying ${archiveFile.get().asFile.path} to ${foliaPluginsDir.path}")
        copy {
            from(archiveFile)
            into(foliaPluginsDir)
        }
    }
}

tasks.runServer {
    minecraftVersion("1.21.4")
}

runPaper.folia.registerTask()
