package me.jarton.perms.config;

import me.jarton.perms.ladder.RankLadder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PluginConfigTest {

    @Test
    void defaultGroupDefaultsToDefault(@TempDir Path tempDir) throws Exception {
        PluginConfig config = new PluginConfig(tempDir);

        assertEquals("default", config.getDefaultGroup());
    }

    @Test
    void settingAndSavingTheDefaultGroupPersistsAcrossInstances(@TempDir Path tempDir) throws Exception {
        PluginConfig config = new PluginConfig(tempDir);
        config.setDefaultGroup("member");
        config.save();

        PluginConfig reloaded = new PluginConfig(tempDir);

        assertEquals("member", reloaded.getDefaultGroup());
    }

    @Test
    void creatingALadderMakesItRetrievable(@TempDir Path tempDir) throws Exception {
        PluginConfig config = new PluginConfig(tempDir);

        config.createLadder("staff");

        assertTrue(config.getLadder("staff").isPresent());
        assertEquals("staff", config.getLadder("staff").get().getName());
    }

    @Test
    void deletingALadderRemovesIt(@TempDir Path tempDir) throws Exception {
        PluginConfig config = new PluginConfig(tempDir);
        config.createLadder("staff");

        config.deleteLadder("staff");

        assertTrue(config.getLadder("staff").isEmpty());
    }

    @Test
    void laddersPersistAcrossInstancesIncludingTheirGroups(@TempDir Path tempDir) throws Exception {
        PluginConfig config = new PluginConfig(tempDir);
        config.createLadder("staff");
        config.getLadder("staff").get().addGroup("member");
        config.getLadder("staff").get().addGroup("trusted");
        config.save();

        PluginConfig reloaded = new PluginConfig(tempDir);

        Optional<RankLadder> ladder = reloaded.getLadder("staff");
        assertTrue(ladder.isPresent());
        assertEquals(List.of("member", "trusted"), ladder.get().getGroups());
    }
}
