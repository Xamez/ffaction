package fr.xamez.ffaction.command.subcommand

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import fr.xamez.ffaction.command.base.ICommand
import fr.xamez.ffaction.localization.LanguageManager
import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

class CreateFactionCommand(private val languageManager: LanguageManager) : ICommand {

    override val name: String = "create"

    override val description: Component = Component.text("Create a new faction.", NamedTextColor.GRAY)

    override val command: LiteralArgumentBuilder<CommandSourceStack> = literal<CommandSourceStack>(name).then(
            argument<CommandSourceStack, String>("name", StringArgumentType.string()).executes { context ->
                    val source = context.source
                    val factionName = StringArgumentType.getString(context, "name")
                    source.sender.sendMessage(
                        Component.text(languageManager.get("command.faction.create.success", "faction" to factionName), NamedTextColor.GREEN)
                    )
                    Command.SINGLE_SUCCESS
                })
}
