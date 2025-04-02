package fr.xamez.ffaction.storage.provider

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import fr.xamez.ffaction.api.repository.FPlayerRepository
import fr.xamez.ffaction.api.repository.FactionRepository
import fr.xamez.ffaction.storage.DatabaseCredentials
import fr.xamez.ffaction.storage.StorageProvider
import fr.xamez.ffaction.storage.impl.sql.SQLFPlayerRepository
import fr.xamez.ffaction.storage.impl.sql.SQLFactionRepository
import org.bukkit.plugin.Plugin

class MySQLProvider(
    private val plugin: Plugin,
    private val credentials: DatabaseCredentials
) : StorageProvider {

    private lateinit var factionRepository: FactionRepository
    private lateinit var fPlayerRepository: FPlayerRepository

    private lateinit var dataSource: HikariDataSource

    override fun initialize(): Boolean {
        return try {
            val config = HikariConfig()
            config.jdbcUrl =
                "jdbc:mysql://${credentials.host}:${credentials.port}/${credentials.database}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
            config.username = credentials.username
            config.password = credentials.password
            config.maximumPoolSize = 10
            config.poolName = "FFactionPool"

            config.addDataSourceProperty("cachePrepStmts", "true")
            config.addDataSourceProperty("prepStmtCacheSize", "250")
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")

            dataSource = HikariDataSource(config)

            factionRepository = SQLFactionRepository(plugin.logger, dataSource)
            fPlayerRepository = SQLFPlayerRepository(plugin.logger, dataSource)

            plugin.logger.info("Successfully connected to MySQL database")
            true
        } catch (e: Exception) {
            plugin.logger.severe("Failed to connect to MySQL database: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    override fun shutdown() {
        try {
            dataSource.close()
            plugin.logger.info("MySQL connection closed")
        } catch (e: Exception) {
            plugin.logger.severe("Error closing MySQL connection: ${e.message}")
        }
    }

    override fun isConnected(): Boolean {
        return !dataSource.isClosed
    }

    override fun getFPlayerRepository(): FPlayerRepository {
        return fPlayerRepository;
    }

    override fun getFactionRepository(): FactionRepository {
        return factionRepository;
    }

}