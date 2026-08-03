package me.jarton.perms.resolve;

import me.jarton.perms.model.Group;
import me.jarton.perms.model.User;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PermissionResolverTest {

    private static final UUID SAMPLE_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final PermissionResolver resolver = new PermissionResolver();

    @Test
    void usersOwnNodeOverridesAGroupNode() {
        Group defaultGroup = new Group("default");
        defaultGroup.setPermission("essentials.fly", false);

        User user = new User(SAMPLE_UUID, "Steve");
        user.addGroup("default");
        user.setPermission("essentials.fly", true);

        Map<String, Boolean> result = resolver.resolve(
            user, Map.of("default", defaultGroup), Set.of("essentials.fly"));

        assertEquals(true, result.get("essentials.fly"));
    }

    @Test
    void laterAddedGroupWinsOverEarlierAddedGroupOnConflict() {
        Group g1 = new Group("g1");
        g1.setPermission("essentials.fly", true);
        Group g2 = new Group("g2");
        g2.setPermission("essentials.fly", false);

        User user = new User(SAMPLE_UUID, "Steve");
        user.addGroup("g1");
        user.addGroup("g2");

        Map<String, Boolean> result = resolver.resolve(
            user, Map.of("g1", g1, "g2", g2), Set.of("essentials.fly"));

        assertEquals(false, result.get("essentials.fly"));
    }

    @Test
    void parentGroupPermissionsAreInherited() {
        Group defaultGroup = new Group("default");
        defaultGroup.setPermission("essentials.help", true);
        Group admin = new Group("admin");
        admin.addParent("default");

        User user = new User(SAMPLE_UUID, "Steve");
        user.addGroup("admin");

        Map<String, Boolean> result = resolver.resolve(
            user, Map.of("default", defaultGroup, "admin", admin), Set.of("essentials.help"));

        assertEquals(true, result.get("essentials.help"));
    }

    @Test
    void aGroupsOwnNodeOverridesItsParentsNodeOnConflict() {
        Group defaultGroup = new Group("default");
        defaultGroup.setPermission("essentials.help", false);
        Group admin = new Group("admin");
        admin.addParent("default");
        admin.setPermission("essentials.help", true);

        User user = new User(SAMPLE_UUID, "Steve");
        user.addGroup("admin");

        Map<String, Boolean> result = resolver.resolve(
            user, Map.of("default", defaultGroup, "admin", admin), Set.of("essentials.help"));

        assertEquals(true, result.get("essentials.help"));
    }

    @Test
    void wildcardExpandsAgainstKnownPermissionNodes() {
        Group admin = new Group("admin");
        admin.setPermission("essentials.*", true);

        User user = new User(SAMPLE_UUID, "Steve");
        user.addGroup("admin");

        Map<String, Boolean> result = resolver.resolve(
            user, Map.of("admin", admin),
            Set.of("essentials.fly", "essentials.kits.starter", "other.plugin.perm"));

        assertEquals(true, result.get("essentials.fly"));
        assertEquals(true, result.get("essentials.kits.starter"));
        assertFalse(result.containsKey("other.plugin.perm"));
    }

    @Test
    void explicitlyGrantedNodeAppearsEvenIfNotInKnownPermissionNodes() {
        User user = new User(SAMPLE_UUID, "Steve");
        user.setPermission("some.unregistered.permission", true);

        Map<String, Boolean> result = resolver.resolve(user, Map.of(), Set.of());

        assertEquals(true, result.get("some.unregistered.permission"));
    }

    @Test
    void aParentCycleDoesNotCauseInfiniteRecursion() {
        Group a = new Group("a");
        a.addParent("b");
        a.setPermission("perm.a", true);
        Group b = new Group("b");
        b.addParent("a");
        b.setPermission("perm.b", true);

        User user = new User(SAMPLE_UUID, "Steve");
        user.addGroup("a");

        Map<String, Boolean> result = assertTimeoutPreemptively(
            java.time.Duration.ofSeconds(2),
            () -> resolver.resolve(user, Map.of("a", a, "b", b), Set.of("perm.a", "perm.b")));

        assertEquals(true, result.get("perm.a"));
        assertEquals(true, result.get("perm.b"));
    }

    @Test
    void aMissingGroupReferenceIsSkippedRatherThanThrowing() {
        User user = new User(SAMPLE_UUID, "Steve");
        user.addGroup("doesNotExist");

        Map<String, Boolean> result = resolver.resolve(user, Map.of(), Set.of());

        assertTrue(result.isEmpty());
    }
}
