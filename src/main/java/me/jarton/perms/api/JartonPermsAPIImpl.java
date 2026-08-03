package me.jarton.perms.api;

import me.jarton.perms.model.Group;
import me.jarton.perms.model.User;
import me.jarton.perms.resolve.PermissionResolver;
import me.jarton.perms.store.DataStore;
import me.jarton.perms.store.UuidIndex;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class JartonPermsAPIImpl implements JartonPermsAPI {

    private final DataStore dataStore;
    private final UuidIndex uuidIndex;
    private final PermissionResolver resolver;

    public JartonPermsAPIImpl(DataStore dataStore, UuidIndex uuidIndex, PermissionResolver resolver) {
        this.dataStore = dataStore;
        this.uuidIndex = uuidIndex;
        this.resolver = resolver;
    }

    @Override
    public Optional<List<String>> getGroups(UUID playerUuid) throws Exception {
        return loadUser(playerUuid).map(User::getGroups);
    }

    @Override
    public Optional<Map<String, Boolean>> getEffectivePermissions(UUID playerUuid, Set<String> knownPermissionNodes) throws Exception {
        Optional<User> user = loadUser(playerUuid);
        if (user.isEmpty()) {
            return Optional.empty();
        }
        Map<String, Group> groups = new HashMap<>();
        for (String groupName : dataStore.listGroupNames()) {
            dataStore.loadGroup(groupName).ifPresent(group -> groups.put(groupName, group));
        }
        return Optional.of(resolver.resolve(user.get(), groups, knownPermissionNodes));
    }

    private Optional<User> loadUser(UUID uuid) throws Exception {
        Optional<String> username = uuidIndex.getUsername(uuid);
        if (username.isEmpty()) {
            return Optional.empty();
        }
        return dataStore.loadUser(username.get());
    }
}
