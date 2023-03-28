package gg.bayes.challenge.persistence.repository;

import gg.bayes.challenge.DotaChallengeApplication;
import gg.bayes.challenge.persistence.model.CombatLogEntryEntity;
import gg.bayes.challenge.persistence.model.HeroDamageSummary;
import gg.bayes.challenge.persistence.model.HeroKillSummary;
import gg.bayes.challenge.persistence.model.ItemPurchaseSummary;
import gg.bayes.challenge.persistence.model.MatchEntity;
import gg.bayes.challenge.persistence.model.SpellCastSummary;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = DotaChallengeApplication.class)
class CombatLogEntryRepositoryTest {
    private final CombatLogEntryRepository repository;
    private final MatchRepository matchRepository;

    @Autowired
    public CombatLogEntryRepositoryTest(CombatLogEntryRepository repository, MatchRepository matchRepository) {
        this.repository = repository;
        this.matchRepository = matchRepository;
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll();
        matchRepository.deleteAll();
    }

    @Test
    void shouldReturnItemPurchaseSummaryWhenPresent() {
        MatchEntity match = new MatchEntity();
        CombatLogEntryEntity hero1Item1 = getItemPurchasedLogEntry(100L, "hero-1", "item-1");
        CombatLogEntryEntity hero1Item2 = getItemPurchasedLogEntry(150L, "hero-1", "item-2");
        CombatLogEntryEntity hero2Item1 = getItemPurchasedLogEntry(200L, "hero-2", "item-3");
        hero1Item1.setMatch(match);
        hero1Item2.setMatch(match);
        hero2Item1.setMatch(match);

        match.setCombatLogEntries(Set.of(hero1Item1, hero1Item2, hero2Item1));

        Long matchId = matchRepository.save(match).getId();

        List<ItemPurchaseSummary> summary = repository.getSummaryByMatchIdAndActorAndType(matchId, "hero-1", CombatLogEntryEntity.Type.ITEM_PURCHASED);

        assertEquals(2, summary.size());

        List<ItemPurchaseSummary> summaryList = new ArrayList<>(summary);
        summaryList.sort(Comparator.comparing(ItemPurchaseSummary::getTimestamp));

        assertEquals("item-1", summaryList.get(0).getItem());
        assertEquals(100, summaryList.get(0).getTimestamp());
        assertEquals("item-2", summaryList.get(1).getItem());
        assertEquals(150, summaryList.get(1).getTimestamp());
    }

    @Test
    void shouldReturnItemPurchaseSummaryWhenNotPresent() {
        List<ItemPurchaseSummary> summary = repository.getSummaryByMatchIdAndActorAndType(11L, "non-existent actor", CombatLogEntryEntity.Type.ITEM_PURCHASED);

        assertTrue(summary.isEmpty());
    }

    @Test
    void shouldReturnHeroKillSummaryWhenPresent() {
        MatchEntity match = new MatchEntity();
        CombatLogEntryEntity hero1Kill1 = getHeroKilledLogEntry(10L, "hero-1", "target-1");
        CombatLogEntryEntity hero1Kill2 = getHeroKilledLogEntry(15L, "hero-1", "target-1");
        CombatLogEntryEntity hero2Kill1 = getHeroKilledLogEntry(20L, "hero-2", "target-3");
        hero1Kill1.setMatch(match);
        hero1Kill2.setMatch(match);
        hero2Kill1.setMatch(match);

        match.setCombatLogEntries(Set.of(hero1Kill1, hero1Kill2, hero2Kill1));

        Long matchId = matchRepository.save(match).getId();

        List<HeroKillSummary> summary = repository.getHeroKillSummary(matchId, CombatLogEntryEntity.Type.HERO_KILLED);

        assertEquals(2, summary.size());

        List<HeroKillSummary> summaryList = new ArrayList<>(summary);
        summaryList.sort(Comparator.comparing(HeroKillSummary::getKills));

        assertEquals("hero-2", summaryList.get(0).getActor());
        assertEquals(1, summaryList.get(0).getKills());
        assertEquals("hero-1", summaryList.get(1).getActor());
        assertEquals(2, summaryList.get(1).getKills());
    }

    @Test
    void shouldReturnHeroKillSummaryWhenNotPresent() {
        List<HeroKillSummary> summary = repository.getHeroKillSummary(11L, CombatLogEntryEntity.Type.HERO_KILLED);

        assertTrue(summary.isEmpty());
    }

    @Test
    void shouldReturnSpellCastSummaryWhenPresent() {
        MatchEntity match = new MatchEntity();
        CombatLogEntryEntity hero1Spell1 = getSpellCastLogEntry(300L, "ability-1", "hero-1", "target-1");
        CombatLogEntryEntity hero1Spell2 = getSpellCastLogEntry(350L, "ability-1", "hero-1", "target-1");
        CombatLogEntryEntity hero2Spell1 = getSpellCastLogEntry(400L, "ability-2", "hero-2", "target-3");
        hero1Spell1.setMatch(match);
        hero1Spell2.setMatch(match);
        hero2Spell1.setMatch(match);

        match.setCombatLogEntries(Set.of(hero1Spell1, hero1Spell2, hero2Spell1));

        Long matchId = matchRepository.save(match).getId();

        List<SpellCastSummary> summary = repository.getSpellCastSummary(matchId, "hero-1", CombatLogEntryEntity.Type.SPELL_CAST);

        assertEquals(1, summary.size());

        List<SpellCastSummary> summaryList = new ArrayList<>(summary);

        assertEquals("ability-1", summaryList.get(0).getSpell());
        assertEquals(2, summaryList.get(0).getCasts());
    }

