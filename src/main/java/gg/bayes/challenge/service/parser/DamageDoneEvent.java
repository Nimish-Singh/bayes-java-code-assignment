package gg.bayes.challenge.service.parser;

import gg.bayes.challenge.persistence.model.CombatLogEntryEntity;
import gg.bayes.challenge.persistence.model.MatchEntity;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DamageDoneEvent implements CombatEvent {
    private final Pattern pattern;

    public DamageDoneEvent() {
        pattern = Pattern.compile("^\\[(.*)\\] npc_dota_hero_(.*) hits npc_dota_hero_(.*) with (.*) for (\\d+) damage(.*)$");
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
        combatLogEntry.setActor(matcher.group(2));
        combatLogEntry.setTarget(matcher.group(3));
        combatLogEntry.setAbility(matcher.group(4));
        combatLogEntry.setDamage(Integer.parseInt(matcher.group(5)));
        combatLogEntry.setType(CombatLogEntryEntity.Type.DAMAGE_DONE);
        combatLogEntry.setMatch(m);
        return combatLogEntry;
    }
}
