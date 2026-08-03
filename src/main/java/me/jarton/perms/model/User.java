package me.jarton.perms.model;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Required;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ConfigSerializable
public class User {

    @Required
    private UUID uuid;
    private String username;
    private LinkedHashMap<String, Boolean> permissions = new LinkedHashMap<>();
    private List<String> groups = new ArrayList<>();

    // No-arg constructor required by Configurate's ObjectMapper.
    public User() {
    }

    public User(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Map<String, Boolean> getPermissions() {
        return permissions;
    }

    public void setPermission(String node, boolean value) {
        permissions.put(node, value);
    }

    public void removePermission(String node) {
        permissions.remove(node);
    }

    public List<String> getGroups() {
        return groups;
    }

    public void addGroup(String groupName) {
        if (!groups.contains(groupName)) {
            groups.add(groupName);
        }
    }

    public void removeGroup(String groupName) {
        groups.remove(groupName);
    }
}
