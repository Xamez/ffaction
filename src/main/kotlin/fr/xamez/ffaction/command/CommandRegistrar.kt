package fr.xamez.ffaction.command

import fr.xamez.ffaction.command.base.FactionCommand
import fr.xamez.ffaction.command.base.ICommand
import fr.xamez.ffaction.command.subcommand.CreateFactionCommand
import fr.xamez.ffaction.localization.LanguageManager
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.plugin.Plugin

class CommandRegistrar {

    companion object {

        fun register(plugin: Plugin, languageManager: LanguageManager) {

            val subCommands: List<ICommand> = listOf(
                CreateFactionCommand(languageManager)
            )

            val factionCommand = FactionCommand(languageManager, subCommands)

            plugin.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { commands ->
                commands.registrar().register(factionCommand.command.build())
            }
        }

    }
}
