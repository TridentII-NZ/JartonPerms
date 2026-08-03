package me.jarton.perms.model;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class GroupTest {

    @Test
    void settingAPermissionTwiceOverwritesRatherThanDuplicates() {
        Group group = new Group("admin");
        group.setPermission("essentials.fly", true);
        group.setPermission("essentials.fly", false);

        assertEquals(1, group.getPermissions().size());
        assertEquals(false, group.getPermissions().get("essentials.fly"));
    }

    @Test
    void removingAPermissionDropsItEntirely() {
        Group group = new Group("admin");
        group.setPermission("essentials.fly", true);
        group.removePermission("essentials.fly");

        assertTrue(group.getPermissions().isEmpty());
    }

    @Test
    void parentsPreserveInsertionOrder() {
        Group group = new Group("admin");
        group.addParent("default");
        group.addParent("staff");

        assertEquals(List.of("default", "staff"), group.getParents());
    }

    @Test
    void removingAParentThatWasNeverAddedIsANoOp() {
        Group group = new Group("admin");
        group.removeParent("nonexistent");

        assertTrue(group.getParents().isEmpty());
    }

    @Test
    void optionsAreSetByKey() {
        Group group = new Group("admin");
        group.setOption("prefix", "&c[Admin]");

        assertEquals("&c[Admin]", group.getOptions().get("prefix"));
    }
}
