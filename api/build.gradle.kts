plugins {
    id("pex-component")
}

useCheckerFramework()
useImmutables()

dependencies {
    val adventureVersion: String by project
    val configurateVersion: String by project
    val pCollectionsVersion: String by project
    val slf4jVersion: String by project

    api("org.pcollections:pcollections:$pCollectionsVersion")
    api("net.kyori:adventure-api:$adventureVersion")
    implementation("net.kyori:adventure-text-serializer-plain:$adventureVersion")
    api("org.spongepowered:configurate-core:$configurateVersion")
    api("org.slf4j:slf4j-api:$slf4jVersion")
}
