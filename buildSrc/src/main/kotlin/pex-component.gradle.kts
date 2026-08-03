import ca.stellardrift.build.localization.LocalizationExtension
import ca.stellardrift.build.localization.TemplateType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    `java-library`
}

group = "me.jarton.perms"

repositories {
    mavenCentral()
}

// No toolchain is pinned, so compilation uses whatever JDK launched Gradle. On this machine
// that's JDK 21, which by default emits major-version-65 class files that spigradle's
// detectSpigotMain task and the shadow plugin's bundled ASM (both last released ~2021-2023,
// well before Java 21 support) can't parse ("Unsupported class file major version 65").
// The project's own dev-runtime target is much older (gradle.properties' developmentRuntime=16),
// so targeting 17 bytecode via --release (rather than requiring an actual JDK 17 toolchain,
// which isn't installed here and would need a toolchain-download resolver) fixes this at the
// root without touching a JDK 17 install.
tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
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
