package fr.xamez.ffaction.config

import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.io.File
import java.io.InputStreamReader

class ConfigManager(private val plugin: Plugin) : Reloadable {

    private val configurationFilename = "config.yml"
    private val configFile = File(plugin.dataFolder, configurationFilename)
    private lateinit var config: YamlConfiguration
    private lateinit var defaultConfig: YamlConfiguration

    init {
        loadConfig()
    }

    private fun loadConfig() {
        val defaultStream =
            plugin.getResource(configurationFilename) ?: throw IllegalStateException("Missing default $configurationFilename in JAR!")
        defaultConfig = YamlConfiguration.loadConfiguration(InputStreamReader(defaultStream))

        if (!configFile.exists()) {
            plugin.saveResource(configurationFilename, false)
            plugin.logger.info("Created config file: $configurationFilename")
        } else {
            updateConfigFile()
        }

        config = YamlConfiguration.loadConfiguration(configFile)
    }

    private fun updateConfigFile() {
        try {
            val existingConfig = YamlConfiguration.loadConfiguration(configFile)
            var updated = false

            for (key in defaultConfig.getKeys(true)) {
                if (!existingConfig.contains(key)) {
                    existingConfig.set(key, defaultConfig.get(key))
                    updated = true
                }
            }

            if (updated) {
                existingConfig.save(configFile)
                plugin.logger.info("Updated $configurationFilename with new keys")
            }
        } catch (e: Exception) {
            plugin.logger.warning("Failed to update $configurationFilename: ${e.message}")
        }
    }

    override fun reload(): Boolean {
        try {
            loadConfig()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun getString(path: String): String? = config.getString(path) ?: defaultConfig.getString(path)
    fun getInt(path: String): Int = config.getInt(path, defaultConfig.getInt(path))
    fun getBoolean(path: String): Boolean = config.getBoolean(path, defaultConfig.getBoolean(path))
    fun getStringList(path: String): List<String> =
        config.getStringList(path).ifEmpty { defaultConfig.getStringList(path) }
}