import ca.stellardrift.build.localization.LocalizationExtension
import ca.stellardrift.build.localization.TemplateType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    `java-library`
}

group = "me.jarton.perms"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

// Testing dependency
val junitVersion: String by project
dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

// If we have localization plugin, configure it
plugins.withId("ca.stellardrift.localization") {
    extensions.configure(LocalizationExtension::class.java) {
        templateType.set(TemplateType.JAVA)
        templateFile.set(rootProject.file("etc/messages-template.java.tmpl"))
    }

    plugins.withId("org.jetbrains.kotlin.jvm") {
        extensions.configure(KotlinJvmProjectExtension::class) {
            sourceSets.named("main") { kotlin.srcDirs(tasks.named("generateLocalization")) }
        }
    }
}
