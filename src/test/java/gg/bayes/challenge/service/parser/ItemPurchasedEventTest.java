package gg.bayes.challenge.service.parser;

import gg.bayes.challenge.persistence.model.CombatLogEntryEntity;
import gg.bayes.challenge.persistence.model.MatchEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ItemPurchasedEventTest {
    private final ItemPurchasedEvent itemPurchasedEvent;

    public ItemPurchasedEventTest() {
        this.itemPurchasedEvent = new ItemPurchasedEvent();
    }

    @Test
    public void shouldParseEvent() {
        MatchEntity match = new MatchEntity();
        match.setId(20L);
        String input = "[00:22:30.458] npc_dota_hero_rubick buys item item_magic_wand";

        CombatLogEntryEntity combatLogEntry = itemPurchasedEvent.parse(input, match);

        assertEquals(1350458, combatLogEntry.getTimestamp());
        assertEquals("rubick", combatLogEntry.getActor());
        assertEquals("magic_wand", combatLogEntry.getItem());
        assertEquals(CombatLogEntryEntity.Type.ITEM_PURCHASED, combatLogEntry.getType());
        assertEquals(20, combatLogEntry.getMatch().getId());
    }

    @Test
    void shouldReturnPattern() {
        String expectedPattern = "^\\[(.*)\\] npc_dota_hero_(.*) buys item item_(.*)$";
        assertEquals(expectedPattern, itemPurchasedEvent.getPattern().pattern());
    }


}