    @Test
    void shouldReturnSpellCastSummaryWhenNotPresent() {
        List<SpellCastSummary> summary = repository.getSpellCastSummary(11L, "non-existent actor", CombatLogEntryEntity.Type.SPELL_CAST);

        assertTrue(summary.isEmpty());
    }

    @Test
    void shouldReturnHeroDamageSummaryWhenPresent() {
        MatchEntity match = new MatchEntity();
        CombatLogEntryEntity hero1Damage1 = getHeroDamageLogEntry(500L, 16, "hero-1", "target-1");
        CombatLogEntryEntity hero1Damage2 = getHeroDamageLogEntry(550L, 17, "hero-1", "target-1");
        CombatLogEntryEntity hero1Damage3 = getHeroDamageLogEntry(600L, 20, "hero-1", "target-2");
        CombatLogEntryEntity hero2Damage1 = getHeroDamageLogEntry(650L, 30, "hero-2", "target-3");
        hero1Damage1.setMatch(match);
        hero1Damage2.setMatch(match);
        hero1Damage3.setMatch(match);
        hero2Damage1.setMatch(match);

        match.setCombatLogEntries(Set.of(hero1Damage1, hero1Damage2, hero1Damage3, hero2Damage1));

        Long matchId = matchRepository.save(match).getId();

        List<HeroDamageSummary> summary = repository.getHeroDamageSummary(matchId, "hero-1", CombatLogEntryEntity.Type.DAMAGE_DONE);

        assertEquals(2, summary.size());

        List<HeroDamageSummary> summaryList = new ArrayList<>(summary);
        summaryList.sort(Comparator.comparing(HeroDamageSummary::getDamageInstances));

        assertEquals("target-2", summaryList.get(0).getTarget());
        assertEquals(1, summaryList.get(0).getDamageInstances());
        assertEquals(20, summaryList.get(0).getTotalDamage());
        assertEquals("target-1", summaryList.get(1).getTarget());
        assertEquals(2, summaryList.get(1).getDamageInstances());
        assertEquals(33, summaryList.get(1).getTotalDamage());
    }

    @Test
    void shouldReturnHeroDamageSummaryWhenNotPresent() {
        List<HeroDamageSummary> summary = repository.getHeroDamageSummary(11L, "non-existent actor", CombatLogEntryEntity.Type.DAMAGE_DONE);

        assertTrue(summary.isEmpty());
    }

    private CombatLogEntryEntity getItemPurchasedLogEntry(Long timestamp, String actor, String item) {
        CombatLogEntryEntity itemPurchaseLogEntry = new CombatLogEntryEntity();
        itemPurchaseLogEntry.setTimestamp(timestamp);
        itemPurchaseLogEntry.setType(CombatLogEntryEntity.Type.ITEM_PURCHASED);
        itemPurchaseLogEntry.setActor(actor);
        itemPurchaseLogEntry.setItem(item);
        return itemPurchaseLogEntry;
    }

    private CombatLogEntryEntity getHeroKilledLogEntry(Long timestamp, String actor, String target) {
        CombatLogEntryEntity heroKillLogEntry = new CombatLogEntryEntity();
        heroKillLogEntry.setTimestamp(timestamp);
        heroKillLogEntry.setType(CombatLogEntryEntity.Type.HERO_KILLED);
        heroKillLogEntry.setActor(actor);
        heroKillLogEntry.setTarget(target);
        return heroKillLogEntry;
    }

    private CombatLogEntryEntity getSpellCastLogEntry(Long timestamp, String ability, String actor, String target) {
        CombatLogEntryEntity spellCastLogEntry = new CombatLogEntryEntity();
        spellCastLogEntry.setTimestamp(timestamp);
        spellCastLogEntry.setType(CombatLogEntryEntity.Type.SPELL_CAST);
        spellCastLogEntry.setAbility(ability);
        spellCastLogEntry.setActor(actor);
        spellCastLogEntry.setTarget(target);
        return spellCastLogEntry;
    }

    private CombatLogEntryEntity getHeroDamageLogEntry(Long timestamp, Integer damage, String actor, String target) {
        CombatLogEntryEntity spellCastLogEntry = new CombatLogEntryEntity();
        spellCastLogEntry.setTimestamp(timestamp);
        spellCastLogEntry.setType(CombatLogEntryEntity.Type.DAMAGE_DONE);
        spellCastLogEntry.setDamage(damage);
        spellCastLogEntry.setActor(actor);
        spellCastLogEntry.setTarget(target);
        return spellCastLogEntry;
    }
}
