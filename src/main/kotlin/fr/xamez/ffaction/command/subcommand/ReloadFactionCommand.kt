package fr.xamez.ffaction.command.subcommand

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import fr.xamez.ffaction.command.base.ICommand
import fr.xamez.ffaction.config.ReloadManager
import fr.xamez.ffaction.localization.LanguageManager
import fr.xamez.ffaction.localization.LocalizationKey
import io.papermc.paper.command.brigadier.CommandSourceStack

class ReloadFactionCommand(
    private val reloadManager: ReloadManager,
    private val languageManager: LanguageManager
) : ICommand {

    override val name: String = "reload"
    override val aliases: List<String> = listOf("r")

    override val command: LiteralArgumentBuilder<CommandSourceStack> =
        literal<CommandSourceStack>(name).executes { context ->
            val results = reloadManager.reloadAll()
            val success = results.values.all { it }

            languageManager.sendMessage(
                context.source.sender,
                if (success)
                    LocalizationKey.COMMAND_RELOAD_SUCCESS
                else
                    LocalizationKey.COMMAND_RELOAD_ERROR,
            )

            Command.SINGLE_SUCCESS
        }

}
