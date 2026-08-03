package me.jarton.perms.resolve;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class NodeTrie {

    private final Map<String, Boolean> exact = new HashMap<>();
    private final Map<String, Boolean> wildcards = new HashMap<>();

    public void insert(String node, boolean value) {
        if (node.equals("*")) {
            wildcards.put("", value);
        } else if (node.endsWith(".*")) {
            String prefix = node.substring(0, node.length() - 2);
            wildcards.put(prefix, value);
        } else {
            exact.put(node, value);
        }
    }

    public Optional<Boolean> resolve(String permission) {
        if (exact.containsKey(permission)) {
            return Optional.of(exact.get(permission));
        }

        String prefix = permission;
        while (true) {
            if (wildcards.containsKey(prefix)) {
                return Optional.of(wildcards.get(prefix));
            }

            int lastDot = prefix.lastIndexOf('.');
            if (lastDot < 0) {
                break;
            }
            prefix = prefix.substring(0, lastDot);
        }

        if (wildcards.containsKey("")) {
            return Optional.of(wildcards.get(""));
        }

        return Optional.empty();
    }
}
