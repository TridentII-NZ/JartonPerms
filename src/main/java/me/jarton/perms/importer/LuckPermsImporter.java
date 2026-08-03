package me.jarton.perms.importer;

import me.jarton.perms.model.Group;
import me.jarton.perms.model.User;
import me.jarton.perms.store.DataStore;
import me.jarton.perms.store.UuidIndex;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.group.GroupManager;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.PermissionNode;
import net.luckperms.api.query.QueryOptions;

import java.util.UUID;

public class LuckPermsImporter {

    private final DataStore dataStore;
    private final UuidIndex uuidIndex;

    public LuckPermsImporter(DataStore dataStore, UuidIndex uuidIndex) {
        this.dataStore = dataStore;
        this.uuidIndex = uuidIndex;
    }

    public ImportResult importFromLuckPerms() throws Exception {
        LuckPerms luckPerms = LuckPermsProvider.get();
        ImportResult result = new ImportResult();

        GroupManager groupManager = luckPerms.getGroupManager();
        for (net.luckperms.api.model.group.Group lpGroup : groupManager.getLoadedGroups()) {
            Group group = new Group(lpGroup.getName());
            for (Node node : lpGroup.getNodes()) {
                applyNode(node, group::setPermission, group::addParent, result);
            }
            CachedMetaData meta = lpGroup.getCachedData().getMetaData(QueryOptions.nonContextual());
            if (meta.getPrefix() != null) {
                group.setOption("prefix", meta.getPrefix());
            }
            if (meta.getSuffix() != null) {
                group.setOption("suffix", meta.getSuffix());
            }
            dataStore.saveGroup(group);
            result.incrementGroups();
        }

        UserManager userManager = luckPerms.getUserManager();
        for (UUID uuid : userManager.getUniqueUsers().join()) {
            net.luckperms.api.model.user.User lpUser = userManager.loadUser(uuid).get();
            String username = lpUser.getUsername() != null ? lpUser.getUsername() : uuid.toString();
            User user = new User(uuid, username);
            for (Node node : lpUser.getNodes()) {
                applyNode(node, user::setPermission, user::addGroup, result);
            }
            dataStore.saveUser(user);
            uuidIndex.update(uuid, username);
            result.incrementUsers();
        }

        return result;
    }

    private void applyNode(
            Node node,
            java.util.function.BiConsumer<String, Boolean> onPermission,
            java.util.function.Consumer<String> onParentOrGroup,
            ImportResult result) {
        if (!node.getContexts().isEmpty()) {
            result.incrementSkipped();
            return;
        }
        if (node instanceof PermissionNode permissionNode) {
            onPermission.accept(permissionNode.getPermission(), permissionNode.getValue());
        } else if (node instanceof InheritanceNode inheritanceNode) {
            onParentOrGroup.accept(inheritanceNode.getGroupName());
        }
    }
}
