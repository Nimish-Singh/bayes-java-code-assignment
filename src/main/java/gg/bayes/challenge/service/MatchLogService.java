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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MatchLogService {
    private final Map<Pattern, CombatEvent> eventParsers;
    private final MatchRepository matchRepository;
    private final CombatLogEntryRepository combatLogEntryRepository;

    @Autowired
    public MatchLogService(List<CombatEvent> combatEvents, MatchRepository matchRepository, CombatLogEntryRepository combatLogEntryRepository) {
        this.matchRepository = matchRepository;
        this.combatLogEntryRepository = combatLogEntryRepository;
        this.eventParsers = combatEvents
            .stream()
            .collect(Collectors.toMap(CombatEvent::getPattern, eventParser -> eventParser));
    }

    public Long ingestMatch(String payload) {
        log.info("Processing request to ingest combat log");

        MatchEntity match = new MatchEntity();

        Set<CombatLogEntryEntity> combatLogEntries = payload
            .lines()
            .parallel()
            .map(line ->
                eventParsers.keySet().stream()
                    .filter(pattern -> pattern.matcher(line).matches())
                    .map(pattern -> eventParsers.get(pattern).parse(line, match))
                    .findFirst()
            )
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toSet());

        match.setCombatLogEntries(combatLogEntries);

        Long matchId = matchRepository.save(match).getId();

        log.info(String.format("Processed request to ingest combat log with match ID: %s", matchId));
        return matchId;
    }

    public List<HeroKills> heroKills(Long matchId) {
        List<HeroKillSummary> summary = combatLogEntryRepository.getHeroKillSummary(matchId, CombatLogEntryEntity.Type.HERO_KILLED);
        log.debug(String.format("Processed request to fetch hero kills with size: %s", summary.size()));

        return summary
            .stream()
            .map(kill -> new HeroKills(kill.getActor(), kill.getKills()))
            .collect(Collectors.toList());
    }

    public List<HeroItem> itemsPurchased(Long matchId, String hero) {
        List<ItemPurchaseSummary> summary = combatLogEntryRepository.getSummaryByMatchIdAndActorAndType(matchId, hero, CombatLogEntryEntity.Type.ITEM_PURCHASED);
        log.debug(String.format("Processed request to fetch items purchased with size: %s", summary.size()));

        return summary
            .stream()
            .map(item -> new HeroItem(item.getItem(), item.getTimestamp()))
            .collect(Collectors.toList());
    }

    public List<HeroSpells> spellCasts(Long matchId, String hero) {
        List<SpellCastSummary> summary = combatLogEntryRepository.getSpellCastSummary(matchId, hero, CombatLogEntryEntity.Type.SPELL_CAST);
        log.debug(String.format("Processed request to fetch spells cast with size: %s", summary.size()));

        return summary
            .stream()
            .map(spell -> new HeroSpells(spell.getSpell(), spell.getCasts()))
            .collect(Collectors.toList());
    }

    public List<HeroDamage> damageDone(Long matchId, String hero) {
        List<HeroDamageSummary> summary = combatLogEntryRepository.getHeroDamageSummary(matchId, hero, CombatLogEntryEntity.Type.DAMAGE_DONE);
        log.debug(String.format("Processed request to fetch damages done with size: %s", summary.size()));

        return summary
            .stream()
            .map(damage -> new HeroDamage(damage.getTarget(), damage.getDamageInstances(), damage.getTotalDamage()))
            .collect(Collectors.toList());
    }
}
