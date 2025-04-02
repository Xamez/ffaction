package fr.xamez.ffaction.storage.provider

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import fr.xamez.ffaction.api.repository.FPlayerRepository
import fr.xamez.ffaction.api.repository.FactionRepository
import fr.xamez.ffaction.storage.StorageProvider
import fr.xamez.ffaction.storage.impl.sql.SQLFPlayerRepository
import fr.xamez.ffaction.storage.impl.sql.SQLFactionRepository
import org.bukkit.plugin.Plugin
import java.io.File

class SQLiteProvider(
    private val plugin: Plugin,
) : StorageProvider {

    private lateinit var factionRepository: FactionRepository
    private lateinit var fPlayerRepository: FPlayerRepository

    private lateinit var dataSource: HikariDataSource

    override fun initialize(): Boolean {
        return try {
            Class.forName("org.sqlite.JDBC")
            val databaseFile = File(plugin.dataFolder, "$storageDirectoryName/data.db")
            if (!databaseFile.parentFile.exists()) {
                databaseFile.parentFile.mkdirs()
            }

            val config = HikariConfig()
            config.jdbcUrl = "jdbc:sqlite:${databaseFile.absolutePath}"
            config.maximumPoolSize = 1
            config.poolName = "FFactionSQLitePool"

            config.addDataSourceProperty("cachePrepStmts", "true")
            config.addDataSourceProperty("prepStmtCacheSize", "250")
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")

            dataSource = HikariDataSource(config)

            factionRepository = SQLFactionRepository(plugin.logger, dataSource)
            fPlayerRepository = SQLFPlayerRepository(plugin.logger, dataSource)

            plugin.logger.info("Successfully connected to SQLite database")
            true
        } catch (e: Exception) {
            plugin.logger.severe("Failed to connect to SQLite database: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    override fun shutdown() {
        try {
            dataSource.close()
            plugin.logger.info("SQLite connection closed")
        } catch (e: Exception) {
            plugin.logger.severe("Error closing SQLite connection: ${e.message}")
        }
    }

    override fun isConnected(): Boolean {
        return !dataSource.isClosed
    }

    override fun getFPlayerRepository(): FPlayerRepository {
        return fPlayerRepository
    }

    override fun getFactionRepository(): FactionRepository {
        return factionRepository
    }

}