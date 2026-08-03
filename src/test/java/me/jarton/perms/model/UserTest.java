package me.jarton.perms.model;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private static final UUID SAMPLE_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void settingAPermissionTwiceOverwritesRatherThanDuplicates() {
        User user = new User(SAMPLE_UUID, "Steve");
        user.setPermission("essentials.fly", true);
        user.setPermission("essentials.fly", false);

        assertEquals(1, user.getPermissions().size());
        assertEquals(false, user.getPermissions().get("essentials.fly"));
    }

    @Test
    void addingTheSameGroupTwiceDoesNotDuplicateIt() {
        User user = new User(SAMPLE_UUID, "Steve");
        user.addGroup("default");
        user.addGroup("default");

        assertEquals(List.of("default"), user.getGroups());
    }

    @Test
    void groupsPreserveInsertionOrder() {
        User user = new User(SAMPLE_UUID, "Steve");
        user.addGroup("default");
        user.addGroup("vip");

        assertEquals(List.of("default", "vip"), user.getGroups());
    }

    @Test
    void removingAGroupDropsIt() {
        User user = new User(SAMPLE_UUID, "Steve");
        user.addGroup("default");
        user.removeGroup("default");

        assertTrue(user.getGroups().isEmpty());
    }

    @Test
    void usernameCanBeUpdatedAfterConstruction() {
        User user = new User(SAMPLE_UUID, "Steve");
        user.setUsername("SteveRenamed");

        assertEquals("SteveRenamed", user.getUsername());
    }
}
