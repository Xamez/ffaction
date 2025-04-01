package fr.xamez.ffaction.command.subcommand

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import fr.xamez.ffaction.api.FFactionAPI
import fr.xamez.ffaction.api.model.FactionRole
import fr.xamez.ffaction.command.base.ICommand
import fr.xamez.ffaction.localization.LanguageManager
import fr.xamez.ffaction.localization.LocalizationKey
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.entity.Player

class DisbandFactionCommand(
    private val factionApi: FFactionAPI,
    private val languageManager: LanguageManager
) : ICommand {

    override val name: String = "disband"

    override val command: LiteralArgumentBuilder<CommandSourceStack> =
        literal<CommandSourceStack>(name).executes { context ->
            val source = context.source
            val sender = source.sender

            if (sender !is Player) {
                languageManager.sendMessage(sender, LocalizationKey.COMMAND_PLAYER_ONLY)
                return@executes Command.SINGLE_SUCCESS
            }

            val fPlayer = factionApi.getPlayer(sender)
            if (fPlayer == null) {
                languageManager.sendMessage(sender, LocalizationKey.COMMAND_PLAYER_NOT_FOUND)
                return@executes Command.SINGLE_SUCCESS
            }

            val faction = factionApi.getPlayerFaction(fPlayer)
            if (faction == null) {
                languageManager.sendMessage(sender, LocalizationKey.COMMAND_NOT_IN_FACTION)
                return@executes Command.SINGLE_SUCCESS
            }

            if (fPlayer.role != FactionRole.LEADER) {
                languageManager.sendMessage(sender, LocalizationKey.COMMAND_LEADER_ONLY)
                return@executes Command.SINGLE_SUCCESS
            }

            val factionName = faction.name
            if (factionApi.disbandFaction(faction)) {
                languageManager.sendMessage(
                    sender,
                    LocalizationKey.COMMAND_DISBAND_SUCCESS,
                    "faction" to factionName
                )
            } else {
                languageManager.sendMessage(sender, LocalizationKey.COMMAND_DISBAND_ERROR)
            }

            Command.SINGLE_SUCCESS
        }

}