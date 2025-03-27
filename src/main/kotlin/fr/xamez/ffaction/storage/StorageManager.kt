package fr.xamez.ffaction.storage

import fr.xamez.ffaction.config.ConfigManager
import fr.xamez.ffaction.config.Reloadable
import fr.xamez.ffaction.storage.provider.MySQLProvider
import fr.xamez.ffaction.storage.provider.SQLiteProvider
import fr.xamez.ffaction.storage.provider.YamlProvider
import org.bukkit.plugin.Plugin

class StorageManager(
    private val plugin: Plugin,
    private val configManager: ConfigManager
) : Reloadable {

    private var provider: StorageProvider? = null
    private var storageType: StorageType = StorageType.YAML

    enum class StorageType {
        MYSQL, SQLITE, YAML
    }

    init {
        initialize()
    }

    private fun initialize() {
        val storageTypeString = configManager.getString("storage.type", "yaml").uppercase()
        try {
            storageType = StorageType.valueOf(storageTypeString)
        } catch (e: IllegalArgumentException) {
            plugin.logger.warning("Invalid storage type: $storageTypeString, defaulting to YAML")
            storageType = StorageType.YAML
        }

        provider = when (storageType) {
            StorageType.MYSQL -> {
                val credentials = DatabaseCredentials(
                    host = configManager.getString("storage.mysql.host", "localhost"),
                    port = configManager.getInt("storage.mysql.port", 3306),
                    database = configManager.getString("storage.mysql.database", "ffaction"),
                    username = configManager.getString("storage.mysql.username", "root"),
                    password = configManager.getString("storage.mysql.password", ""),
                    tablePrefix = configManager.getString("storage.mysql.tablePrefix", "ffaction_")
                )
                MySQLProvider(plugin, credentials)
            }

            StorageType.SQLITE -> {
                val credentials = DatabaseCredentials(
                    database = configManager.getString("storage.sqlite.filename", "ffaction/data"),
                    tablePrefix = configManager.getString("storage.sqlite.tablePrefix", "ffaction_")
                )
                SQLiteProvider(plugin, credentials)
            }

            StorageType.YAML -> {
                YamlProvider(plugin, configManager.getString("storage.yaml.filename", "ffaction/data"))
            }
        }

        if (provider?.initialize() != true) {
            plugin.logger.severe("Failed to initialize storage provider")
        } else {
            plugin.logger.info("Storage provider initialized: $storageType")
        }
    }

    fun getProvider(): StorageProvider? {
        return provider
    }

    override fun reload(): Boolean {
        try {
            provider?.shutdown()
            initialize()
            return true
        } catch (e: Exception) {
            plugin.logger.severe("Failed to reload storage manager: ${e.message}")
            e.printStackTrace()
            return false
        }
    }

    fun shutdown() {
        provider?.shutdown()
    }
}