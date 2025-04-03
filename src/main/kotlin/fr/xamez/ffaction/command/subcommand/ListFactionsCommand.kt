package fr.xamez.ffaction.command.subcommand

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import fr.xamez.ffaction.api.FFactionAPI
import fr.xamez.ffaction.command.base.ICommand
import fr.xamez.ffaction.localization.LanguageManager
import fr.xamez.ffaction.localization.LocalizationKey
import io.papermc.paper.command.brigadier.CommandSourceStack

class ListFactionsCommand(
    private val factionApi: FFactionAPI,
    private val languageManager: LanguageManager
) : ICommand {

    override val name: String = "list"
    override val aliases: List<String> = listOf("l")

    override val command: LiteralArgumentBuilder<CommandSourceStack> =
        literal<CommandSourceStack>(name).executes { context ->
            val source = context.source
            val sender = source.sender

            val factions = factionApi.getAllFactions()
            if (factions.isEmpty()) {
                languageManager.sendMessage(sender, LocalizationKey.COMMAND_NO_FACTION)
                return@executes Command.SINGLE_SUCCESS
            }

            languageManager.sendMessage(
                sender,
                LocalizationKey.COMMAND_FACTIONS_LIST_HEADER,
                "count" to factions.size.toString()
            )

            factions.forEach { faction ->

                val factionMembers = factionApi.getFactionMembers(faction)

                languageManager.sendMessage(
                    sender,
                    LocalizationKey.COMMAND_FACTIONS_LIST_ENTRY,
                    "name" to faction.name,
                    "members" to factionMembers.size.toString(),
                    "online" to factionMembers.count { it.isOnline() }.toString()
                )
            }

            Command.SINGLE_SUCCESS
        }
}