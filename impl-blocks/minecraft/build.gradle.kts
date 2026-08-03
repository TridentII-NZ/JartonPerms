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
    id("pex-component")
    id("ca.stellardrift.localization")
}

// pex-component only wires up mavenCentral(); com.mojang:brigadier isn't published there,
// only on Mojang's own library host (which the Bukkit module's spigradle plugin normally
// adds automatically, but this module compiles independently of that plugin).
repositories {
    maven("https://libraries.minecraft.net/") {
        name = "mojang"
    }
}

useImmutables()
dependencies {
    val adventureVersion: String by project
    val cloudVersion: String by project
    val errorproneVersion: String by project
    val immutablesVersion: String by project

    // See core/build.gradle.kts: this module's source also references @DoNotCall directly.
    compileOnly("com.google.errorprone:error_prone_annotations:$errorproneVersion")

    api(project(":api"))
    api(project(":core"))
    implementation("net.kyori:adventure-text-serializer-plain:$adventureVersion")
    compileOnlyApi("org.immutables:gson:$immutablesVersion")
    runtimeOnly(project(":datastore:sql"))
    api("cloud.commandframework:cloud-core:$cloudVersion")
    api("cloud.commandframework:cloud-minecraft-extras:$cloudVersion")
    compileOnly("cloud.commandframework:cloud-brigadier:$cloudVersion")
    compileOnly("com.mojang:brigadier:1.0.17")

    // Fixed version to line up with MC
    implementation("com.google.code.gson:gson:2.8.0")

    testRuntimeOnly("org.slf4j:slf4j-simple:1.7.30")
}
