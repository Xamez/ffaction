package fr.xamez.ffaction.command.subcommand

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import fr.xamez.ffaction.api.FFactionAPI
import fr.xamez.ffaction.command.base.CommandTarget
import fr.xamez.ffaction.command.base.ICommand
import fr.xamez.ffaction.localization.LanguageManager
import fr.xamez.ffaction.localization.LocalizationKey
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.entity.Player

class ListFactionMembersCommand(
    private val factionApi: FFactionAPI,
    private val languageManager: LanguageManager
) : ICommand {

    override val name: String = "members"
    override val commandTarget: CommandTarget = CommandTarget.PLAYER
    override val aliases: List<String> = listOf("m")

    override val command: LiteralArgumentBuilder<CommandSourceStack> =
        literal<CommandSourceStack>(name).executes { context ->
            val source = context.source
            val player = source.sender as Player

            val fPlayer = factionApi.getPlayer(player)
            if (fPlayer == null) {
                languageManager.sendMessage(player, LocalizationKey.COMMAND_PLAYER_NOT_FOUND)
                return@executes Command.SINGLE_SUCCESS
            }

            val faction = factionApi.getPlayerFaction(fPlayer)
            if (faction == null) {
                languageManager.sendMessage(player, LocalizationKey.COMMAND_NOT_IN_FACTION)
                return@executes Command.SINGLE_SUCCESS
            }

            val members = factionApi.getFactionMembers(faction)
            languageManager.sendMessage(
                player,
                LocalizationKey.COMMAND_MEMBERS_HEADER,
                "faction" to faction.name
            )

            members.groupBy { it.role }
                .forEach { (role, players) ->
                    languageManager.sendMessage(
                        player,
                        LocalizationKey.COMMAND_MEMBERS_ROLE,
                        "role" to role.name
                    )
                    players.forEach { member ->
                        languageManager.sendMessage(
                            player,
                            LocalizationKey.COMMAND_MEMBERS_ENTRY,
                            "player" to member.name,
                            "online" to (member.isOnline().toString())
                        )
                    }
                }

            Command.SINGLE_SUCCESS
        }

}