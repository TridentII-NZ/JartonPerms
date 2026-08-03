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

            if (dataStore.loadGroup(config.getDefaultGroup()).isEmpty()) {
                dataStore.saveGroup(new me.jarton.perms.model.Group(config.getDefaultGroup()));
            }
            config.save();

            PermissionResolver resolver = new PermissionResolver();
            JoinListener joinListener = new JoinListener(this, dataStore, uuidIndex, config, resolver);
            me.jarton.perms.importer.LuckPermsImporter importer =
                    new me.jarton.perms.importer.LuckPermsImporter(dataStore, uuidIndex);

            getServer().getServicesManager().register(
                    me.jarton.perms.api.JartonPermsAPI.class,
                    new me.jarton.perms.api.JartonPermsAPIImpl(dataStore, uuidIndex, resolver),
                    this,
                    org.bukkit.plugin.ServicePriority.Normal);

            getServer().getPluginManager().registerEvents(joinListener, this);
            JpCommand jpCommand = new JpCommand(dataStore, config, joinListener, importer, this);
            getCommand("jp").setExecutor(jpCommand);
            getCommand("jp").setTabCompleter(jpCommand);

            getLogger().info("JartonPerms enabled.");
        } catch (Exception e) {
            getLogger().log(java.util.logging.Level.SEVERE, "Failed to enable JartonPerms", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("JartonPerms disabled.");
    }
}
