package fr.xamez.ffaction.storage.provider

import fr.xamez.ffaction.api.repository.FPlayerRepository
import fr.xamez.ffaction.api.repository.FactionRepository
import fr.xamez.ffaction.storage.StorageProvider
import fr.xamez.ffaction.storage.impl.yaml.YamlFPlayerRepository
import fr.xamez.ffaction.storage.impl.yaml.YamlFactionRepository
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.io.File

class YamlProvider(
    private val plugin: Plugin,
) : StorageProvider {

    private lateinit var factionFile: File
    private lateinit var playerFile: File
    private lateinit var factionConfig: YamlConfiguration
    private lateinit var playerConfig: YamlConfiguration

    private lateinit var factionRepository: FactionRepository
    private lateinit var fPlayerRepository: FPlayerRepository

    override fun initialize(): Boolean {
        return try {
            val parentDir = File(plugin.dataFolder, storageDirectoryName)
            parentDir.mkdirs()

            factionFile = File(parentDir, "faction_data.yml")
            if (!factionFile.exists()) {
                factionFile.createNewFile()
            }

            playerFile = File(parentDir, "player_data.yml")
            if (!playerFile.exists()) {
                playerFile.createNewFile()
            }

            factionConfig = YamlConfiguration.loadConfiguration(factionFile)
            playerConfig = YamlConfiguration.loadConfiguration(playerFile)

            factionRepository = YamlFactionRepository(plugin.logger, factionConfig, factionFile)
            fPlayerRepository = YamlFPlayerRepository(plugin.logger, playerConfig, playerFile)

            plugin.logger.info("Successfully initialized YAML storage with separate files")
            true
        } catch (e: Exception) {
            plugin.logger.severe("Failed to initialize YAML storage: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    override fun shutdown() {
        try {
            if (::factionConfig.isInitialized && ::factionFile.isInitialized) {
                factionConfig.save(factionFile)
            }
            if (::playerConfig.isInitialized && ::playerFile.isInitialized) {
                playerConfig.save(playerFile)
            }
            plugin.logger.info("YAML data saved")
        } catch (e: Exception) {
            plugin.logger.severe("Error saving YAML data: ${e.message}")
        }
    }

    override fun isConnected(): Boolean {
        return ::factionConfig.isInitialized && ::playerConfig.isInitialized &&
                ::factionFile.isInitialized && ::playerFile.isInitialized &&
                factionFile.exists() && playerFile.exists()
    }

    override fun getFPlayerRepository(): FPlayerRepository {
        return fPlayerRepository
    }

    override fun getFactionRepository(): FactionRepository {
        return factionRepository
    }

}