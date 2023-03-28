package gg.bayes.challenge.rest.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/*
 * Integration test template to get you started. Add tests and make modifications as you see fit.
 */
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class MatchControllerIntegrationTest {

    private static final String COMBATLOG_FILE_1 = "/data/combatlog_1.log.txt";
    private static final String COMBATLOG_FILE_2 = "/data/combatlog_2.log.txt";

    @Autowired
    private MockMvc mvc;

    private Map<String, Long> matchIds;

    @BeforeAll
    void setup() throws Exception {
        // Populate the database with all events from both sample data files and store the returned
        // match IDS.
        matchIds = Map.of(
                COMBATLOG_FILE_1, ingestMatch(COMBATLOG_FILE_1),
                COMBATLOG_FILE_2, ingestMatch(COMBATLOG_FILE_2));
    }

    @Test
    void shouldReturnListOfHeroesAndTheirKills() throws Exception {
        mvc.perform(get("/api/match/{matchId}", matchIds.get(COMBATLOG_FILE_1)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().string("[{\"hero\":\"abyssal_underlord\",\"kills\":6},{\"hero\":\"bane\",\"kills\":2},{\"hero\":\"bloodseeker\",\"kills\":11},{\"hero\":\"death_prophet\",\"kills\":9},{\"hero\":\"dragon_knight\",\"kills\":3},{\"hero\":\"mars\",\"kills\":6},{\"hero\":\"pangolier\",\"kills\":5},{\"hero\":\"puck\",\"kills\":7},{\"hero\":\"rubick\",\"kills\":4},{\"hero\":\"snapfire\",\"kills\":2}]"));

        mvc.perform(get("/api/match/{matchId}", matchIds.get(COMBATLOG_FILE_2)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().string("[{\"hero\":\"centaur\",\"kills\":1},{\"hero\":\"earthshaker\",\"kills\":2},{\"hero\":\"ember_spirit\",\"kills\":14},{\"hero\":\"grimstroke\",\"kills\":4},{\"hero\":\"gyrocopter\",\"kills\":3},{\"hero\":\"lycan\",\"kills\":2},{\"hero\":\"mars\",\"kills\":3},{\"hero\":\"monkey_king\",\"kills\":7},{\"hero\":\"rubick\",\"kills\":3}]"));
    }

    @Test
    void shouldReturnEmptyListOfHeroesAndTheirKillsForNonExistentMatch() throws Exception {
        mvc.perform(get("/api/match/{matchId}", 100))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().string("[]"));
    }

    @Test
    void shouldReturnListOfItemsPurchased() throws Exception {
        mvc.perform(get("/api/match/{matchId}/{heroName}/items", matchIds.get(COMBATLOG_FILE_1), "abyssal_underlord"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(30));

        mvc.perform(get("/api/match/{matchId}/{heroName}/items", matchIds.get(COMBATLOG_FILE_2), "centaur"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(24));
    }

    @Test
    void shouldReturnEmptyListOfItemsPurchasedForNonExistentMatch() throws Exception {
        mvc.perform(get("/api/match/{matchId}/{heroName}/items", 100, "centaur"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().string("[]"));
    }

    @Test
    void shouldReturnEmptyListOfItemsPurchasedForNonExistentHero() throws Exception {
        mvc.perform(get("/api/match/{matchId}/{heroName}/items", matchIds.get(COMBATLOG_FILE_1), "nonExistentHero"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().string("[]"));
    }

    @Test
    void shouldReturnListOfSpellsCast() throws Exception {
        mvc.perform(get("/api/match/{matchId}/{heroName}/spells", matchIds.get(COMBATLOG_FILE_1), "abyssal_underlord"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().string("[{\"spell\":\"abyssal_underlord_cancel_dark_rift\",\"casts\":1},{\"spell\":\"abyssal_underlord_dark_rift\",\"casts\":3},{\"spell\":\"abyssal_underlord_firestorm\",\"casts\":67},{\"spell\":\"abyssal_underlord_pit_of_malice\",\"casts\":14}]"));

        mvc.perform(get("/api/match/{matchId}/{heroName}/spells", matchIds.get(COMBATLOG_FILE_2), "centaur"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().string("[{\"spell\":\"centaur_double_edge\",\"casts\":27},{\"spell\":\"centaur_hoof_stomp\",\"casts\":8},{\"spell\":\"centaur_return\",\"casts\":4},{\"spell\":\"centaur_stampede\",\"casts\":5}]"));
    }

    @Test
    void shouldReturnEmptyListOfSpellsCastForNonExistentMatch() throws Exception {
        mvc.perform(get("/api/match/{matchId}/{heroName}/spells", 100, "centaur"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().string("[]"));
    }

    @Test
    void shouldReturnEmptyListOfSpellsCastForNonExistentHero() throws Exception {
        mvc.perform(get("/api/match/{matchId}/{heroName}/spells", matchIds.get(COMBATLOG_FILE_1), "nonExistentHero"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().string("[]"));
    }

    @Test
    void shouldReturnListOfHeroDamages() throws Exception {
        mvc.perform(get("/api/match/{matchId}/{heroName}/damage", matchIds.get(COMBATLOG_FILE_1), "abyssal_underlord"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().string("[{\"target\":\"bane\",\"damage_instances\":68,\"total_damage\":3483},{\"target\":\"bloodseeker\",\"damage_instances\":196,\"total_damage\":6172},{\"target\":\"death_prophet\",\"damage_instances\":76,\"total_damage\":5865},{\"target\":\"mars\",\"damage_instances\":22,\"total_damage\":1450},{\"target\":\"rubick\",\"damage_instances\":28,\"total_damage\":1690}]"));

        mvc.perform(get("/api/match/{matchId}/{heroName}/damage", matchIds.get(COMBATLOG_FILE_2), "centaur"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().string("[{\"target\":\"centaur\",\"damage_instances\":27,\"total_damage\":7015},{\"target\":\"ember_spirit\",\"damage_instances\":32,\"total_damage\":1520},{\"target\":\"grimstroke\",\"damage_instances\":8,\"total_damage\":1187},{\"target\":\"lycan\",\"damage_instances\":27,\"total_damage\":1578},{\"target\":\"mars\",\"damage_instances\":18,\"total_damage\":1487}]"));
    }

    @Test
    void shouldReturnEmptyListOfHeroDamagesForNonExistentMatch() throws Exception {
        mvc.perform(get("/api/match/{matchId}/{heroName}/damage", 100, "centaur"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().string("[]"));
    }

    @Test
    void shouldReturnEmptyListOfHeroDamagesForNonExistentHero() throws Exception {
        mvc.perform(get("/api/match/{matchId}/{heroName}/damage", matchIds.get(COMBATLOG_FILE_1), "nonExistentHero"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().string("[]"));
    }

    /**
     * Helper method that ingests a combat log file and returns the match id associated with all parsed events.
     *
     * @param file file path as a classpath resource, e.g.: /data/combatlog_1.log.txt.
     * @return the id of the match associated with the events parsed from the given file
     * @throws Exception if an error happens when reading or ingesting the file
     */
    private Long ingestMatch(String file) throws Exception {
        String fileContent = IOUtils.resourceToString(file, StandardCharsets.UTF_8);

        return Long.parseLong(mvc.perform(post("/api/match")
                                         .contentType(MediaType.TEXT_PLAIN)
                                         .content(fileContent))
                                 .andReturn()
                                 .getResponse()
                                 .getContentAsString());
    }
}
