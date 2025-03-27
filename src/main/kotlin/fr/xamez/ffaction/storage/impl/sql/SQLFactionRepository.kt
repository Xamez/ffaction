package fr.xamez.ffaction.storage.impl.sql

import com.zaxxer.hikari.HikariDataSource
import fr.xamez.ffaction.api.model.FLocation
import fr.xamez.ffaction.api.model.Faction
import fr.xamez.ffaction.api.model.FactionRelation
import fr.xamez.ffaction.api.repository.FactionRepository
import fr.xamez.ffaction.util.SerializationUtil
import org.bukkit.Location
import java.sql.ResultSet
import java.util.*
import java.util.logging.Logger

class SQLFactionRepository(
    logger: Logger,
    dataSource: HikariDataSource
) : AbstractSQLRepository(logger, dataSource), FactionRepository {

    init {
        initTables()
    }

    override fun getFaction(id: String): Faction? {
        return withConnection { executor ->
            executor.query(
                "SELECT * FROM factions WHERE id = ?",
                { extractFaction(it, executor) },
                id
            )
        }
    }

    override fun getFactionByName(name: String): Faction? {
        return withConnection { executor ->
            executor.query(
                "SELECT * FROM factions WHERE LOWER(name) = LOWER(?)",
                { extractFaction(it, executor) },
                name
            )
        }
    }

    override fun getAllFactions(): List<Faction> {
        return withConnection { executor ->
            executor.queryList(
                "SELECT * FROM factions",
                { extractFaction(it, executor) }
            )
        }
    }

    override fun saveFaction(faction: Faction): Boolean {
        return withConnection { executor ->
            val exists = getFaction(faction.id) != null

            if (exists) {
                val updated = executor.update(
                    """
                    UPDATE factions
                    SET name = ?, description = ?, leader_id = ?, home = ?, open = ?, power = ?, max_power = ?
                    WHERE id = ?
                    """,
                    faction.name,
                    faction.description,
                    faction.leaderId.toString(),
                    faction.home?.let { SerializationUtil.serializeLocation(it) },
                    faction.isOpen,
                    faction.power,
                    faction.maxPower,
                    faction.id
                ) > 0

                if (updated) {
                    updateClaims(faction, executor)
                    updateRelations(faction, executor)
                }

                updated
            } else {
                val created = executor.update(
                    """
                    INSERT INTO factions (id, name, description, leader_id, home, open, power, max_power)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    faction.id,
                    faction.name,
                    faction.description,
                    faction.leaderId.toString(),
                    faction.home?.let { SerializationUtil.serializeLocation(it) },
                    faction.isOpen,
                    faction.power,
                    faction.maxPower
                ) > 0

                if (created && faction.claims.isNotEmpty()) {
                    saveClaims(faction, executor)
                }

                if (created && faction.relations.isNotEmpty()) {
                    saveRelations(faction, executor)
                }

                created
            }
        }
    }

    override fun deleteFaction(id: String): Boolean {
        return withConnection { executor ->
            executor.update("DELETE FROM factions WHERE id = ?", id) > 0
        }
    }

    override fun getClaimedChunks(factionId: String): Set<FLocation> {
        return withConnection { executor ->
            getClaims(factionId, executor)
        }
    }

    override fun getFactionAt(location: FLocation): Faction? {
        return withConnection { executor ->
            executor.query(
                """
                SELECT f.* FROM factions f
                JOIN claims c ON f.id = c.faction_id
                WHERE c.world = ? AND c.chunk_x = ? AND c.chunk_z = ?
                """,
                { extractFaction(it, executor) },
                location.world, location.chunkX, location.chunkZ
            )
        }
    }

    override fun addClaim(factionId: String, location: FLocation): Boolean {
        return withConnection { executor ->
            executor.update(
                """
                INSERT INTO claims (world, chunk_x, chunk_z, faction_id)
                VALUES (?, ?, ?, ?)
                """,
                location.world, location.chunkX, location.chunkZ, factionId
            ) > 0
        }
    }

    override fun removeClaim(location: FLocation): Boolean {
        return withConnection { executor ->
            executor.update(
                "DELETE FROM claims WHERE world = ? AND chunk_x = ? AND chunk_z = ?",
                location.world, location.chunkX, location.chunkZ
            ) > 0
        }
    }

    override fun setHome(factionId: String, location: Location): Boolean {
        return withConnection { executor ->
            executor.update(
                "UPDATE factions SET home = ? WHERE id = ?",
                SerializationUtil.serializeLocation(location), factionId
            ) > 0
        }
    }

    override fun setPower(factionId: String, power: Double): Boolean {
        return withConnection { executor ->
            executor.update(
                "UPDATE factions SET power = ? WHERE id = ?",
                power, factionId
            ) > 0
        }
    }

    override fun setMaxPower(factionId: String, maxPower: Double): Boolean {
        return withConnection { executor ->
            executor.update(
                "UPDATE factions SET max_power = ? WHERE id = ?",
                maxPower, factionId
            ) > 0
        }
    }

    override fun setOpen(factionId: String, open: Boolean): Boolean {
        return withConnection { executor ->
            executor.update(
                "UPDATE factions SET open = ? WHERE id = ?",
                open, factionId
            ) > 0
        }
    }

    override fun setLeader(factionId: String, leaderId: String): Boolean {
        return withConnection { executor ->
            executor.update(
                "UPDATE factions SET leader_id = ? WHERE id = ?",
                leaderId, factionId
            ) > 0
        }
    }

    override fun setDescription(factionId: String, description: String): Boolean {
        return withConnection { executor ->
            executor.update(
                "UPDATE factions SET description = ? WHERE id = ?",
                description, factionId
            ) > 0
        }
    }

    override fun getRelation(factionId: String, otherFactionId: String): FactionRelation {
        return withConnection { executor ->
            executor.query(
                "SELECT relation FROM relations WHERE faction_id = ? AND other_faction_id = ?",
                { rs -> FactionRelation.valueOf(rs.getString("relation")) },
                factionId, otherFactionId
            ) ?: FactionRelation.NEUTRAL
        }
    }

    override fun setRelation(factionId: String, otherFactionId: String, relation: FactionRelation): Boolean {
        return withConnection { executor ->
            executor.update(
                "DELETE FROM relations WHERE faction_id = ? AND other_faction_id = ?",
                factionId, otherFactionId
            )

            val updated = executor.update(
                "INSERT INTO relations (faction_id, other_faction_id, relation) VALUES (?, ?, ?)",
                factionId, otherFactionId, relation.name
            ) > 0

            executor.update(
                "INSERT INTO relations (faction_id, other_faction_id, relation) VALUES (?, ?, ?)",
                otherFactionId, factionId, relation.name
            )

            updated
        }
    }

    override fun removeRelation(factionId: String, otherFactionId: String): Boolean {
        TODO("Not yet implemented")
    }

    private fun extractFaction(rs: ResultSet, executor: SQLExecutor): Faction {
        val id = rs.getString("id")
        val homeStr = rs.getString("home")
        val home: Location? = if (homeStr != null) {
            SerializationUtil.deserializeLocation(homeStr)
        } else {
            null
        }

        val leaderIdStr = rs.getString("leader_id")
        val leaderId = if (leaderIdStr != null) UUID.fromString(leaderIdStr) else UUID(0, 0)

        return Faction(
            id = id,
            name = rs.getString("name"),
            description = rs.getString("description") ?: "",
            leaderId = leaderId,
            home = home,
            isOpen = rs.getBoolean("open"),
            power = rs.getDouble("power"),
            maxPower = rs.getDouble("max_power"),
            claims = getClaims(id, executor),
            relations = getRelations(id, executor)
        )
    }

    private fun getClaims(factionId: String, executor: SQLExecutor): Set<FLocation> {
        return executor.queryList(
            "SELECT world, chunk_x, chunk_z FROM claims WHERE faction_id = ?",
            { rs ->
                FLocation(
                    rs.getString("world"),
                    rs.getInt("chunk_x"),
                    rs.getInt("chunk_z")
                )
            },
            factionId
        ).toSet()
    }

    private fun getRelations(factionId: String, executor: SQLExecutor): Map<String, FactionRelation> {
        return executor.queryList(
            "SELECT other_faction_id, relation FROM relations WHERE faction_id = ?",
            { rs ->
                Pair(
                    rs.getString("other_faction_id"),
                    FactionRelation.valueOf(rs.getString("relation"))
                )
            },
            factionId
        ).toMap()
    }

    private fun updateClaims(faction: Faction, executor: SQLExecutor) {
        executor.update("DELETE FROM claims WHERE faction_id = ?", faction.id)

        if (faction.claims.isNotEmpty()) {
            saveClaims(faction, executor)
        }
    }

    private fun saveClaims(faction: Faction, executor: SQLExecutor) {
        val batch = faction.claims.map { claim ->
            arrayOf<Any?>(claim.world, claim.chunkX, claim.chunkZ, faction.id)
        }

        executor.batchUpdate(
            "INSERT INTO claims (world, chunk_x, chunk_z, faction_id) VALUES (?, ?, ?, ?)",
            batch
        )
    }

    private fun updateRelations(faction: Faction, executor: SQLExecutor) {
        executor.update("DELETE FROM relations WHERE faction_id = ?", faction.id)

        if (faction.relations.isNotEmpty()) {
            saveRelations(faction, executor)
        }
    }

    private fun saveRelations(faction: Faction, executor: SQLExecutor) {
        val batch = faction.relations.map { (otherFactionId, relation) ->
            arrayOf<Any?>(faction.id, otherFactionId, relation.name)
        }

        executor.batchUpdate(
            "INSERT INTO relations (faction_id, other_faction_id, relation) VALUES (?, ?, ?)",
            batch
        )
    }

}