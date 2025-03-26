package fr.xamez.ffaction.localization

import fr.xamez.ffaction.config.ConfigManager
import fr.xamez.ffaction.config.Reloadable
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.text.SimpleDateFormat
import java.util.*

class LanguageManager(private val plugin: Plugin, private val config: ConfigManager) : Reloadable {

    private val fallbackLang = "en"
    private val langFolderName = "langs"
    private val langFolder = File(plugin.dataFolder, langFolderName)
    private lateinit var langFromConfig: String
    private lateinit var langConfig: YamlConfiguration

    private val undefinedKey = "<red><hover:show_text:'You should provide a translation for this key'>%s</hover></red>"

    private val miniMessage = MiniMessage.miniMessage()
    private val legacySerializer = LegacyComponentSerializer.legacySection()

    init {
        initializeLanguages()
    }

    private fun initializeLanguages() {
        if (!langFolder.exists()) langFolder.mkdirs()

        val langFilenames = LangFileExtractor.extractLangFilenames()

        langFromConfig = config.getString("language") ?: run {
            plugin.logger.warning("Language not found in config, falling back to English")
            fallbackLang
        }

        for (langFileName in langFilenames) {
            val targetFile = File(langFolder, langFileName)
            if (!targetFile.exists()) {
                plugin.saveResource("$langFolderName/$langFileName", false)
                plugin.logger.info("Created language file: $langFileName")
            } else {
                try {
                    YamlConfiguration.loadConfiguration(targetFile)
                    updateLanguageFile(langFileName, targetFile)
                } catch (e: Exception) {
                    createBackup(targetFile)
                    plugin.logger.severe("Invalid YAML in language file $langFileName, created backup and restoring default")
                    plugin.saveResource("$langFolderName/$langFileName", true)
                }
            }
        }

        val langFile = File(langFolder, "$langFromConfig.yml")
        try {
            langConfig = YamlConfiguration.loadConfiguration(langFile)
            plugin.logger.info("Loaded language: $langFromConfig")
        } catch (e: Exception) {
            createBackup(langFile)
            plugin.logger.severe("Failed to load language file $langFromConfig.yml - invalid YAML. Created backup and falling back to English")
            langFromConfig = fallbackLang
            val fallbackFile = File(langFolder, "$fallbackLang.yml")
            if (!fallbackFile.exists())
                plugin.saveResource("$langFolderName/$fallbackLang.yml", false)
            langConfig = YamlConfiguration.loadConfiguration(fallbackFile)
        }
    }

    private fun createBackup(file: File) {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd-HHmmss").format(Date())
            val backupFile = File(file.parentFile, "${file.nameWithoutExtension}-$timestamp.yml.bak")
            Files.copy(file.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            plugin.logger.info("Created backup of ${file.name} as ${backupFile.name}")
        } catch (e: Exception) {
            plugin.logger.severe("Failed to create backup of ${file.name}: ${e.message}")
        }
    }

    private fun updateLanguageFile(langFileName: String, existingFile: File) {
        try {
            val existingConfig = YamlConfiguration.loadConfiguration(existingFile)

            val defaultConfigStream = plugin.getResource("$langFolderName/$langFileName")
            if (defaultConfigStream != null) {
                val defaultConfig = YamlConfiguration.loadConfiguration(defaultConfigStream.reader())
                var updated = false

                for (key in defaultConfig.getKeys(true)) {
                    if (!existingConfig.contains(key)) {
                        existingConfig.set(key, defaultConfig.get(key))
                        updated = true
                    }
                }

                if (updated) {
                    existingConfig.save(existingFile)
                    plugin.logger.info("Updated language file with new keys: $langFileName")
                }
            }
        } catch (e: Exception) {
            plugin.logger.warning("Failed to update language file $langFileName: ${e.message}")
        }
    }

    private fun getString(key: String): String = langConfig.getString(key) ?: undefinedKey.format(key)

    private fun getString(key: String, vararg replacements: Pair<String, String>): String {
        var text = getString(key)
        replacements.forEach { (placeholder, value) ->
            text = text.replace("{$placeholder}", value)
        }
        return text
    }

    fun get(key: String): Component {
        val text = getString(key)
        return if (text.contains('&')) {
            legacySerializer.deserialize(text.replace("&", "§"))
        } else {
            miniMessage.deserialize(text)
        }
    }

    fun get(key: String, vararg replacements: Pair<String, String>): Component {
        val text = getString(key, *replacements)
        return if (text.contains('&')) {
            legacySerializer.deserialize(text.replace("&", "§"))
        } else {
            miniMessage.deserialize(text)
        }
    }

    override fun reload(): Boolean {
        try {
            initializeLanguages()
            return true
        } catch (e: Exception) {
            plugin.logger.severe("Failed to reload language files: ${e.message}")
            e.printStackTrace()
            return false
        }
    }
}