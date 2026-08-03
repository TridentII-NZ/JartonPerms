package me.jarton.perms.store;

import me.jarton.perms.model.Group;
import me.jarton.perms.model.User;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class DataStore {

    private static final String FILE_EXTENSION = ".conf";

    private final Path groupsDir;
    private final Path usersDir;

    public DataStore(Path baseDir) throws IOException {
        this.groupsDir = baseDir.resolve("groups");
        this.usersDir = baseDir.resolve("users");
        Files.createDirectories(groupsDir);
        Files.createDirectories(usersDir);
    }

    public Optional<Group> loadGroup(String name) throws ConfigurateException {
        return load(groupFile(name), Group.class);
    }

    public void saveGroup(Group group) throws ConfigurateException {
        save(groupFile(group.getName()), Group.class, group);
    }

    public void deleteGroup(String name) throws IOException {
        Files.deleteIfExists(groupFile(name));
    }

    public Set<String> listGroupNames() throws IOException {
        return listNames(groupsDir);
    }

    public Optional<User> loadUser(String username) throws ConfigurateException {
        return load(userFile(username), User.class);
    }

    public void saveUser(User user) throws ConfigurateException {
        save(userFile(user.getUsername()), User.class, user);
    }

    public void renameUserFile(String oldUsername, String newUsername) throws ConfigurateException {
        Optional<User> user = loadUser(oldUsername);
        if (user.isPresent()) {
            user.get().setUsername(newUsername);
            // Usernames are reused across time; stale files at the target path are overwritten, not an error.
            save(userFile(newUsername), User.class, user.get());
            try {
                Files.deleteIfExists(userFile(oldUsername));
            } catch (IOException e) {
                throw new ConfigurateException("Failed to delete old user file: " + oldUsername, e);
            }
        }
    }

    private <T> Optional<T> load(Path file, Class<T> type) throws ConfigurateException {
        if (!Files.exists(file)) {
            return Optional.empty();
        }
        HoconConfigurationLoader loader = HoconConfigurationLoader.builder().path(file).build();
        CommentedConfigurationNode node = loader.load();
        return Optional.ofNullable(node.get(type));
    }

    private <T> void save(Path file, Class<T> type, T value) throws ConfigurateException {
        HoconConfigurationLoader loader = HoconConfigurationLoader.builder().path(file).build();
        CommentedConfigurationNode node = loader.createNode();
        node.set(type, value);
        loader.save(node);
    }

    private Set<String> listNames(Path directory) throws IOException {
        Set<String> names = new LinkedHashSet<>();
        try (Stream<Path> files = Files.list(directory)) {
            files.filter(path -> path.toString().endsWith(FILE_EXTENSION))
                 .forEach(path -> {
                     String filename = path.getFileName().toString();
                     names.add(filename.substring(0, filename.length() - FILE_EXTENSION.length()));
                 });
        }
        return names;
    }

    private Path groupFile(String name) {
        return groupsDir.resolve(name + FILE_EXTENSION);
    }

    private Path userFile(String username) {
        return usersDir.resolve(username + FILE_EXTENSION);
    }
}
