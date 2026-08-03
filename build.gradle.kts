import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    `java-library`
    id("com.gradleup.shadow") version "8.3.5"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:${project.property("paperApiVersion")}")
    compileOnly("net.luckperms:api:5.4")
    implementation("org.spongepowered:configurate-hocon:${project.property("configurateVersion")}")
    testImplementation(platform("org.junit:junit-bom:${project.property("junitVersion")}"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.processResources {
    filesMatching("plugin.yml") {
        expand("version" to project.version)
    }
}

tasks.shadowJar {
    archiveClassifier.set("")
    relocate("org.spongepowered.configurate", "me.jarton.perms.ext.configurate")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
