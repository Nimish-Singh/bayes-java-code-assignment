package gg.bayes.challenge.service;

import gg.bayes.challenge.persistence.model.CombatLogEntryEntity;
import gg.bayes.challenge.persistence.model.HeroDamageSummary;
import gg.bayes.challenge.persistence.model.HeroKillSummary;
import gg.bayes.challenge.persistence.model.ItemPurchaseSummary;
import gg.bayes.challenge.persistence.model.MatchEntity;
import gg.bayes.challenge.persistence.model.SpellCastSummary;
import gg.bayes.challenge.persistence.repository.CombatLogEntryRepository;
import gg.bayes.challenge.persistence.repository.MatchRepository;
import gg.bayes.challenge.rest.model.HeroDamage;
import gg.bayes.challenge.rest.model.HeroItem;
import gg.bayes.challenge.rest.model.HeroKills;
import gg.bayes.challenge.rest.model.HeroSpells;
import gg.bayes.challenge.service.parser.CombatEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MatchLogServiceTest {
    private final MatchRepository matchRepository;
    private final CombatLogEntryRepository combatLogEntryRepository;
    private final MatchLogService service;
    private final CombatEvent aCombatEvent;
    private final CombatEvent anotherCombatEvent;

    private final Long matchId;
    private final String hero;

    public MatchLogServiceTest() {
        this.matchRepository = mock(MatchRepository.class);
        this.combatLogEntryRepository = mock(CombatLogEntryRepository.class);

        this.aCombatEvent = mock(CombatEvent.class);
        when(aCombatEvent.getPattern()).thenReturn(Pattern.compile("aPattern"));
        this.anotherCombatEvent = mock(CombatEvent.class);
        when(anotherCombatEvent.getPattern()).thenReturn(Pattern.compile("anotherPattern"));

        matchId = 4L;
        hero = "hero";

        service = new MatchLogService(List.of(aCombatEvent, anotherCombatEvent), matchRepository, combatLogEntryRepository);
    }

    @Test
    void shouldParseAndSaveInput() {
        String lineMatchingAPattern = "line with aPattern", lineMatchingAnotherPattern = "line with anotherPattern", nonMatchingLine = "non-matching line";
        String payload = String.format("%s\n%s\n%s", lineMatchingAPattern, lineMatchingAnotherPattern, nonMatchingLine);
        CombatLogEntryEntity aCombatLogEntry = mock(CombatLogEntryEntity.class);
        CombatLogEntryEntity anotherCombatLogEntry = mock(CombatLogEntryEntity.class);
        MatchEntity savedMatch = mock(MatchEntity.class);

        when(aCombatEvent.parse(eq(lineMatchingAPattern), any())).thenReturn(aCombatLogEntry);
        when(anotherCombatEvent.parse(eq(lineMatchingAnotherPattern), any())).thenReturn(anotherCombatLogEntry);
        when(matchRepository.save(any())).thenReturn(savedMatch);
        when(savedMatch.getId()).thenReturn(matchId);

        Long matchId = service.ingestMatch(payload);

        assertEquals(this.matchId, matchId);
    }

    @Test
    void shouldReturnHeroKills() {
        HeroKillSummary aSummary = mock(HeroKillSummary.class);
        HeroKillSummary anotherSummary = mock(HeroKillSummary.class);

        when(aSummary.getActor()).thenReturn("hero-1");
        when(aSummary.getKills()).thenReturn(4);
        when(anotherSummary.getActor()).thenReturn("hero-2");
        when(anotherSummary.getKills()).thenReturn(6);
        when(combatLogEntryRepository.getHeroKillSummary(matchId, CombatLogEntryEntity.Type.HERO_KILLED)).thenReturn(List.of(aSummary, anotherSummary));

        List<HeroKills> heroKills = service.heroKills(matchId);

        assertEquals(2, heroKills.size());
        assertEquals("hero-1", heroKills.get(0).getHero());
        assertEquals(4, heroKills.get(0).getKills());
        assertEquals("hero-2", heroKills.get(1).getHero());
        assertEquals(6, heroKills.get(1).getKills());
    }

    @Test
    void shouldReturnHeroItems() {
        ItemPurchaseSummary aSummary = mock(ItemPurchaseSummary.class);
        ItemPurchaseSummary anotherSummary = mock(ItemPurchaseSummary.class);

        when(aSummary.getItem()).thenReturn("item-1");
        when(aSummary.getTimestamp()).thenReturn(4L);
        when(anotherSummary.getItem()).thenReturn("item-2");
        when(anotherSummary.getTimestamp()).thenReturn(6L);
        when(combatLogEntryRepository.getSummaryByMatchIdAndActorAndType(matchId, hero, CombatLogEntryEntity.Type.ITEM_PURCHASED)).thenReturn(List.of(aSummary, anotherSummary));

        List<HeroItem> heroItems = service.itemsPurchased(matchId, hero);

        assertEquals(2, heroItems.size());
        assertEquals("item-1", heroItems.get(0).getItem());
        assertEquals(4, heroItems.get(0).getTimestamp());
        assertEquals("item-2", heroItems.get(1).getItem());
        assertEquals(6, heroItems.get(1).getTimestamp());
    }

    @Test
    void shouldReturnHeroSpells() {
        SpellCastSummary aSummary = mock(SpellCastSummary.class);
        SpellCastSummary anotherSummary = mock(SpellCastSummary.class);

        when(aSummary.getSpell()).thenReturn("spell-1");
        when(aSummary.getCasts()).thenReturn(4);
        when(anotherSummary.getSpell()).thenReturn("spell-2");
        when(anotherSummary.getCasts()).thenReturn(6);
        when(combatLogEntryRepository.getSpellCastSummary(matchId, hero, CombatLogEntryEntity.Type.SPELL_CAST)).thenReturn(List.of(aSummary, anotherSummary));

        List<HeroSpells> heroSpells = service.spellCasts(matchId, hero);

        assertEquals(2, heroSpells.size());
        assertEquals("spell-1", heroSpells.get(0).getSpell());
        assertEquals(4, heroSpells.get(0).getCasts());
        assertEquals("spell-2", heroSpells.get(1).getSpell());
        assertEquals(6, heroSpells.get(1).getCasts());
    }

    @Test
    void shouldReturnDamagesDone() {
        HeroDamageSummary aSummary = mock(HeroDamageSummary.class);
        HeroDamageSummary anotherSummary = mock(HeroDamageSummary.class);

        when(aSummary.getTarget()).thenReturn("target-1");
        when(aSummary.getDamageInstances()).thenReturn(4);
        when(aSummary.getTotalDamage()).thenReturn(30);
        when(anotherSummary.getTarget()).thenReturn("target-2");
        when(anotherSummary.getDamageInstances()).thenReturn(6);
        when(anotherSummary.getTotalDamage()).thenReturn(50);
        when(combatLogEntryRepository.getHeroDamageSummary(matchId, hero, CombatLogEntryEntity.Type.DAMAGE_DONE)).thenReturn(List.of(aSummary, anotherSummary));

        List<HeroDamage> heroDamages = service.damageDone(matchId, hero);

        assertEquals(2, heroDamages.size());
        assertEquals("target-1", heroDamages.get(0).getTarget());
        assertEquals(4, heroDamages.get(0).getDamageInstances());
        assertEquals(30, heroDamages.get(0).getTotalDamage());
        assertEquals("target-2", heroDamages.get(1).getTarget());
        assertEquals(6, heroDamages.get(1).getDamageInstances());
        assertEquals(50, heroDamages.get(1).getTotalDamage());
    }
}
