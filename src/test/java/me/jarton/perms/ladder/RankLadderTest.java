package me.jarton.perms.ladder;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RankLadderTest {

    @Test
    void addGroupDoesNotDuplicate() {
        RankLadder ladder = new RankLadder("staff");
        ladder.addGroup("member");
        ladder.addGroup("member");

        assertEquals(List.of("member"), ladder.getGroups());
    }

    @Test
    void currentGroupReturnsTheHighestRankedLadderGroupTheUserHolds() {
        RankLadder ladder = new RankLadder("staff");
        ladder.addGroup("member");
        ladder.addGroup("trusted");
        ladder.addGroup("veteran");

        Optional<String> result = ladder.currentGroup(List.of("member", "trusted", "unrelated-group"));

        assertEquals(Optional.of("trusted"), result);
    }

    @Test
    void currentGroupIsEmptyWhenTheUserHoldsNoLadderGroup() {
        RankLadder ladder = new RankLadder("staff");
        ladder.addGroup("member");

        assertEquals(Optional.empty(), ladder.currentGroup(List.of("unrelated-group")));
    }

    @Test
    void promotingAPlayerNotOnTheLadderMovesThemToTheFirstGroup() {
        RankLadder ladder = new RankLadder("staff");
        ladder.addGroup("member");
        ladder.addGroup("trusted");

        Optional<String> result = ladder.promote(List.of("unrelated-group"));

        assertEquals(Optional.of("member"), result);
    }

    @Test
    void promotingMovesToTheNextGroupUp() {
        RankLadder ladder = new RankLadder("staff");
        ladder.addGroup("member");
        ladder.addGroup("trusted");
        ladder.addGroup("veteran");

        Optional<String> result = ladder.promote(List.of("member"));

        assertEquals(Optional.of("trusted"), result);
    }

    @Test
    void promotingFromTheTopGroupIsANoOp() {
        RankLadder ladder = new RankLadder("staff");
        ladder.addGroup("member");
        ladder.addGroup("veteran");

        Optional<String> result = ladder.promote(List.of("veteran"));

        assertEquals(Optional.empty(), result);
    }

    @Test
    void promotingOnAnEmptyLadderIsANoOp() {
        RankLadder ladder = new RankLadder("staff");

        assertEquals(Optional.empty(), ladder.promote(List.of()));
    }

    @Test
    void demotingMovesToThePreviousGroup() {
        RankLadder ladder = new RankLadder("staff");
        ladder.addGroup("member");
        ladder.addGroup("trusted");
        ladder.addGroup("veteran");

        Optional<String> result = ladder.demote(List.of("veteran"));

        assertEquals(Optional.of("trusted"), result);
    }

    @Test
    void demotingFromTheBottomGroupIsANoOp() {
        RankLadder ladder = new RankLadder("staff");
        ladder.addGroup("member");
        ladder.addGroup("trusted");

        Optional<String> result = ladder.demote(List.of("member"));

        assertEquals(Optional.empty(), result);
    }

    @Test
    void demotingAPlayerNotOnTheLadderIsANoOp() {
        RankLadder ladder = new RankLadder("staff");
        ladder.addGroup("member");

        Optional<String> result = ladder.demote(List.of("unrelated-group"));

        assertEquals(Optional.empty(), result);
    }

    @Test
    void removingAGroupTakesItOffTheLadder() {
        RankLadder ladder = new RankLadder("staff");
        ladder.addGroup("member");
        ladder.addGroup("trusted");

        ladder.removeGroup("member");

        assertEquals(List.of("trusted"), ladder.getGroups());
    }
}
