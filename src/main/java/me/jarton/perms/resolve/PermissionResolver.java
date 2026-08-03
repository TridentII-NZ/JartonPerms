package me.jarton.perms.resolve;

import me.jarton.perms.model.Group;
import me.jarton.perms.model.User;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PermissionResolver {

    public Map<String, Boolean> resolve(User user, Map<String, Group> groupsByName, Set<String> knownPermissionNodes) {
        NodeTrie trie = new NodeTrie();
        Set<String> explicitNodes = new HashSet<>();

        for (String groupName : user.getGroups()) {
            Set<String> visitedPath = new HashSet<>();
            insertGroup(groupName, groupsByName, visitedPath, trie, explicitNodes);
        }

        for (Map.Entry<String, Boolean> entry : user.getPermissions().entrySet()) {
            insertNode(trie, explicitNodes, entry.getKey(), entry.getValue());
        }

        Set<String> nodesToResolve = new HashSet<>(knownPermissionNodes);
        nodesToResolve.addAll(explicitNodes);

        Map<String, Boolean> effective = new HashMap<>();
        for (String node : nodesToResolve) {
            trie.resolve(node).ifPresent(value -> effective.put(node, value));
        }
        return effective;
    }

    private void insertGroup(
            String groupName,
            Map<String, Group> groupsByName,
            Set<String> visitedPath,
            NodeTrie trie,
            Set<String> explicitNodes) {
        if (!visitedPath.add(groupName)) {
            return;
        }
        Group group = groupsByName.get(groupName);
        if (group == null) {
            visitedPath.remove(groupName);
            return;
        }
        for (String parent : group.getParents()) {
            insertGroup(parent, groupsByName, visitedPath, trie, explicitNodes);
        }
        for (Map.Entry<String, Boolean> entry : group.getPermissions().entrySet()) {
            insertNode(trie, explicitNodes, entry.getKey(), entry.getValue());
        }
        visitedPath.remove(groupName);
    }

    private void insertNode(NodeTrie trie, Set<String> explicitNodes, String node, boolean value) {
        trie.insert(node, value);
        if (!node.equals("*") && !node.endsWith(".*")) {
            explicitNodes.add(node);
        }
    }
}
