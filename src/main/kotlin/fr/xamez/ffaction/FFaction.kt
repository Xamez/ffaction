package fr.xamez.ffaction

import fr.xamez.ffaction.command.CommandRegistrar
import fr.xamez.ffaction.config.ConfigManager
import fr.xamez.ffaction.localization.LanguageManager
import org.bukkit.plugin.java.JavaPlugin


class FFaction : JavaPlugin() {

    override fun onEnable() {

        val configManager = ConfigManager(this)
        val languageManager = LanguageManager(this, configManager)

        CommandRegistrar.register(this, languageManager)

    }

    override fun onDisable() {
    }

}
