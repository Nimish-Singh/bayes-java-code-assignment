package gg.bayes.challenge.persistence.repository;

import gg.bayes.challenge.persistence.model.CombatLogEntryEntity;
import gg.bayes.challenge.persistence.model.HeroDamageSummary;
import gg.bayes.challenge.persistence.model.HeroKillSummary;
import gg.bayes.challenge.persistence.model.ItemPurchaseSummary;
import gg.bayes.challenge.persistence.model.SpellCastSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CombatLogEntryRepository extends JpaRepository<CombatLogEntryEntity, Long> {
    List<ItemPurchaseSummary> getSummaryByMatchIdAndActorAndType(Long matchId, String actor, CombatLogEntryEntity.Type type);

    @Query("SELECT c.actor AS actor, COUNT(c.target) AS kills " +
        "FROM CombatLogEntryEntity c " +
        "WHERE c.match.id = :matchId AND c.type = :type " +
        "GROUP BY c.actor")
    List<HeroKillSummary> getHeroKillSummary(Long matchId, CombatLogEntryEntity.Type type);

    @Query("SELECT c.ability AS spell, COUNT(c.target) AS casts " +
        "FROM CombatLogEntryEntity c " +
        "WHERE c.match.id = :matchId AND c.actor = :actor AND c.type = :type " +
        "GROUP BY c.ability")
    List<SpellCastSummary> getSpellCastSummary(Long matchId, String actor, CombatLogEntryEntity.Type type);

    @Query("SELECT c.target AS target, COUNT(c.damage) AS damageInstances, SUM(c.damage) AS totalDamage " +
        "FROM CombatLogEntryEntity c " +
        "WHERE c.match.id = :matchId AND c.actor = :actor AND c.type = :type " +
        "GROUP BY c.target")
    List<HeroDamageSummary> getHeroDamageSummary(Long matchId, String actor, CombatLogEntryEntity.Type type);
}
