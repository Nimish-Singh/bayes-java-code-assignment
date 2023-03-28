package gg.bayes.challenge.service.parser;

import gg.bayes.challenge.persistence.model.CombatLogEntryEntity;
import gg.bayes.challenge.persistence.model.MatchEntity;

import java.time.Duration;
import java.time.LocalTime;
import java.util.regex.Pattern;

public interface CombatEvent {
    Pattern getPattern();

    CombatLogEntryEntity parse(String line, MatchEntity m);

    default Long parseTimestamp(String s) {
        return Duration.between(LocalTime.MIDNIGHT, LocalTime.parse(s)).toMillis();
    }
}
