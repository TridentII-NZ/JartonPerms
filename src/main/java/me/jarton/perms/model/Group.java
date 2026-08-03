package me.jarton.perms.model;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Required;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@ConfigSerializable
public class Group {

    @Required
    private String name;
    private LinkedHashMap<String, Boolean> permissions = new LinkedHashMap<>();
    private List<String> parents = new ArrayList<>();
    private LinkedHashMap<String, String> options = new LinkedHashMap<>();

    // No-arg constructor required by Configurate's ObjectMapper.
    public Group() {
    }

    public Group(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
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

    public List<String> getParents() {
        return parents;
    }

    public void addParent(String groupName) {
        if (!parents.contains(groupName)) {
            parents.add(groupName);
        }
    }

    public void removeParent(String groupName) {
        parents.remove(groupName);
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public void setOption(String key, String value) {
        options.put(key, value);
    }
}
