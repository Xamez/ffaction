package fr.xamez.ffaction.storage.impl.sql

import com.zaxxer.hikari.HikariDataSource
import fr.xamez.ffaction.api.model.FPlayer
import fr.xamez.ffaction.api.model.FactionRole
import fr.xamez.ffaction.api.repository.FPlayerRepository
import fr.xamez.ffaction.storage.impl.AbstractRepository
import java.util.*
import java.util.logging.Logger

class SQLFPlayerRepository(
    logger: Logger,
    private val dataSource: HikariDataSource,
    useCache: Boolean = true
) : AbstractRepository<FPlayer, UUID>(logger, useCache), FPlayerRepository {

    init {
        initTables()
    }

    override fun fetchById(id: UUID): FPlayer? {
        return withConnection { executor ->
            executor.query(
                "SELECT * FROM players WHERE uuid = ?",
                { rs ->
                    if (rs.next()) {
                        extractPlayer(rs)
                    } else null
                },
                id.toString()
            )
        }
    }

    override fun fetchAll(): List<FPlayer> {
        return withConnection { executor ->
            executor.queryList(
                "SELECT * FROM players",
                { rs -> extractPlayer(rs) }
            )
        }
    }

    override fun persist(entity: FPlayer): Boolean {
        return withConnection { executor ->
            val exists = executor.query(
                "SELECT 1 FROM players WHERE uuid = ?",
                { rs -> rs.next() },
                entity.uuid.toString()
            )

            if (exists == true) {
                executor.update(
                    """
                    UPDATE players SET 
                        name = ?, 
                        faction_id = ?, 
                        role = ?, 
                        power = ?,
                        max_power = ?
                    WHERE uuid = ?
                    """,
                    entity.name,
                    entity.factionId,
                    entity.role.name,
                    entity.power,
                    entity.maxPower,
                    entity.uuid.toString()
                ) > 0
            } else {
                executor.update(
                    """
                    INSERT INTO players (uuid, name, faction_id, role, power, max_power) 
                    VALUES (?, ?, ?, ?, ?, ?)
                    """,
                    entity.uuid.toString(),
                    entity.name,
                    entity.factionId,
                    entity.role.name,
                    entity.power,
                    entity.maxPower
                ) > 0
            }
        }
    }

    override fun removeById(id: UUID): Boolean {
        return withConnection { executor ->
            executor.update("DELETE FROM players WHERE uuid = ?", id.toString()) > 0
        }
    }

    override fun getEntityId(entity: FPlayer): UUID = entity.uuid

    override fun executeQuery(queryType: String, params: Map<String, Any?>): Any? {
        return when (queryType) {
            "findByName" -> {
                val name = params["name"] as? String ?: return null
                withConnection { executor ->
                    executor.query(
                        "SELECT * FROM players WHERE name = ?",
                        { rs ->
                            if (rs.next()) extractPlayer(rs) else null
                        },
                        name
                    )
                }
            }
            "findByFaction" -> {
                val factionId = params["factionId"] as? String ?: return null
                withConnection { executor ->
                    executor.queryList(
                        "SELECT * FROM players WHERE faction_id = ?",
                        { rs -> extractPlayer(rs) },
                        factionId
                    )
                }
            }
            else -> null
        }
    }

    override fun findByName(name: String): FPlayer? {
        return query("findByName", mapOf("name" to name)) { it as? FPlayer }
    }

    override fun findByFaction(factionId: String): List<FPlayer> {
        return query("findByFaction", mapOf("factionId" to factionId)) { it as? List<FPlayer> } ?: emptyList()
    }

    private fun extractPlayer(rs: java.sql.ResultSet): FPlayer {
        val uuid = UUID.fromString(rs.getString("uuid"))
        val name = rs.getString("name")
        val factionId = rs.getString("faction_id")
        val roleStr = rs.getString("role")
        val role = try {
            FactionRole.valueOf(roleStr)
        } catch (e: Exception) {
            logger.warning("Invalid faction role: $roleStr for player $name")
            FactionRole.MEMBER
        }
        val power = rs.getDouble("power")
        val maxPower = rs.getDouble("max_power")

        return FPlayer(
            uuid = uuid,
            name = name,
            factionId = factionId,
            role = role,
            power = power,
            maxPower = maxPower
        )
    }

    private fun initTables() {
        withConnection { executor ->
            executor.execute(
                """
                CREATE TABLE IF NOT EXISTS players (
                    uuid VARCHAR(36) PRIMARY KEY,
                    name VARCHAR(16) NOT NULL,
                    faction_id VARCHAR(50),
                    role VARCHAR(20) NOT NULL,
                    power DOUBLE NOT NULL DEFAULT 10.0,
                    max_power DOUBLE NOT NULL DEFAULT 20.0,
                    FOREIGN KEY (faction_id) REFERENCES factions(id) ON DELETE SET NULL
                )
                """
            )
        }
    }

    private fun <T> withConnection(action: (SQLExecutor) -> T): T {
        dataSource.connection.use { connection ->
            val executor = SQLExecutor(logger, connection)
            return action(executor)
        }
    }
}