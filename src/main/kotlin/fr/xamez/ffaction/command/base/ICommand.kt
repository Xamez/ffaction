package fr.xamez.ffaction.command.base

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.text.Component

interface ICommand {
    val name: String
    val description: Component
    val permission: String
        get() = "faction.command.$name"
    val command: LiteralArgumentBuilder<CommandSourceStack>
    val subCommands: List<ICommand>
        get() = emptyList()

    fun getUsage(parentCommand: String = ""): String {
        return "/$parentCommand $name" + getArguments(command)
    }

    private fun getArguments(builder: LiteralArgumentBuilder<CommandSourceStack>): String {
        val args = mutableListOf<String>()

        builder.arguments.forEach { child -> args.add(child.usageText) }

        return if (args.isNotEmpty()) " " + args.joinToString(" ") else ""
    }

}
