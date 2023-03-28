package gg.bayes.challenge.service.parser;

import gg.bayes.challenge.persistence.model.CombatLogEntryEntity;
import gg.bayes.challenge.persistence.model.MatchEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DamageDoneEventTest {
    private final DamageDoneEvent damageDoneEvent;

    public DamageDoneEventTest() {
        this.damageDoneEvent = new DamageDoneEvent();
    }

    @Test
    public void shouldParseEvent() {
        MatchEntity match = new MatchEntity();
        match.setId(20L);
        String input = "[00:22:10.596] npc_dota_hero_rubick hits npc_dota_hero_puck with dota_unknown for 43 damage (650->607)";

        CombatLogEntryEntity combatLogEntry = damageDoneEvent.parse(input, match);

        assertEquals(1330596, combatLogEntry.getTimestamp());
        assertEquals("rubick", combatLogEntry.getActor());
        assertEquals("puck", combatLogEntry.getTarget());
        assertEquals("dota_unknown", combatLogEntry.getAbility());
        assertEquals(43, combatLogEntry.getDamage());
        assertEquals(CombatLogEntryEntity.Type.DAMAGE_DONE, combatLogEntry.getType());
        assertEquals(20, combatLogEntry.getMatch().getId());
    }

    @Test
    void shouldReturnPattern() {
        String expectedPattern = "^\\[(.*)\\] npc_dota_hero_(.*) hits npc_dota_hero_(.*) with (.*) for (\\d+) damage(.*)$";
        assertEquals(expectedPattern, damageDoneEvent.getPattern().pattern());
    }
}
