package fr.xamez.ffaction

import fr.xamez.ffaction.api.FFactionAPI
import fr.xamez.ffaction.api.FFactionAPIImpl
import fr.xamez.ffaction.command.CommandRegistrar
import fr.xamez.ffaction.config.ConfigManager
import fr.xamez.ffaction.config.ReloadManager
import fr.xamez.ffaction.localization.LanguageManager
import fr.xamez.ffaction.storage.StorageManager
import org.bukkit.plugin.java.JavaPlugin

class FFaction : JavaPlugin() {

    private lateinit var storageManager: StorageManager
    private lateinit var factionAPI: FFactionAPI

    override fun onEnable() {
        val reloadManager = ReloadManager(this)
        val configManager = ConfigManager(this)
        val languageManager = LanguageManager(this, configManager)
        storageManager = StorageManager(this, configManager)

        factionAPI = FFactionAPIImpl(storageManager, this)

        reloadManager.register("config", configManager)
        reloadManager.register("language", languageManager)
        reloadManager.register("storage", storageManager)

        CommandRegistrar.register(this, reloadManager, configManager, languageManager)
    }

    override fun onDisable() {
        if (::storageManager.isInitialized) {
            storageManager.shutdown()
        }
    }

    fun getAPI(): FFactionAPI {
        return factionAPI
    }
}