package fr.xamez.ffaction.command.subcommand

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import fr.xamez.ffaction.api.FFactionAPI
import fr.xamez.ffaction.command.base.CommandTarget
import fr.xamez.ffaction.command.base.ICommand
import fr.xamez.ffaction.localization.LanguageManager
import fr.xamez.ffaction.localization.LocalizationKey
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.entity.Player

class CreateFactionCommand(
    private val factionApi: FFactionAPI,
    private val languageManager: LanguageManager
) : ICommand {

    override val name: String = "create"
    override val commandTarget: CommandTarget = CommandTarget.PLAYER

    override val command: LiteralArgumentBuilder<CommandSourceStack> = literal<CommandSourceStack>(name).then(
        argument<CommandSourceStack, String>("name", StringArgumentType.string()).executes { context ->
            val source = context.source
            val sender = source.sender

            val player = sender as Player

            val factionName = StringArgumentType.getString(context, "name")
            val result = factionApi.createFaction(factionName, player)

            if (result != null) {
                languageManager.sendMessage(
                    sender,
                    LocalizationKey.COMMAND_CREATE_SUCCESS,
                    "faction" to factionName
                )
            } else {
                languageManager.sendMessage(sender, LocalizationKey.COMMAND_CREATE_ERROR)
            }

            Command.SINGLE_SUCCESS
        })

}