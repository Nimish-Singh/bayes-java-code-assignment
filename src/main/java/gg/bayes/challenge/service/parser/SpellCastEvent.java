package gg.bayes.challenge.service.parser;

import gg.bayes.challenge.persistence.model.CombatLogEntryEntity;
import gg.bayes.challenge.persistence.model.MatchEntity;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SpellCastEvent implements CombatEvent {
    private final Pattern pattern;

    public SpellCastEvent() {
        this.pattern = Pattern.compile("^\\[(.*)\\] npc_dota_hero_(.*) casts ability (.*) \\(lvl (\\d+)\\) on (.*)$");
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
        combatLogEntry.setAbility(matcher.group(3));
        combatLogEntry.setAbilityLevel(Integer.parseInt(matcher.group(4)));
        combatLogEntry.setType(CombatLogEntryEntity.Type.SPELL_CAST);

        String target;
        if (matcher.group(5).contains("npc_dota_hero_")) {
            target = matcher.group(5).substring(14);
        } else {
            target = matcher.group(5);
        }
        combatLogEntry.setTarget(target);

        combatLogEntry.setMatch(m);
        return combatLogEntry;
    }
}
