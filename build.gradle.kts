plugins {
    // Minecraft 26.1+ is unobfuscated and therefore uses Loom's non-remapping plugin.
    id("net.fabricmc.fabric-loom") version "1.17.19"
    id("maven-publish")
}

version = project.property("mod_version") as String
group = project.property("maven_group") as String

base {
    archivesName.set(project.property("archives_base_name") as String)
}

repositories {
    mavenCentral()
    maven {
        name = "Nucleoid"
        url = uri("https://maven.nucleoid.xyz/")
    }
}

dependencies {
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    
    implementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
    implementation("org.yaml:snakeyaml:2.2")
    implementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_api_version")}")
    implementation("eu.pb4:sgui:2.1.0+26.2")
}

tasks.withType<ProcessResources>().configureEach {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
    options.encoding = "UTF-8"
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}
