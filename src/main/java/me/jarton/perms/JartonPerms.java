package me.jarton.perms;

import me.jarton.perms.command.JpCommand;
import me.jarton.perms.config.PluginConfig;
import me.jarton.perms.listener.JoinListener;
import me.jarton.perms.resolve.PermissionResolver;
import me.jarton.perms.store.DataStore;
import me.jarton.perms.store.UuidIndex;
import org.bukkit.plugin.java.JavaPlugin;

public final class JartonPerms extends JavaPlugin {

    @Override
    public void onEnable() {
        try {
            getDataFolder().mkdirs();
            DataStore dataStore = new DataStore(getDataFolder().toPath());
            UuidIndex uuidIndex = new UuidIndex(getDataFolder().toPath());
            PluginConfig config = new PluginConfig(getDataFolder().toPath());
            PermissionResolver resolver = new PermissionResolver();
            JoinListener joinListener = new JoinListener(this, dataStore, uuidIndex, config, resolver);

            getServer().getPluginManager().registerEvents(joinListener, this);
            JpCommand jpCommand = new JpCommand(dataStore, config, joinListener);
            getCommand("jp").setExecutor(jpCommand);
            getCommand("jp").setTabCompleter(jpCommand);

            getLogger().info("JartonPerms enabled.");
        } catch (Exception e) {
            getLogger().severe("Failed to enable JartonPerms: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("JartonPerms disabled.");
    }
}
