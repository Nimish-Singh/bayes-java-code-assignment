# Bayes Java Dota Challenge

This is the [task](TASK.md).

This application can be started via `mvn spring-boot:run` on the CLI (from the root folder of this project). All the
APIs of this application can then be accessed from the Swagger page at `localhost:8080/swagger-ui/`
The application ingests a file of log entries, parses some specific events (as mentioned in the [task](TASK.md) file)
and stores them in the in-memory H2 database.

# Solution overview:

* The events to be parsed and the rules to parse the events have ben derived from the instructions in
  the [task](TASK.md) file
* Parsing of events has been implemented via a strategy pattern. There is one interface `CombatEvent`, along with
  implementation classes- one for each kind of event to be parsed.
  This implementation allows the system to be extensible- if a new event needs to be added, then it can simply be
  added as an implementation of `CombatEvent` interface, and its parsing shall be supported out of the box.
* The entity shared as part of the codebase (`CombatLogEntryEntity`) has been used to persist all events. Each
  event `Type` contains a different set of attributes that are stored within the same entity.
  `CombatLogEntryEntity` contains a union of all the fields that each of the respective event contains. The different
  fields that these events contain are summarised below:

| Event          | Actor | Target | Ability | AbilityLevel | Item | Damage |
|----------------|-------|--------|---------|--------------|------|--------|
| ITEM_PURCHASED | ✅     |       |         |              | ✅    |        |
| HERO_KILLED    | ✅     | ✅     |         |              |      |        |
| SPELL_CAST     | ✅     | ✅     | ✅       | ✅           |      |        |
| DAMAGE_DONE    | ✅     | ✅     | ✅       |              |      | ✅     |

This entity structure allows us to make use of a single table to store all events; but makes it slightly cumbersome to
add new event types. One way to address that would be to decompose this entity into an inheritance structure-
where the common fields are part of the base entity class and each event of interest can extend that base class and
support a specific event type

* The repositories have minimal code- only as much as was required
* SQL projection has been achieved in the repository via interfaces (`HeroDamageSummary`, `HeroKillSummary`
  , `ItemPurchaseSummary`, `SpellCastSummary`), which have been clubbed with entity models
* No global exception handler has been added to the project. The aim has been to not add more code than what is
  necessary to achieve the required functionality
* Some logging has been added to aid in debugging
* Test coverage has been kept high- covering positive as well as negative scenarios. Each class is unit-tested, while
  integration tests have been added in `MatchControllerIntegrationTest` 
