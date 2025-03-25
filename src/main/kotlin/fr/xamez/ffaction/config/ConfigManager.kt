package fr.xamez.ffaction.config

import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.io.File
import java.io.InputStreamReader

class ConfigManager(private val plugin: Plugin) {

    private val configFile = File(plugin.dataFolder, "config.yml")
    private lateinit var config: YamlConfiguration
    private lateinit var defaultConfig: YamlConfiguration

    init {
        loadConfig()
    }

    private fun loadConfig() {
        val defaultStream =
            plugin.getResource("config.yml") ?: throw IllegalStateException("Missing default config.yml in JAR!")
        defaultConfig = YamlConfiguration.loadConfiguration(InputStreamReader(defaultStream))

        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false)
        }

        config = YamlConfiguration.loadConfiguration(configFile)
    }

    fun reload() = loadConfig()

    fun getString(path: String): String? = config.getString(path) ?: defaultConfig.getString(path)
    fun getInt(path: String): Int = config.getInt(path, defaultConfig.getInt(path))
    fun getBoolean(path: String): Boolean = config.getBoolean(path, defaultConfig.getBoolean(path))
    fun getStringList(path: String): List<String> =
        config.getStringList(path).ifEmpty { defaultConfig.getStringList(path) }
}
