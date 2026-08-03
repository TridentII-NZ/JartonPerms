package me.jarton.perms.listener;

import me.jarton.perms.config.PluginConfig;
import me.jarton.perms.model.Group;
import me.jarton.perms.model.User;
import me.jarton.perms.resolve.PermissionResolver;
import me.jarton.perms.store.DataStore;
import me.jarton.perms.store.UuidIndex;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class JoinListener implements Listener {

    private final Plugin plugin;
    private final DataStore dataStore;
    private final UuidIndex uuidIndex;
    private final PluginConfig config;
    private final PermissionResolver resolver;
    private final Map<UUID, PermissionAttachment> attachments = new HashMap<>();

    public JoinListener(
            Plugin plugin,
            DataStore dataStore,
            UuidIndex uuidIndex,
            PluginConfig config,
            PermissionResolver resolver) {
        this.plugin = plugin;
        this.dataStore = dataStore;
        this.uuidIndex = uuidIndex;
        this.config = config;
        this.resolver = resolver;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        try {
            User user = resolveUser(player.getUniqueId(), player.getName());
            apply(player, user);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load JartonPerms data for " + player.getName(), e);
        }
    }

    /**
     * Loads (or creates) the {@link User} for a joining player, handling the
     * username-rename detection described in the design spec: renaming the backing
     * file when the UUID index shows a different previous username, or provisioning a
     * brand-new user (added to the configured default group) when the UUID is unseen.
     */
    public User resolveUser(UUID uuid, String currentUsername) throws Exception {
        Optional<String> oldUsername = uuidIndex.checkRename(uuid, currentUsername);
        if (oldUsername.isPresent()) {
            dataStore.renameUserFile(oldUsername.get(), currentUsername);
            uuidIndex.update(uuid, currentUsername);
            return dataStore.loadUser(currentUsername).orElseGet(() -> newUser(uuid, currentUsername));
        }

        if (!uuidIndex.isKnown(uuid)) {
            User user = newUser(uuid, currentUsername);
            dataStore.saveUser(user);
            uuidIndex.update(uuid, currentUsername);
            return user;
        }

        return dataStore.loadUser(currentUsername).orElseGet(() -> newUser(uuid, currentUsername));
    }

    private User newUser(UUID uuid, String username) {
        User user = new User(uuid, username);
        user.addGroup(config.getDefaultGroup());
        return user;
    }

    /**
     * Recomputes a player's effective permissions and pushes them into a fresh
     * {@link PermissionAttachment}, replacing any attachment this plugin previously
     * added. Public (not just called from {@code onJoin}) so Task 9's {@code /jp
     * reload} command can recompute a player's attachment after an edit without a
     * synthetic join event.
     */
    public void apply(Player player, User user) throws Exception {
        Map<String, Group> groups = new HashMap<>();
        for (String groupName : dataStore.listGroupNames()) {
            dataStore.loadGroup(groupName).ifPresent(group -> groups.put(groupName, group));
        }

        Set<String> knownPermissionNodes = Bukkit.getPluginManager().getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());

        Map<String, Boolean> effective = resolver.resolve(user, groups, knownPermissionNodes);

        PermissionAttachment previous = attachments.remove(player.getUniqueId());
        if (previous != null) {
            player.removeAttachment(previous);
        }
        PermissionAttachment attachment = player.addAttachment(plugin);
        effective.forEach(attachment::setPermission);
        attachments.put(player.getUniqueId(), attachment);
    }
}
