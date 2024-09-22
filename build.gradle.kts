import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("maven-publish")
    id("io.github.goooler.shadow") version("8.1.7")
}

var versionStr = System.getenv("GIT_COMMIT") ?: "dev"

group = "net.mangolise"
version = versionStr

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://maven.serble.net/snapshots")
}

dependencies {
    implementation("net.minestom:minestom-snapshots:d0754f2a15")
    implementation("net.mangolise:mango-game-sdk:latest")

    // https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
    implementation("ch.qos.logback:logback-classic:1.5.6")
}

java {
    withSourcesJar()
}

publishing {
    repositories {
        maven {
            name = "serbleMaven"
            url = uri("https://maven.serble.net/snapshots/")
            credentials {
                username = System.getenv("SERBLE_REPO_USERNAME")?:""
                password = System.getenv("SERBLE_REPO_PASSWORD")?:""
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }

    publications {
        create<MavenPublication>("mavenGitCommit") {
            groupId = "net.mangolise"
            artifactId = "kulorised"
            version = versionStr
            from(components["java"])
        }

        create<MavenPublication>("mavenLatest") {
            groupId = "net.mangolise"
            artifactId = "kulorised"
            version = "latest"
            from(components["java"])
        }
    }
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

tasks.register("packageWorlds", net.mangolise.gamesdk.gradle.PackageWorldTask::class.java)
tasks.processResources {
    dependsOn("packageWorlds")
}
