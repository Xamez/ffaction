package fr.xamez.ffaction.localization

import fr.xamez.ffaction.config.ConfigManager
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.io.File
import java.io.InputStreamReader

class LanguageManager(private val plugin: Plugin, config: ConfigManager) {

    private val defaultLanguage = config.getString("language")
    private val langFolder = File(plugin.dataFolder, "langs")
    private val langFile = File(langFolder, "$defaultLanguage.yml")
    private lateinit var langConfig: YamlConfiguration
    private lateinit var defaultLangConfig: YamlConfiguration

    init {
        loadLanguage()
    }

    private fun loadLanguage() {
        val defaultStream =
            plugin.getResource("langs/en.yml") ?: throw IllegalStateException("Missing default en.yml in JAR!")
        defaultLangConfig = YamlConfiguration.loadConfiguration(InputStreamReader(defaultStream))

        if (!langFolder.exists()) langFolder.mkdirs()


        if (!langFile.exists()) {
            val resourcePath = "langs/$defaultLanguage.yml"
            if (plugin.getResource(resourcePath) != null) {
                plugin.saveResource(resourcePath, false)
            } else {
                plugin.logger.warning("Language file '$defaultLanguage.yml' not found! Using English fallback.")
            }
        }

        langConfig = if (langFile.exists()) YamlConfiguration.loadConfiguration(langFile) else defaultLangConfig
    }

    fun reload() = loadLanguage()

    fun get(key: String): String = langConfig.getString(key) ?: defaultLangConfig.getString(key) ?: key

    fun get(key: String, vararg replacements: Pair<String, String>): String {
        var text = get(key)
        replacements.forEach { (placeholder, value) ->
            text = text.replace("{$placeholder}", value)
        }
        return text
    }

}
