package fr.xamez.ffaction.listener

import fr.xamez.ffaction.api.FFactionAPI
import fr.xamez.ffaction.config.ConfigManager
import fr.xamez.ffaction.localization.LanguageManager
import org.bukkit.plugin.Plugin

class ListenerManager(
    private val plugin: Plugin,
    private val factionApi: FFactionAPI,
    private val configManager: ConfigManager,
    private val languageManager: LanguageManager
) {

    init {
        initialize()
    }

    private fun initialize() {
        val events = listOf(
            PlayerListener(factionApi),
        )

        val pluginManager = plugin.server.pluginManager

        for (event in events) {
            pluginManager.registerEvents(event, plugin)
        }
    }

}