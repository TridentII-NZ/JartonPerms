package me.jarton.perms.store;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class UuidIndex {

    private final Path indexFile;
    private final Map<UUID, String> index = new HashMap<>();

    public UuidIndex(Path baseDir) throws ConfigurateException {
        this.indexFile = baseDir.resolve("uuid-index.conf");
        load();
    }

    public Optional<String> checkRename(UUID uuid, String currentUsername) {
        String previous = index.get(uuid);
        if (previous != null && !previous.equals(currentUsername)) {
            return Optional.of(previous);
        }
        return Optional.empty();
    }

    public boolean isKnown(UUID uuid) {
        return index.containsKey(uuid);
    }

    public void update(UUID uuid, String username) throws ConfigurateException {
        index.put(uuid, username);
        persist();
    }

    private void load() throws ConfigurateException {
        if (!Files.exists(indexFile)) {
            return;
        }
        HoconConfigurationLoader loader = HoconConfigurationLoader.builder().path(indexFile).build();
        CommentedConfigurationNode node = loader.load();
        for (Map.Entry<Object, ? extends CommentedConfigurationNode> entry : node.childrenMap().entrySet()) {
            UUID uuid = UUID.fromString(entry.getKey().toString());
            String username = entry.getValue().getString();
            if (username != null) {
                index.put(uuid, username);
            }
        }
    }

    private void persist() throws ConfigurateException {
        HoconConfigurationLoader loader = HoconConfigurationLoader.builder().path(indexFile).build();
        CommentedConfigurationNode node = loader.createNode();
        for (Map.Entry<UUID, String> entry : index.entrySet()) {
            node.node(entry.getKey().toString()).set(entry.getValue());
        }
        loader.save(node);
    }
}
