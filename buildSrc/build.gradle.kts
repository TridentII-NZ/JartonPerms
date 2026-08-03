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
        sequenceOf("asm", "asm-util", "asm-tree", "asm-analysis").forEach {
            implementation("org.ow2.asm:$it") {
                version { require("9.2") }
                because("Newer plugin versions need a newer ASM")
            }
        }
    }

    implementation("ca.stellardrift:gradle-plugin-localization:6.2.0")
    implementation("ca.stellardrift:gradle-plugin-templating:6.0.1")
    implementation("com.github.jengelman.gradle.plugins:shadow:6.1.0")
    implementation("kr.entree:spigradle:2.2.3") {
        exclude(group = "kr.entree", module = "spigradle-annotations")
    }
    implementation(kotlin("gradle-plugin", "1.4.21"))
    implementation(kotlin("reflect"))
}
