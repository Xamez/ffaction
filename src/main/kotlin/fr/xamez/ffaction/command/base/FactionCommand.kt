package fr.xamez.ffaction.command.base

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import fr.xamez.ffaction.config.ConfigManager
import fr.xamez.ffaction.localization.LanguageManager
import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

class FactionCommand(
    private val configManager: ConfigManager,
    private val languageManager: LanguageManager,
    private val commands: List<ICommand>
) : ICommand {

    override val name: String = "faction"

    override val description: Component = Component.text("Base faction command", NamedTextColor.GOLD)

    override val command: LiteralArgumentBuilder<CommandSourceStack> = literal<CommandSourceStack>(name).apply {
        commands.forEach {
            then(it.command.requires { source -> source.sender.hasPermission(it.permission) })
        }
    }.requires { source -> source.sender.hasPermission(permission) }.executes { context ->
        val source = context.source
        source.sender.sendMessage(Component.text("Faction Commands:", NamedTextColor.GOLD))

        subCommands.filter { source.sender.hasPermission(it.permission) }.forEach { subCommand ->
            source.sender.sendMessage(
                Component.text("${subCommand.getUsage("faction")} ", NamedTextColor.YELLOW)
                    .append(Component.text("- ", NamedTextColor.GRAY)).append(subCommand.description)
            )
        }
        Command.SINGLE_SUCCESS
    }

    override val permission: String = "faction.command"

    override val subCommands: List<ICommand> = commands

}
