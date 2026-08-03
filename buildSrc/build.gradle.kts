/*
 * PermissionsEx
 * Copyright (C) zml and PermissionsEx contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

plugins {
    `kotlin-dsl`
}

repositories {
    maven("https://plugins.gradle.org/m2/") {
        name = "gradlePluginPortalMaven"
    }
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    constraints {
        // 9.2 only understands class files up to Java 18 (major version 62); this project
        // compiles with the ambient JDK (21, major version 65) and spigradle's detectSpigotMain
        // task scans the compiled classes with this ASM version, throwing "Unsupported class
        // file major version 65" at 9.2. 9.7.1 covers Java 21 with headroom.
        sequenceOf("asm", "asm-util", "asm-tree", "asm-analysis").forEach {
            implementation("org.ow2.asm:$it") {
                version { require("9.7.1") }
                because("Newer plugin versions need a newer ASM")
            }
        }

        // shadow's minimize{} feature (UnusedTracker) resolves classes via org.vafer:jdependency,
        // which bundles its own *shaded, relocated* copy of ASM internally
        // (org.vafer.jdeb.shaded.objectweb.asm) rather than using the external org.ow2.asm
        // dependency above — shadow's own POM even excludes org.ow2.asm from jdependency's
        // transitive deps, so the org.ow2.asm constraint above can't reach this copy at all.
        // Pinned at 2.8.0 by shadow:8.1.1; 2.13 bundles a newer ASM that understands Java 21
        // class files.
        implementation("org.vafer:jdependency") {
            version { require("2.13") }
            because("2.8.0's internally-shaded ASM can't read Java 21 class files")
        }
    }

    implementation("ca.stellardrift:gradle-plugin-localization:6.2.0")
    implementation("ca.stellardrift:gradle-plugin-templating:6.0.1")
    // 6.1.0 predates Gradle 7's removal of the legacy `maven` publish plugin; ShadowJavaPlugin's
    // static configuration references org.gradle.api.plugins.MavenPlugin unconditionally and
    // throws NoClassDefFoundError under Gradle 8.10.2. 8.1.1 is the last johnrengelman release
    // and is Gradle 8-compatible.
    implementation("com.github.johnrengelman:shadow:8.1.1")
    implementation("kr.entree:spigradle:2.2.3") {
        // spigradle-annotations 2.2.3 was never published (404 on both the Gradle Plugin
        // Portal and Maven Central); SpigotPlugin's static initializer hard-requires the
        // annotations classes at runtime though, so it can't just be excluded outright —
        // pin to the newest version that Maven Central actually has (2.2.0) instead.
        exclude(group = "kr.entree", module = "spigradle-annotations")
    }
    implementation("kr.entree:spigradle-annotations:2.2.0")
    implementation(kotlin("gradle-plugin", "1.4.21"))
    implementation(kotlin("reflect"))
}
