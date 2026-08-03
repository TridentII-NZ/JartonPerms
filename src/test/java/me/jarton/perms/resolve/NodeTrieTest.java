package me.jarton.perms.resolve;

import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class NodeTrieTest {

    @Test
    void exactNodeResolvesToItsValue() {
        NodeTrie trie = new NodeTrie();
        trie.insert("essentials.fly", true);

        assertEquals(Optional.of(true), trie.resolve("essentials.fly"));
    }

    @Test
    void unknownPermissionResolvesToEmpty() {
        NodeTrie trie = new NodeTrie();

        assertEquals(Optional.empty(), trie.resolve("essentials.fly"));
    }

    @Test
    void wildcardGrantsEverythingUnderIt() {
        NodeTrie trie = new NodeTrie();
        trie.insert("essentials.*", true);

        assertEquals(Optional.of(true), trie.resolve("essentials.fly"));
        assertEquals(Optional.of(true), trie.resolve("essentials.kits.starter"));
    }

    @Test
    void exactNodeOverridesAWildcardThatWouldOtherwiseMatch() {
        NodeTrie trie = new NodeTrie();
        trie.insert("essentials.*", true);
        trie.insert("essentials.fly", false);

        assertEquals(Optional.of(false), trie.resolve("essentials.fly"));
        assertEquals(Optional.of(true), trie.resolve("essentials.kits.starter"));
    }

    @Test
    void moreSpecificWildcardOverridesABroaderOne() {
        NodeTrie trie = new NodeTrie();
        trie.insert("essentials.*", true);
        trie.insert("essentials.kits.*", false);

        assertEquals(Optional.of(false), trie.resolve("essentials.kits.starter"));
        assertEquals(Optional.of(true), trie.resolve("essentials.fly"));
    }

    @Test
    void rootWildcardMatchesAnyPermission() {
        NodeTrie trie = new NodeTrie();
        trie.insert("*", true);

        assertEquals(Optional.of(true), trie.resolve("some.random.permission"));
    }

    @Test
    void insertingTheSameExactNodeTwiceOverwrites() {
        NodeTrie trie = new NodeTrie();
        trie.insert("essentials.fly", true);
        trie.insert("essentials.fly", false);

        assertEquals(Optional.of(false), trie.resolve("essentials.fly"));
    }
}
