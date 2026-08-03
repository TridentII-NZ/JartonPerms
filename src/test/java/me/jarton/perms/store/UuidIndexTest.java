package me.jarton.perms.store;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UuidIndexTest {

    private static final UUID SAMPLE_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void aFreshUuidIsNotKnown(@TempDir Path tempDir) throws Exception {
        UuidIndex index = new UuidIndex(tempDir);

        assertFalse(index.isKnown(SAMPLE_UUID));
        assertEquals(Optional.empty(), index.checkRename(SAMPLE_UUID, "Steve"));
    }

    @Test
    void updatingMakesTheUuidKnown(@TempDir Path tempDir) throws Exception {
        UuidIndex index = new UuidIndex(tempDir);

        index.update(SAMPLE_UUID, "Steve");

        assertTrue(index.isKnown(SAMPLE_UUID));
    }

    @Test
    void checkRenameDetectsAUsernameChange(@TempDir Path tempDir) throws Exception {
        UuidIndex index = new UuidIndex(tempDir);
        index.update(SAMPLE_UUID, "OldName");

        Optional<String> result = index.checkRename(SAMPLE_UUID, "NewName");

        assertEquals(Optional.of("OldName"), result);
    }

    @Test
    void checkRenameReturnsEmptyWhenUsernameIsUnchanged(@TempDir Path tempDir) throws Exception {
        UuidIndex index = new UuidIndex(tempDir);
        index.update(SAMPLE_UUID, "Steve");

        Optional<String> result = index.checkRename(SAMPLE_UUID, "Steve");

        assertEquals(Optional.empty(), result);
    }

    @Test
    void theIndexPersistsAcrossInstances(@TempDir Path tempDir) throws Exception {
        UuidIndex first = new UuidIndex(tempDir);
        first.update(SAMPLE_UUID, "Steve");

        UuidIndex second = new UuidIndex(tempDir);

        assertTrue(second.isKnown(SAMPLE_UUID));
        assertEquals(Optional.of("Steve"), second.checkRename(SAMPLE_UUID, "NewName"));
    }

    @Test
    void getUsernameReturnsTheCurrentlyIndexedUsername(@TempDir Path tempDir) throws Exception {
        UuidIndex index = new UuidIndex(tempDir);
        index.update(SAMPLE_UUID, "Steve");

        assertEquals(Optional.of("Steve"), index.getUsername(SAMPLE_UUID));
    }

    @Test
    void getUsernameIsEmptyForAnUnknownUuid(@TempDir Path tempDir) throws Exception {
        UuidIndex index = new UuidIndex(tempDir);

        assertEquals(Optional.empty(), index.getUsername(SAMPLE_UUID));
    }
}
