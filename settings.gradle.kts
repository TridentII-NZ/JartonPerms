rootProject.name = "JartonPerms"

include("api")
include("core")

listOf("bukkit", "velocity").forEach {
    include(":platform:$it")
}

listOf("proxy-common", "hikari-config", "minecraft", "glob", "legacy").forEach {
    include("impl-blocks:$it")
}

listOf("file", "sql").forEach {
    include(":datastore:$it")
}
