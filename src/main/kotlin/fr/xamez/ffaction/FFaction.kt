package fr.xamez.ffaction

import fr.xamez.ffaction.command.CommandRegistrar
import fr.xamez.ffaction.config.ConfigManager
import fr.xamez.ffaction.config.ReloadManager
import fr.xamez.ffaction.localization.LanguageManager
import org.bukkit.plugin.java.JavaPlugin


class FFaction : JavaPlugin() {

    override fun onEnable() {

        val reloadManager = ReloadManager(this)
        val configManager = ConfigManager(this)
        val languageManager = LanguageManager(this, configManager)

        reloadManager.register("config", configManager)
        reloadManager.register("language", languageManager)

        CommandRegistrar.register(this, reloadManager, configManager, languageManager)
    }

    override fun onDisable() {
    }

}
