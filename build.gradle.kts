import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("io.github.goooler.shadow") version("8.1.7")
}

group = "net.kulorised"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.serble.net/snapshots")
}

dependencies {
    implementation("net.minestom:minestom-snapshots:461c56e749")
    implementation("net.mangolise:mango-game-sdk:latest")

    // https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
    implementation("ch.qos.logback:logback-classic:1.5.6")
}

tasks.withType<Jar> {
    manifest {
        // Change this to your main class
        attributes["Main-Class"] = "net.mangolise.kulorised.Test"
    }
}

tasks.withType<ShadowJar> {
    minimize {
        exclude(dependency("com.github.ben-manes.caffeine:caffeine:.*"))
    }
}
