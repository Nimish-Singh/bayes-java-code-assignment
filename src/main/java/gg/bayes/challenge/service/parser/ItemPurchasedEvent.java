package gg.bayes.challenge.service.parser;

import gg.bayes.challenge.persistence.model.CombatLogEntryEntity;
import gg.bayes.challenge.persistence.model.MatchEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ItemPurchasedEvent implements CombatEvent {
    private final Pattern pattern;

    public ItemPurchasedEvent() {
        pattern = Pattern.compile("^\\[(.*)\\] npc_dota_hero_(.*) buys item item_(.*)$");
    }

    @Override
    public Pattern getPattern() {
        return pattern;
    }

    @Override
    public CombatLogEntryEntity parse(String line, MatchEntity match) {
        Matcher matcher = pattern.matcher(line);
        matcher.matches();
        CombatLogEntryEntity combatLogEntry = new CombatLogEntryEntity();
        combatLogEntry.setTimestamp(parseTimestamp(matcher.group(1)));
        combatLogEntry.setActor(matcher.group(2));
        combatLogEntry.setItem(matcher.group(3));
        combatLogEntry.setType(CombatLogEntryEntity.Type.ITEM_PURCHASED);
        combatLogEntry.setMatch(match);
        return combatLogEntry;
    }
}
