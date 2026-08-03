package me.jarton.perms.api;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface JartonPermsAPI {

    Optional<List<String>> getGroups(UUID playerUuid) throws Exception;

    Optional<Map<String, Boolean>> getEffectivePermissions(UUID playerUuid, Set<String> knownPermissionNodes) throws Exception;
}
