package fr.xamez.ffaction.command.base

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

enum class CommandTarget {
    ALL, PLAYER, CONSOLE
}

interface ICommand {

    val name: String
    val description: String
        get() = "command.$name.description"
    val aliases: List<String>
        get() = emptyList()
    val permission: String
        get() = "faction.command.$name"
    val command: LiteralArgumentBuilder<CommandSourceStack>
    val subCommands: List<ICommand>
        get() = emptyList()
    val commandTarget: CommandTarget
        get() = CommandTarget.ALL

    fun getUsage(parentCommand: String = ""): String {
        return "/$parentCommand $name" + getArguments(command)
    }

    private fun getArguments(builder: LiteralArgumentBuilder<CommandSourceStack>): String {
        val args = mutableListOf<String>()

        builder.arguments.forEach { child -> args.add(child.usageText) }

        return if (args.isNotEmpty()) " " + args.joinToString(" ") else ""
    }

    fun checkSender(sender: CommandSender): Boolean {
        return when (commandTarget) {
            CommandTarget.ALL -> true
            CommandTarget.PLAYER -> sender is Player
            CommandTarget.CONSOLE -> sender !is Player
        }
    }

}