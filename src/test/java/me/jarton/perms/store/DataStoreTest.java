package me.jarton.perms.store;

import me.jarton.perms.model.Group;
import me.jarton.perms.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DataStoreTest {

    private static final UUID SAMPLE_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void savingAndLoadingAGroupRoundTripsAllFields(@TempDir Path tempDir) throws Exception {
        DataStore store = new DataStore(tempDir);
        Group group = new Group("admin");
        group.setPermission("essentials.fly", true);
        group.addParent("default");
        group.setOption("prefix", "&c[Admin]");
        store.saveGroup(group);

        Optional<Group> loaded = store.loadGroup("admin");

        assertTrue(loaded.isPresent());
        assertEquals("admin", loaded.get().getName());
        assertEquals(true, loaded.get().getPermissions().get("essentials.fly"));
        assertEquals(java.util.List.of("default"), loaded.get().getParents());
        assertEquals("&c[Admin]", loaded.get().getOptions().get("prefix"));
    }

    @Test
    void loadingAGroupThatDoesNotExistReturnsEmpty(@TempDir Path tempDir) throws Exception {
        DataStore store = new DataStore(tempDir);

        assertTrue(store.loadGroup("nonexistent").isEmpty());
    }

    @Test
    void deletingAGroupRemovesItsFile(@TempDir Path tempDir) throws Exception {
        DataStore store = new DataStore(tempDir);
        store.saveGroup(new Group("temp"));

        store.deleteGroup("temp");

        assertTrue(store.loadGroup("temp").isEmpty());
    }

    @Test
    void listGroupNamesReturnsEverySavedGroup(@TempDir Path tempDir) throws Exception {
        DataStore store = new DataStore(tempDir);
        store.saveGroup(new Group("default"));
        store.saveGroup(new Group("admin"));

        Set<String> names = store.listGroupNames();

        assertEquals(Set.of("default", "admin"), names);
    }

    @Test
    void savingAndLoadingAUserRoundTripsAllFields(@TempDir Path tempDir) throws Exception {
        DataStore store = new DataStore(tempDir);
        User user = new User(SAMPLE_UUID, "Steve");
        user.setPermission("essentials.fly", true);
        user.addGroup("default");
        store.saveUser(user);

        Optional<User> loaded = store.loadUser("Steve");

        assertTrue(loaded.isPresent());
        assertEquals(SAMPLE_UUID, loaded.get().getUuid());
        assertEquals("Steve", loaded.get().getUsername());
        assertEquals(true, loaded.get().getPermissions().get("essentials.fly"));
        assertEquals(java.util.List.of("default"), loaded.get().getGroups());
    }

    @Test
    void renamingAUserFileMovesItToTheNewUsername(@TempDir Path tempDir) throws Exception {
        DataStore store = new DataStore(tempDir);
        store.saveUser(new User(SAMPLE_UUID, "OldName"));

        store.renameUserFile("OldName", "NewName");

        assertTrue(store.loadUser("OldName").isEmpty());
        assertTrue(store.loadUser("NewName").isPresent());
    }

    @Test
    void constructingADataStoreCreatesTheGroupsAndUsersDirectories(@TempDir Path tempDir) throws Exception {
        new DataStore(tempDir);

        assertTrue(Files.isDirectory(tempDir.resolve("groups")));
        assertTrue(Files.isDirectory(tempDir.resolve("users")));
    }

    @Test
    void renamingAUserFileOverwritesStaleFileAtTargetUsername(@TempDir Path tempDir) throws Exception {
        DataStore store = new DataStore(tempDir);
        UUID oldUuid = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID newUuid = UUID.fromString("00000000-0000-0000-0000-000000000002");

        // Save a stale file (different user) at the target username.
        User staleUser = new User(newUuid, "TargetName");
        store.saveUser(staleUser);

        // Rename from a different username to the target, which should overwrite the stale file.
        User renamedUser = new User(oldUuid, "OldName");
        store.saveUser(renamedUser);

        store.renameUserFile("OldName", "TargetName");

        // Target file now has the renamed-from user's data, not the stale data.
        Optional<User> loaded = store.loadUser("TargetName");
        assertTrue(loaded.isPresent());
        assertEquals(oldUuid, loaded.get().getUuid());
        assertEquals("TargetName", loaded.get().getUsername());
    }

    @Test
    void renamingAUserFileWithOnlyACaseChangeDoesNotDeleteTheData(@TempDir Path tempDir) throws Exception {
        DataStore store = new DataStore(tempDir);
        store.saveUser(new User(SAMPLE_UUID, "Steve"));

        store.renameUserFile("Steve", "STEVE");

        Optional<User> loaded = store.loadUser("STEVE");
        assertTrue(loaded.isPresent());
        assertEquals("STEVE", loaded.get().getUsername());
        assertEquals(SAMPLE_UUID, loaded.get().getUuid());
    }
}
