package gg.bayes.challenge.service.parser;

import gg.bayes.challenge.persistence.model.CombatLogEntryEntity;
import gg.bayes.challenge.persistence.model.MatchEntity;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class HeroKilledEvent implements CombatEvent {
    private final Pattern pattern;

    public HeroKilledEvent() {
        pattern = Pattern.compile("^\\[(.*)\\] npc_dota_hero_(.*) is killed by npc_dota_hero_(.*)$");
    }

    @Override
    public Pattern getPattern() {
        return pattern;
    }

    @Override
    public CombatLogEntryEntity parse(String line, MatchEntity m) {
        Matcher matcher = pattern.matcher(line);
        matcher.matches();
        CombatLogEntryEntity combatLogEntry = new CombatLogEntryEntity();
        combatLogEntry.setTimestamp(parseTimestamp(matcher.group(1)));
        combatLogEntry.setTarget(matcher.group(2));
        combatLogEntry.setActor(matcher.group(3));
        combatLogEntry.setType(CombatLogEntryEntity.Type.HERO_KILLED);
        combatLogEntry.setMatch(m);
        return combatLogEntry;
    }
}
