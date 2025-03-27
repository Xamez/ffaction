package fr.xamez.ffaction.storage.impl.sql

import com.zaxxer.hikari.HikariDataSource
import java.sql.SQLException
import java.util.logging.Logger

abstract class AbstractSQLRepository(
    protected val logger: Logger,
    private val dataSource: HikariDataSource
) {

    protected fun <T> withConnection(action: (SQLExecutor) -> T): T {
        dataSource.connection.use { connection ->
            val executor = SQLExecutor(logger, connection)
            return action(executor)
        }
    }

    protected fun initTables() {
        try {
            withConnection { executor ->
                executor.execute(
                    """
                    CREATE TABLE IF NOT EXISTS factions (
                        id VARCHAR(50) PRIMARY KEY,
                        name VARCHAR(100) NOT NULL,
                        description TEXT,
                        leader_id VARCHAR(36),
                        home TEXT,
                        open BOOLEAN NOT NULL DEFAULT false,
                        power DOUBLE NOT NULL DEFAULT 0.0,
                        max_power DOUBLE NOT NULL DEFAULT 10.0
                    )
                    """
                )

                executor.execute(
                    """
                        CREATE TABLE IF NOT EXISTS players (
                            uuid VARCHAR(36) PRIMARY KEY,
                            name VARCHAR(16) NOT NULL,
                            faction_id VARCHAR(50),
                            role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
                            power DOUBLE NOT NULL DEFAULT 0.0,
                            max_power DOUBLE NOT NULL DEFAULT 10.0,
                            FOREIGN KEY (faction_id) REFERENCES factions(id) ON DELETE SET NULL
                        )
                    """
                )

                executor.execute(
                    """
                        CREATE TABLE IF NOT EXISTS claims (
                            world VARCHAR(50) NOT NULL,
                            chunk_x INT NOT NULL,
                            chunk_z INT NOT NULL,
                            faction_id VARCHAR(50) NOT NULL,
                            PRIMARY KEY (world, chunk_x, chunk_z),
                            FOREIGN KEY (faction_id) REFERENCES factions(id) ON DELETE CASCADE
                        )
                    """
                )

                executor.execute(
                    """
                        CREATE TABLE IF NOT EXISTS relations (
                            faction_id VARCHAR(50) NOT NULL,
                            other_faction_id VARCHAR(50) NOT NULL,
                            relation VARCHAR(20) NOT NULL,
                            PRIMARY KEY (faction_id, other_faction_id),
                            FOREIGN KEY (faction_id) REFERENCES factions(id) ON DELETE CASCADE,
                            FOREIGN KEY (other_faction_id) REFERENCES factions(id) ON DELETE CASCADE
                        )
                    """
                )
            }

        } catch (e: SQLException) {
            logger.severe("Failed to initialize tables")
            throw RuntimeException(e)
        }
    }

}