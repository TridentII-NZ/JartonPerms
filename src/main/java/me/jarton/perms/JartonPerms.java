package me.jarton.perms;

import org.bukkit.plugin.java.JavaPlugin;

public final class JartonPerms extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("JartonPerms enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("JartonPerms disabled.");
    }
}
