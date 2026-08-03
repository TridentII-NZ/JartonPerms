package me.jarton.perms.ladder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RankLadder {

    private final String name;
    private final List<String> groups = new ArrayList<>();

    public RankLadder(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void addGroup(String groupName) {
        if (!groups.contains(groupName)) {
            groups.add(groupName);
        }
    }

    public void removeGroup(String groupName) {
        groups.remove(groupName);
    }

    public Optional<String> currentGroup(List<String> userGroups) {
        return currentIndex(userGroups).map(groups::get);
    }

    public Optional<String> promote(List<String> userGroups) {
        if (groups.isEmpty()) {
            return Optional.empty();
        }
        Optional<Integer> current = currentIndex(userGroups);
        if (current.isEmpty()) {
            return Optional.of(groups.get(0));
        }
        int index = current.get();
        if (index >= groups.size() - 1) {
            return Optional.empty();
        }
        return Optional.of(groups.get(index + 1));
    }

    public Optional<String> demote(List<String> userGroups) {
        Optional<Integer> current = currentIndex(userGroups);
        if (current.isEmpty()) {
            return Optional.empty();
        }
        int index = current.get();
        if (index <= 0) {
            return Optional.empty();
        }
        return Optional.of(groups.get(index - 1));
    }

    private Optional<Integer> currentIndex(List<String> userGroups) {
        int highest = -1;
        for (String groupName : userGroups) {
            int index = groups.indexOf(groupName);
            if (index > highest) {
                highest = index;
            }
        }
        return highest >= 0 ? Optional.of(highest) : Optional.empty();
    }
}
