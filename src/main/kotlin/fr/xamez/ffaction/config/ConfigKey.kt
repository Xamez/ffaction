package fr.xamez.ffaction.config

enum class ConfigKey(val key: String, val defaultValue: Any) {

    FACTION_MIN_POWER("faction.minPower", 0),
    FACTION_MAX_POWER("faction.maxPower", 20),
    FACTION_MAX_CLAIMS("faction.maxClaims", 50),
    FACTION_MAX_MEMBERS("faction.maxMembers", 20),
    FACTION_MAX_FACTIONS("faction.maxFactions", 100),
    FACTION_MIN_FACTION_NAME_LENGTH("faction.minFactionNameLength", 3),
    FACTION_MAX_FACTION_NAME_LENGTH("faction.maxFactionNameLength", 20),
    FACTION_MAX_FACTION_DESCRIPTION_LENGTH("faction.maxFactionDescriptionLength", 120),

    ;

}