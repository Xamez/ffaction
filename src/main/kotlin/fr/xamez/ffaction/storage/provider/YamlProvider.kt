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
    private val filename: String = "data.yml"
) : StorageProvider {

    private lateinit var file: File
    private lateinit var config: YamlConfiguration

    private lateinit var factionRepository: FactionRepository
    private lateinit var fPlayerRepository: FPlayerRepository

    override fun initialize(): Boolean {
        return try {
            file = File(plugin.dataFolder, "$storageDirectoryName/$filename.yml")

            if (!file.exists()) {
                file.parentFile.mkdirs()
                file.createNewFile()
            }

            config = YamlConfiguration.loadConfiguration(file)
            factionRepository = YamlFactionRepository(plugin.logger, config, file);
            fPlayerRepository = YamlFPlayerRepository(plugin.logger, config, file);
            plugin.logger.info("Successfully initialized YAML storage")
            true
        } catch (e: Exception) {
            plugin.logger.severe("Failed to initialize YAML storage: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    override fun shutdown() {
        try {
            config.save(file)
            plugin.logger.info("YAML data saved")
        } catch (e: Exception) {
            plugin.logger.severe("Error saving YAML data: ${e.message}")
        }
    }

    override fun isConnected(): Boolean {
        return ::config.isInitialized && ::file.isInitialized && file.exists()
    }

    override fun getFPlayerRepository(): FPlayerRepository {
        return fPlayerRepository
    }

    override fun getFactionRepository(): FactionRepository {
        return factionRepository
    }
}