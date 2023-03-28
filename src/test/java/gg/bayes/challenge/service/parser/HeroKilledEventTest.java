package gg.bayes.challenge.service.parser;

import gg.bayes.challenge.persistence.model.CombatLogEntryEntity;
import gg.bayes.challenge.persistence.model.MatchEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HeroKilledEventTest {
    private final HeroKilledEvent heroKilledEvent;

    public HeroKilledEventTest() {
        this.heroKilledEvent = new HeroKilledEvent();
    }

    @Test
    public void shouldParseEvent() {
        MatchEntity match = new MatchEntity();
        match.setId(20L);
        String input = "[00:22:25.826] npc_dota_hero_rubick is killed by npc_dota_hero_puck";

        CombatLogEntryEntity combatLogEntry = heroKilledEvent.parse(input, match);

        assertEquals(1345826, combatLogEntry.getTimestamp());
        assertEquals("rubick", combatLogEntry.getTarget());
        assertEquals("puck", combatLogEntry.getActor());
        assertEquals(CombatLogEntryEntity.Type.HERO_KILLED, combatLogEntry.getType());
        assertEquals(20, combatLogEntry.getMatch().getId());
    }

    @Test
    void shouldReturnPattern() {
        String expectedPattern = "^\\[(.*)\\] npc_dota_hero_(.*) is killed by npc_dota_hero_(.*)$";
        assertEquals(expectedPattern, heroKilledEvent.getPattern().pattern());
    }

}