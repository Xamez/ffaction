package fr.xamez.ffaction.localization

enum class LocalizationKey(val key: String) {

    COMMAND_HELP_USAGE_HEADER("command.help.usage_header"),
    COMMAND_RELOAD_SUCCESS("command.reload.success"),
    COMMAND_RELOAD_ERROR("command.reload.error"),
    COMMAND_CREATE_SUCCESS("command.create.success"),
    COMMAND_CREATE_ERROR("command.create.error"),
    COMMAND_DISBAND_SUCCESS("command.disband.success"),
    COMMAND_DISBAND_ERROR("command.disband.error"),

    COMMAND_NOT_IN_FACTION("command.error.not_in_faction"),
    COMMAND_LEADER_ONLY("command.error.leader_only"),
    COMMAND_PLAYER_NOT_FOUND("command.error.player_not_found"),
    COMMAND_PLAYER_ONLY("command.error.player_only"),

    ;

}