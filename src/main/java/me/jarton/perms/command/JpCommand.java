package me.jarton.perms.command;

import me.jarton.perms.config.PluginConfig;
import me.jarton.perms.ladder.RankLadder;
import me.jarton.perms.listener.JoinListener;
import me.jarton.perms.model.Group;
import me.jarton.perms.model.User;
import me.jarton.perms.store.DataStore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JpCommand implements CommandExecutor, TabCompleter {

    private final DataStore dataStore;
    private final PluginConfig config;
    private final JoinListener joinListener;
    private final me.jarton.perms.importer.LuckPermsImporter importer;

    public JpCommand(DataStore dataStore, PluginConfig config, JoinListener joinListener, me.jarton.perms.importer.LuckPermsImporter importer) {
        this.dataStore = dataStore;
        this.config = config;
        this.joinListener = joinListener;
        this.importer = importer;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("jartonperms.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        try {
            switch (args[0].toLowerCase()) {
                case "user":
                    handleUser(sender, args);
                    break;
                case "group":
                    handleGroup(sender, args);
                    break;
                case "ladder":
                    handleLadder(sender, args);
                    break;
                case "promote":
                    handlePromoteOrDemote(sender, args, true);
                    break;
                case "demote":
                    handlePromoteOrDemote(sender, args, false);
                    break;
                case "reload":
                    handleReload(sender);
                    break;
                case "import":
                    handleImport(sender, args);
                    break;
                default:
                    sendUsage(sender);
            }
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Command failed: " + e.getMessage());
        }
        return true;
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "JartonPerms — /jp user|group|ladder|promote|demote|reload|import");
    }

    // ---- user ----

    private void handleUser(CommandSender sender, String[] args) throws Exception {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /jp user <name> permission|group|info ...");
            return;
        }
        String username = args[1];
        Optional<User> maybeUser = dataStore.loadUser(username);
        if (maybeUser.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "No JartonPerms data for '" + username + "' — they must join once first.");
            return;
        }
        User user = maybeUser.get();
        String sub = args[2].toLowerCase();

        switch (sub) {
            case "permission": {
                if (args.length < 5) {
                    sender.sendMessage(ChatColor.RED + "Usage: /jp user <name> permission add|remove <node>");
                    return;
                }
                String node = args[4];
                if (args[3].equalsIgnoreCase("add")) {
                    user.setPermission(node, true);
                } else {
                    user.removePermission(node);
                }
                dataStore.saveUser(user);
                refreshIfOnline(username, user);
                sender.sendMessage(ChatColor.GREEN + "Updated " + username + "'s permission " + node);
                break;
            }
            case "group": {
                if (args.length < 5) {
                    sender.sendMessage(ChatColor.RED + "Usage: /jp user <name> group add|remove <group>");
                    return;
                }
                String groupName = args[4];
                if (args[3].equalsIgnoreCase("add")) {
                    if (dataStore.loadGroup(groupName).isEmpty()) {
                        sender.sendMessage(ChatColor.RED + "No such group '" + groupName + "'.");
                        return;
                    }
                    user.addGroup(groupName);
                } else {
                    user.removeGroup(groupName);
                }
                dataStore.saveUser(user);
                refreshIfOnline(username, user);
                sender.sendMessage(ChatColor.GREEN + "Updated " + username + "'s groups.");
                break;
            }
            case "info": {
                sender.sendMessage(ChatColor.GOLD + "User " + user.getUsername() + " (" + user.getUuid() + ")");
                sender.sendMessage(ChatColor.GRAY + "Groups: " + String.join(", ", user.getGroups()));
                sender.sendMessage(ChatColor.GRAY + "Own permissions: " + user.getPermissions());
                break;
            }
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand '" + sub + "'.");
        }
    }

    // ---- group ----

    private void handleGroup(CommandSender sender, String[] args) throws Exception {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /jp group create|delete|list|<name> ...");
            return;
        }
        String first = args[1].toLowerCase();

        if (first.equals("list")) {
            sender.sendMessage(ChatColor.GOLD + "Groups: " + String.join(", ", dataStore.listGroupNames()));
            return;
        }
        if (first.equals("create")) {
            requireArg(args, 2, "/jp group create <name>");
            String name = args[2];
            if (dataStore.loadGroup(name).isPresent()) {
                sender.sendMessage(ChatColor.RED + "Group '" + name + "' already exists.");
                return;
            }
            dataStore.saveGroup(new Group(name));
            sender.sendMessage(ChatColor.GREEN + "Created group '" + name + "'.");
            return;
        }
        if (first.equals("delete")) {
            requireArg(args, 2, "/jp group delete <name>");
            dataStore.deleteGroup(args[2]);
            sender.sendMessage(ChatColor.GREEN + "Deleted group '" + args[2] + "'.");
            return;
        }

        String groupName = args[1];
        Optional<Group> maybeGroup = dataStore.loadGroup(groupName);
        if (maybeGroup.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "No such group '" + groupName + "'.");
            return;
        }
        Group group = maybeGroup.get();
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /jp group " + groupName + " permission|parent|prefix|suffix|option ...");
            return;
        }
        String action = args[2].toLowerCase();

        switch (action) {
            case "permission": {
                requireArg(args, 4, "/jp group " + groupName + " permission add|remove <node>");
                String node = args[4];
                if (args[3].equalsIgnoreCase("add")) {
                    group.setPermission(node, true);
                } else {
                    group.removePermission(node);
                }
                dataStore.saveGroup(group);
                refreshAllOnline();
                sender.sendMessage(ChatColor.GREEN + "Updated group '" + groupName + "' permission " + node);
                break;
            }
            case "parent": {
                requireArg(args, 4, "/jp group " + groupName + " parent add|remove <group>");
                String parentName = args[4];
                if (args[3].equalsIgnoreCase("add")) {
                    group.addParent(parentName);
                } else {
                    group.removeParent(parentName);
                }
                dataStore.saveGroup(group);
                refreshAllOnline();
                sender.sendMessage(ChatColor.GREEN + "Updated group '" + groupName + "' parents.");
                break;
            }
            case "prefix":
            case "suffix": {
                requireArg(args, 4, "/jp group " + groupName + " " + action + " set <value>");
                String value = String.join(" ", java.util.Arrays.copyOfRange(args, 4, args.length));
                group.setOption(action, value);
                dataStore.saveGroup(group);
                refreshAllOnline();
                sender.sendMessage(ChatColor.GREEN + "Set " + groupName + "'s " + action + ".");
                break;
            }
            case "option": {
                requireArg(args, 5, "/jp group " + groupName + " option set <key> <value>");
                String key = args[4];
                String value = String.join(" ", java.util.Arrays.copyOfRange(args, 5, args.length));
                group.setOption(key, value);
                dataStore.saveGroup(group);
                refreshAllOnline();
                sender.sendMessage(ChatColor.GREEN + "Set " + groupName + "'s option '" + key + "'.");
                break;
            }
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand '" + action + "'.");
        }
    }

    // ---- ladder ----

    private void handleLadder(CommandSender sender, String[] args) throws Exception {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /jp ladder create|delete|<name> ...");
            return;
        }
        String first = args[1].toLowerCase();

        if (first.equals("create")) {
            requireArg(args, 2, "/jp ladder create <name>");
            config.createLadder(args[2]);
            config.save();
            sender.sendMessage(ChatColor.GREEN + "Created ladder '" + args[2] + "'.");
            return;
        }
        if (first.equals("delete")) {
            requireArg(args, 2, "/jp ladder delete <name>");
            config.deleteLadder(args[2]);
            config.save();
            sender.sendMessage(ChatColor.GREEN + "Deleted ladder '" + args[2] + "'.");
            return;
        }

        Optional<RankLadder> maybeLadder = config.getLadder(args[1]);
        if (maybeLadder.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "No such ladder '" + args[1] + "'.");
            return;
        }
        RankLadder ladder = maybeLadder.get();
        requireArg(args, 3, "/jp ladder " + args[1] + " add|remove <group>");
        String groupName = args[3];
        if (args[2].equalsIgnoreCase("add")) {
            ladder.addGroup(groupName);
        } else {
            ladder.removeGroup(groupName);
        }
        config.save();
        sender.sendMessage(ChatColor.GREEN + "Updated ladder '" + args[1] + "'.");
    }

    // ---- promote / demote ----

    private void handlePromoteOrDemote(CommandSender sender, String[] args, boolean promote) throws Exception {
        requireArg(args, 2, "/jp " + (promote ? "promote" : "demote") + " <player> <ladder>");
        String username = args[1];
        String ladderName = args[2];

        Optional<RankLadder> maybeLadder = config.getLadder(ladderName);
        if (maybeLadder.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "No such ladder '" + ladderName + "'.");
            return;
        }
        Optional<User> maybeUser = dataStore.loadUser(username);
        if (maybeUser.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "No JartonPerms data for '" + username + "'.");
            return;
        }
        User user = maybeUser.get();
        RankLadder ladder = maybeLadder.get();

        Optional<String> target = promote ? ladder.promote(user.getGroups()) : ladder.demote(user.getGroups());
        if (target.isEmpty()) {
            sender.sendMessage(ChatColor.RED + username + " cannot be " + (promote ? "promoted" : "demoted") + " on that ladder.");
            return;
        }

        ladder.currentGroup(user.getGroups()).ifPresent(user::removeGroup);
        user.addGroup(target.get());
        dataStore.saveUser(user);
        refreshIfOnline(username, user);
        sender.sendMessage(ChatColor.GREEN + username + " is now in '" + target.get() + "'.");
    }

    // ---- reload ----

    private void handleReload(CommandSender sender) throws Exception {
        refreshAllOnline();
        sender.sendMessage(ChatColor.GREEN + "JartonPerms reloaded for all online players.");
    }

    // ---- import ----

    private void handleImport(CommandSender sender, String[] args) throws Exception {
        if (args.length < 2 || !args[1].equalsIgnoreCase("luckperms")) {
            sender.sendMessage(ChatColor.RED + "Usage: /jp import luckperms");
            return;
        }
        org.bukkit.plugin.Plugin luckPerms = Bukkit.getPluginManager().getPlugin("LuckPerms");
        if (luckPerms == null || !luckPerms.isEnabled()) {
            sender.sendMessage(ChatColor.RED + "LuckPerms is not installed or not enabled.");
            return;
        }
        me.jarton.perms.importer.ImportResult result = importer.importFromLuckPerms();
        sender.sendMessage(ChatColor.GREEN + result.summary());
    }

    // ---- helpers ----

    private void requireArg(String[] args, int index, String usage) {
        if (args.length <= index) {
            throw new IllegalArgumentException("Usage: " + usage);
        }
    }

    private void refreshIfOnline(String username, User user) throws Exception {
        Player player = Bukkit.getPlayerExact(username);
        if (player != null) {
            joinListener.apply(player, user);
        }
    }

    private void refreshAllOnline() throws Exception {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Optional<User> user = dataStore.loadUser(player.getName());
            if (user.isPresent()) {
                joinListener.apply(player, user.get());
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(args[0], List.of("user", "group", "ladder", "promote", "demote", "reload"));
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("user") || args[0].equalsIgnoreCase("promote") || args[0].equalsIgnoreCase("demote"))) {
            return filter(args[1], Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("group")) {
            List<String> options = new ArrayList<>(List.of("create", "delete", "list"));
            try {
                options.addAll(dataStore.listGroupNames());
            } catch (Exception ignored) {
                // best-effort tab-complete; an I/O failure here just means fewer suggestions
            }
            return filter(args[1], options);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("ladder")) {
            List<String> options = new ArrayList<>(List.of("create", "delete"));
            options.addAll(config.getLadders().keySet());
            return filter(args[1], options);
        }
        if (args.length == 3 && (args[0].equalsIgnoreCase("promote") || args[0].equalsIgnoreCase("demote"))) {
            return filter(args[2], config.getLadders().keySet());
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("user")) {
            return filter(args[2], List.of("permission", "group", "info"));
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("group")) {
            return filter(args[2], List.of("permission", "parent", "prefix", "suffix", "option"));
        }
        if (args.length == 4 && List.of("permission", "parent", "group").contains(args[2].toLowerCase())) {
            return filter(args[3], List.of("add", "remove"));
        }
        return List.of();
    }

    private List<String> filter(String prefix, java.util.Collection<String> options) {
        String lower = prefix.toLowerCase();
        return options.stream()
                .filter(option -> option.toLowerCase().startsWith(lower))
                .sorted()
                .collect(Collectors.toList());
    }
}
