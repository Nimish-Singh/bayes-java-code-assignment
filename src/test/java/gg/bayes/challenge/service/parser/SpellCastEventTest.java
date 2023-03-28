package gg.bayes.challenge.service.parser;

import gg.bayes.challenge.persistence.model.CombatLogEntryEntity;
import gg.bayes.challenge.persistence.model.MatchEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpellCastEventTest {
    private final SpellCastEvent spellCastEvent;

    public SpellCastEventTest() {
        this.spellCastEvent = new SpellCastEvent();
    }

    @Test
    public void shouldParseEventWhenTargetIsHero() {
        MatchEntity match = new MatchEntity();
        match.setId(20L);
        String input = "[00:23:59.570] npc_dota_hero_bane casts ability bane_nightmare (lvl 2) on npc_dota_hero_bane";

        CombatLogEntryEntity combatLogEntry = spellCastEvent.parse(input, match);

        assertEquals(1439570, combatLogEntry.getTimestamp());
        assertEquals("bane", combatLogEntry.getActor());
        assertEquals("bane", combatLogEntry.getTarget());
        assertEquals("bane_nightmare", combatLogEntry.getAbility());
        assertEquals(2, combatLogEntry.getAbilityLevel());
        assertEquals(CombatLogEntryEntity.Type.SPELL_CAST, combatLogEntry.getType());
        assertEquals(20, combatLogEntry.getMatch().getId());
    }

    @Test
    public void shouldParseEventWhenTargetIsNotHero() {
        MatchEntity match = new MatchEntity();
        match.setId(20L);
        String input = "[00:24:00.969] npc_dota_hero_mars casts ability mars_spear (lvl 4) on dota_unknown";

        CombatLogEntryEntity combatLogEntry = spellCastEvent.parse(input, match);

        assertEquals(1440969, combatLogEntry.getTimestamp());
        assertEquals("mars", combatLogEntry.getActor());
        assertEquals("dota_unknown", combatLogEntry.getTarget());
        assertEquals("mars_spear", combatLogEntry.getAbility());
        assertEquals(4, combatLogEntry.getAbilityLevel());
        assertEquals(CombatLogEntryEntity.Type.SPELL_CAST, combatLogEntry.getType());
        assertEquals(20, combatLogEntry.getMatch().getId());
    }

    @Test
    void shouldReturnPattern() {
        String expectedPattern = "^\\[(.*)\\] npc_dota_hero_(.*) casts ability (.*) \\(lvl (\\d+)\\) on (.*)$";
        assertEquals(expectedPattern, spellCastEvent.getPattern().pattern());
    }

}