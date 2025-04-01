package fr.xamez.ffaction.command.base

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import fr.xamez.ffaction.config.ConfigManager
import fr.xamez.ffaction.localization.LanguageManager
import fr.xamez.ffaction.localization.LocalizationKey
import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

class FactionCommand(
    private val configManager: ConfigManager,
    private val languageManager: LanguageManager,
    private val commands: List<ICommand>
) : ICommand {

    override val name: String = "faction"

    override val command: LiteralArgumentBuilder<CommandSourceStack> = literal<CommandSourceStack>(name).apply {
        commands.forEach {
            then(it.command.requires { source -> source.sender.hasPermission(it.permission) })
        }
    }.requires { source -> source.sender.hasPermission(permission) }.executes { context ->
        val source = context.source
        languageManager.sendMessage(source.sender, LocalizationKey.COMMAND_HELP_USAGE_HEADER)

        subCommands.filter { source.sender.hasPermission(it.permission) }.forEach { subCommand ->
            // TODO: Make this message customizable using language files
            source.sender.sendMessage(
                Component.text("${subCommand.getUsage("faction")} ", NamedTextColor.YELLOW)
                    .append(Component.text("- ", NamedTextColor.GRAY)).append(languageManager.get(subCommand.description))
            )
        }
        Command.SINGLE_SUCCESS
    }

    override val permission: String = "faction.command"

    override val subCommands: List<ICommand> = commands

}
