package fr.xamez.ffaction.storage.impl.sql

import com.zaxxer.hikari.HikariDataSource
import fr.xamez.ffaction.api.model.FPlayer
import fr.xamez.ffaction.api.model.FactionRole
import fr.xamez.ffaction.api.repository.FPlayerRepository
import java.sql.ResultSet
import java.util.*
import java.util.logging.Logger

class SQLFPlayerRepository(
    logger: Logger,
    dataSource: HikariDataSource
) : AbstractSQLRepository(logger, dataSource), FPlayerRepository {

    init {
        initTables()
    }

    override fun getPlayer(uuid: UUID): FPlayer? {
        return withConnection { executor ->
            executor.query(
                "SELECT * FROM players WHERE uuid = ?",
                { extractPlayer(it) },
                uuid.toString()
            )
        }
    }

    override fun getPlayerByName(name: String): FPlayer? {
        return withConnection { executor ->
            executor.query(
                "SELECT * FROM players WHERE LOWER(name) = LOWER(?)",
                { extractPlayer(it) },
                name
            )
        }
    }

    override fun getAllPlayers(): List<FPlayer> {
        return withConnection { executor ->
            executor.queryList(
                "SELECT * FROM players",
                { extractPlayer(it) }
            )
        }
    }

    override fun savePlayer(player: FPlayer): Boolean {
        return withConnection { executor ->
            val exists = getPlayer(player.uuid) != null

            if (exists) {
                executor.update(
                    """
                    UPDATE players
                    SET name = ?, faction_id = ?, role = ?, power = ?, max_power = ?
                    WHERE uuid = ?
                    """,
                    player.name,
                    player.factionId,
                    player.role.name,
                    player.power,
                    player.maxPower,
                    player.uuid.toString()
                ) > 0
            } else {
                executor.update(
                    """
                    INSERT INTO players (uuid, name, faction_id, role, power, max_power)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """,
                    player.uuid.toString(),
                    player.name,
                    player.factionId,
                    player.role.name,
                    player.power,
                    player.maxPower
                ) > 0
            }
        }
    }

    override fun deletePlayer(uuid: UUID): Boolean {
        return withConnection { executor ->
            executor.update(
                "DELETE FROM players WHERE uuid = ?",
                uuid.toString()
            ) > 0
        }
    }

    override fun getPlayersInFaction(factionId: String): List<FPlayer> {
        return withConnection { executor ->
            executor.queryList(
                "SELECT * FROM players WHERE faction_id = ?",
                { extractPlayer(it) },
                factionId
            )
        }
    }

    private fun extractPlayer(rs: ResultSet): FPlayer {
        return FPlayer(
            uuid = UUID.fromString(rs.getString("uuid")),
            name = rs.getString("name"),
            factionId = rs.getString("faction_id"),
            role = FactionRole.valueOf(rs.getString("role")),
            power = rs.getDouble("power"),
            maxPower = rs.getDouble("max_power")
        )
    }

}