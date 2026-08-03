package me.jarton.perms.importer;

public class ImportResult {

    private int groupsImported;
    private int usersImported;
    private int nodesSkippedForContext;

    public void incrementGroups() {
        groupsImported++;
    }

    public void incrementUsers() {
        usersImported++;
    }

    public void incrementSkipped() {
        nodesSkippedForContext++;
    }

    public String summary() {
        return "Imported " + groupsImported + " group(s) and " + usersImported
                + " user(s) from LuckPerms. Skipped " + nodesSkippedForContext
                + " context-scoped node(s) — JartonPerms has no context system.";
    }
}
