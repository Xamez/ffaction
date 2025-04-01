package fr.xamez.ffaction.command

import fr.xamez.ffaction.api.FFactionAPI
import fr.xamez.ffaction.command.base.FactionCommand
import fr.xamez.ffaction.command.base.ICommand
import fr.xamez.ffaction.command.subcommand.CreateFactionCommand
import fr.xamez.ffaction.command.subcommand.DisbandFactionCommand
import fr.xamez.ffaction.command.subcommand.ReloadFactionCommand
import fr.xamez.ffaction.config.ConfigManager
import fr.xamez.ffaction.config.ReloadManager
import fr.xamez.ffaction.localization.LanguageManager
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.plugin.Plugin

class CommandRegistrar {

    companion object {

        fun register(
            plugin: Plugin,
            reloadManager: ReloadManager,
            configManager: ConfigManager,
            languageManager: LanguageManager,
            factionApi: FFactionAPI
        ) {
            val subCommands: List<ICommand> = listOf(
                ReloadFactionCommand(reloadManager, languageManager),
                CreateFactionCommand(factionApi, languageManager),
                DisbandFactionCommand(factionApi, languageManager)
            )

            val factionCommand = FactionCommand(configManager, languageManager, subCommands)

            plugin.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { commands ->
                commands.registrar().register(factionCommand.command.build())
            }
        }

    }
}