package me.jarton.perms.config;

import me.jarton.perms.ladder.RankLadder;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class PluginConfig {

    private final Path configFile;
    private String defaultGroup = "default";
    private final Map<String, RankLadder> ladders = new LinkedHashMap<>();

    public PluginConfig(Path baseDir) throws ConfigurateException {
        this.configFile = baseDir.resolve("config.conf");
        load();
    }

    public String getDefaultGroup() {
        return defaultGroup;
    }

    public void setDefaultGroup(String defaultGroup) {
        this.defaultGroup = defaultGroup;
    }

    public Map<String, RankLadder> getLadders() {
        return ladders;
    }

    public Optional<RankLadder> getLadder(String name) {
        return Optional.ofNullable(ladders.get(name));
    }

    public void createLadder(String name) {
        ladders.putIfAbsent(name, new RankLadder(name));
    }

    public void deleteLadder(String name) {
        ladders.remove(name);
    }

    public void save() throws ConfigurateException {
        HoconConfigurationLoader loader = HoconConfigurationLoader.builder().path(configFile).build();
        CommentedConfigurationNode node = loader.createNode();
        node.node("default-group").set(defaultGroup);
        CommentedConfigurationNode laddersNode = node.node("ladders");
        for (Map.Entry<String, RankLadder> entry : ladders.entrySet()) {
            laddersNode.node(entry.getKey()).set(RankLadder.class, entry.getValue());
        }
        loader.save(node);
    }

    private void load() throws ConfigurateException {
        if (!Files.exists(configFile)) {
            return;
        }
        HoconConfigurationLoader loader = HoconConfigurationLoader.builder().path(configFile).build();
        CommentedConfigurationNode node = loader.load();

        String storedDefaultGroup = node.node("default-group").getString();
        if (storedDefaultGroup != null) {
            defaultGroup = storedDefaultGroup;
        }

        for (Map.Entry<Object, ? extends CommentedConfigurationNode> entry
                : node.node("ladders").childrenMap().entrySet()) {
            RankLadder ladder = entry.getValue().get(RankLadder.class);
            if (ladder != null) {
                ladders.put(entry.getKey().toString(), ladder);
            }
        }
    }
}
