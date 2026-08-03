import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import kr.entree.spigradle.data.Load
import kr.entree.spigradle.kotlin.spigot

plugins {
    id("com.github.johnrengelman.shadow")
    id("pex-platform")
    id("ca.stellardrift.localization")
    id("kr.entree.spigradle")
}

java {
    registerFeature("h2dbSupport") {
        usingSourceSet(sourceSets["main"])
    }
}

// spigradle bundles the dead papermc.io/repo host (now 403s) as one of its default repos;
// the working hosts have to be added explicitly for spigot-api's bungeecord-chat dependency,
// the spigot-api snapshot itself, and the two `shadow(...)` runtime-only plugin deps below.
repositories {
    maven("https://repo.papermc.io/repository/maven-public/") { name = "papermc" }
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") { name = "spigotmc" }
    maven("https://maven.enginehub.org/repo/") { name = "enginehub" }
    maven("https://nexus.hc.to/content/repositories/pub_releases/") { name = "vault" }
}

dependencies {
    val adventurePlatformVersion: String by project
    val cloudVersion: String by project
    val slf4jVersion: String by project
    val spigotVersion = "1.15.1-R0.1-SNAPSHOT"

    api(project(":impl-blocks:minecraft")) {
        exclude(group = "com.google.guava")
        exclude("org.yaml", "snakeyaml")
        exclude("com.google.code.gson", "gson")
    }

    implementation(project(":impl-blocks:hikari-config"))
    implementation("org.spongepowered:configurate-yaml") {
        exclude("org.yaml", "snakeyaml")
    }
    implementation("net.kyori:adventure-platform-bukkit:$adventurePlatformVersion") {
        exclude("com.google.code.gson")
    }
    implementation("cloud.commandframework:cloud-paper:$cloudVersion")
    implementation("org.slf4j:slf4j-jdk14:$slf4jVersion")

    // provided at runtime
    shadow(spigot(spigotVersion))
    shadow("net.milkbowl.vault:VaultAPI:1.7")
    shadow("com.sk89q.worldguard:worldguard-bukkit:7.0.4") {
        exclude(group = "org.bstats")
    }
    shadow("com.h2database:h2:1.4.200")
}

spigot {
    val pexDescription: String by project
    val pexSuffix: String by project
    name = rootProject.name
    version = "${project.version}$pexSuffix"
    description = pexDescription
    apiVersion = "1.13"
    load = Load.STARTUP
    softDepends("Vault", "WorldGuard")

    debug {
        buildVersion = "1.16.5"
    }
}

pexPlatform {
    relocate(
        "cloud.commandframework",
        "com.github.benmanes.caffeine",
        "com.typesafe.config",
        "com.zaxxer.hikari",
        "io.leangen.geantyref",
        "net.kyori",
        "org.checkerframework",
        "org.jdbi",
        "org.antlr",
        "org.pcollections",
        "org.slf4j",
        "org.spongepowered.configurate"
    )
}

// spigradle's generateSpigotDescription writes plugin.yml straight into build/resources/main
// without registering itself as an output of processResources, so Gradle 8's stricter
// task-dependency validation flags every task that reads main's output as having an undeclared
// implicit dependency. Can't wire this onto processResources — generateSpigotDescription itself
// depends on `classes` (it inspects compiled classes to detect the plugin main class), and
// classes depends on processResources, so that would be circular. Declare it directly on the
// two tasks that actually package resources/main instead.
tasks.jar {
    dependsOn(tasks.generateSpigotDescription)
}

val shadowJar by tasks.getting(ShadowJar::class) {
    dependsOn(tasks.generateSpigotDescription)
    dependencies {
        exclude("org.yaml:snakeyaml")
    }
}

tasks.register("runBukkit") {
    dependsOn(tasks.debugPaper)
    group = "pex"
    description = "Run a Bukkit environment"
}

tasks.runSpigot {
    javaLauncher.set(pexPlatform.developmentRuntime())
}
