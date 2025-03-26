package fr.xamez.ffaction.command.subcommand

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import fr.xamez.ffaction.command.base.ICommand
import fr.xamez.ffaction.config.ReloadManager
import fr.xamez.ffaction.localization.LanguageManager
import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

class ReloadFactionCommand(
    private val reloadManager: ReloadManager,
    private val languageManager: LanguageManager
) : ICommand {

    override val name: String = "reload"

    override val description: Component =
        Component.text("Reload the whole configuration as well as language", NamedTextColor.GRAY)

    override val command: LiteralArgumentBuilder<CommandSourceStack> =
        literal<CommandSourceStack>(name).executes { context ->
            val results = reloadManager.reloadAll()
            val success = results.values.all { it }

            context.source.sender.sendMessage(
                if (success)
                    languageManager.get("command.faction.reload.success")
                else
                    languageManager.get("command.faction.reload.error"),
            )

            Command.SINGLE_SUCCESS
        }
}
