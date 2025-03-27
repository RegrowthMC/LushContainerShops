plugins {
    `java-library`
    `maven-publish`
    id("com.gradleup.shadow") version("8.3.0")
    id("xyz.jpenilla.run-paper") version("2.2.4")
}

group = "org.lushplugins"
version = "1.0.0"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.papermc.io/repository/maven-public/") // Paper
    maven("https://repo.lushplugins.org/snapshots/") // LushLib
    maven("https://repo.codemc.io/repository/maven-releases/") // PacketEvents
    maven("https://maven.evokegames.gg/snapshots") // EntityLib
}

dependencies {
    // Dependencies
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")

    // Soft Dependencies
    compileOnly("com.github.retrooper:packetevents-spigot:2.7.0")

    // Libraries
    implementation("org.lushplugins:LushLib:0.10.59")
    implementation("io.github.revxrsal:lamp.common:4.0.0-rc.9")
    implementation("io.github.revxrsal:lamp.bukkit:4.0.0-rc.10")
    implementation("me.tofaa.entitylib:spigot:+e181b97-SNAPSHOT")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))

    registerFeature("optional") {
        usingSourceSet(sourceSets["main"])
    }

    withSourcesJar()
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"

        // Preserve parameter names in the bytecode (required by Lamp)
        options.compilerArgs.add("-parameters")
    }

    shadowJar {
        relocate("org.lushplugins.lushlib", "org.lushplugins.lushcontainershops.libs.lushlib")
        relocate("revxrsal", "org.lushplugins.lushcontainershops.libs.revxrsal")

        archiveFileName.set("${project.name}-${project.version}.jar")
    }

    processResources{
        filesMatching("plugin.yml") {
            expand(project.properties)
        }

        inputs.property("version", rootProject.version)
        filesMatching("plugin.yml") {
            expand("version" to rootProject.version)
        }
    }

    runServer {
        minecraftVersion("1.21.1")

        downloadPlugins {
            modrinth("packetevents", "2.7.0")
        }
    }
}

publishing {
    repositories {
        maven {
            name = "lushReleases"
            url = uri("https://repo.lushplugins.org/releases")
            credentials(PasswordCredentials::class)
            authentication {
                isAllowInsecureProtocol = true
                create<BasicAuthentication>("basic")
            }
        }

        maven {
            name = "lushSnapshots"
            url = uri("https://repo.lushplugins.org/snapshots")
            credentials(PasswordCredentials::class)
            authentication {
                isAllowInsecureProtocol = true
                create<BasicAuthentication>("basic")
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            groupId = rootProject.group.toString()
            artifactId = rootProject.name
            version = rootProject.version.toString()
            from(project.components["java"])
        }
    }
}
