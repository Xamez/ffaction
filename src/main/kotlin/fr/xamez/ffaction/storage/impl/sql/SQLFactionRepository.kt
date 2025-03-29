package fr.xamez.ffaction.storage.impl.sql

import com.zaxxer.hikari.HikariDataSource
import fr.xamez.ffaction.api.model.FLocation
import fr.xamez.ffaction.api.model.Faction
import fr.xamez.ffaction.api.model.FactionRelation
import fr.xamez.ffaction.api.repository.FactionRepository
import fr.xamez.ffaction.storage.impl.AbstractRepository
import fr.xamez.ffaction.util.SerializationUtil
import java.util.*
import java.util.logging.Logger

class SQLFactionRepository(
    logger: Logger,
    private val dataSource: HikariDataSource,
    useCache: Boolean = true
) : AbstractRepository<Faction, String>(logger, useCache), FactionRepository {

    init {
        initTables()
    }

    override fun fetchById(id: String): Faction? {
        return withConnection { executor ->
            executor.query(
                "SELECT * FROM factions WHERE id = ?",
                { rs ->
                    if (rs.next()) {
                        extractFaction(rs, executor)
                    } else null
                },
                id
            )
        }
    }

    override fun fetchAll(): List<Faction> {
        return withConnection { executor ->
            val factions = mutableListOf<Faction>()
            executor.query(
                "SELECT * FROM factions",
                { rs ->
                    while (rs.next()) {
                        extractFaction(rs, executor)?.let { factions.add(it) }
                    }
                    factions.toList()
                }
            ) ?: emptyList()
        }
    }

    override fun persist(entity: Faction): Boolean {
        return withConnection { executor ->
            val exists = executor.query(
                "SELECT 1 FROM factions WHERE id = ?",
                { rs -> rs.next() },
                entity.id
            )

            if (exists == true) {
                val updated = executor.update(
                    """
                    UPDATE factions SET 
                        name = ?, 
                        description = ?, 
                        leader_id = ?, 
                        home = ?,
                        open = ?,
                        power = ?,
                        max_power = ?
                    WHERE id = ?
                    """,
                    entity.name,
                    entity.description,
                    entity.leaderId.toString(),
                    entity.home?.let { SerializationUtil.serializeLocation(it) },
                    entity.isOpen,
                    entity.power,
                    entity.maxPower,
                    entity.id
                ) > 0

                if (updated) {
                    executor.update("DELETE FROM relations WHERE faction_id = ?", entity.id)

                    entity.relations.forEach { (otherFactionId, relation) ->
                        executor.update(
                            "INSERT INTO relations (faction_id, other_faction_id, relation) VALUES (?, ?, ?)",
                            entity.id, otherFactionId, relation.name
                        )
                    }
                }

                if (updated) {
                    executor.update("DELETE FROM claims WHERE faction_id = ?", entity.id)

                    entity.claims.forEach { loc ->
                        executor.update(
                            "INSERT INTO claims (world, chunk_x, chunk_z, faction_id) VALUES (?, ?, ?, ?)",
                            loc.world, loc.chunkX, loc.chunkZ, entity.id
                        )
                    }
                }

                updated
            } else {
                val inserted = executor.update(
                    """
                    INSERT INTO factions (id, name, description, leader_id, home, open, power, max_power) 
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    entity.id,
                    entity.name,
                    entity.description,
                    entity.leaderId.toString(),
                    entity.home?.let { SerializationUtil.serializeLocation(it) },
                    entity.isOpen,
                    entity.power,
                    entity.maxPower
                ) > 0

                if (inserted) {
                    entity.relations.forEach { (otherFactionId, relation) ->
                        executor.update(
                            "INSERT INTO relations (faction_id, other_faction_id, relation) VALUES (?, ?, ?)",
                            entity.id, otherFactionId, relation.name
                        )
                    }

                    entity.claims.forEach { loc ->
                        executor.update(
                            "INSERT INTO claims (world, chunk_x, chunk_z, faction_id) VALUES (?, ?, ?, ?)",
                            loc.world, loc.chunkX, loc.chunkZ, entity.id
                        )
                    }
                }

                inserted
            }
        }
    }

    override fun removeById(id: String): Boolean {
        return withConnection { executor ->
            executor.update("DELETE FROM factions WHERE id = ?", id) > 0
        }
    }

    override fun getEntityId(entity: Faction): String = entity.id

    override fun executeQuery(queryType: String, params: Map<String, Any?>): Any? {
        return when (queryType) {
            "findByName" -> {
                val name = params["name"] as? String ?: return null
                withConnection { executor ->
                    executor.query(
                        "SELECT * FROM factions WHERE name = ?",
                        { rs ->
                            if (rs.next()) extractFaction(rs, executor) else null
                        },
                        name
                    )
                }
            }

            "findByLocation" -> {
                val location = params["location"] as? FLocation ?: return null
                withConnection { executor ->
                    executor.query(
                        "SELECT f.* FROM factions f JOIN claims c ON f.id = c.faction_id " +
                                "WHERE c.world = ? AND c.chunk_x = ? AND c.chunk_z = ?",
                        { rs ->
                            if (rs.next()) extractFaction(rs, executor) else null
                        },
                        location.world, location.chunkX, location.chunkZ
                    )
                }
            }

            "getClaimsFor" -> {
                val factionId = params["factionId"] as? String ?: return null
                withConnection { executor ->
                    val claims = mutableSetOf<FLocation>()
                    executor.query(
                        "SELECT world, chunk_x, chunk_z FROM claims WHERE faction_id = ?",
                        { rs ->
                            while (rs.next()) {
                                claims.add(
                                    FLocation(
                                        rs.getString("world"),
                                        rs.getInt("chunk_x"),
                                        rs.getInt("chunk_z")
                                    )
                                )
                            }
                            claims
                        },
                        factionId
                    )
                }
            }

            "addClaim" -> {
                val factionId = params["factionId"] as? String ?: return false
                val location = params["location"] as? FLocation ?: return false
                withConnection { executor ->
                    executor.update(
                        "DELETE FROM claims WHERE world = ? AND chunk_x = ? AND chunk_z = ?",
                        location.world, location.chunkX, location.chunkZ
                    )

                    executor.update(
                        "INSERT INTO claims (world, chunk_x, chunk_z, faction_id) VALUES (?, ?, ?, ?)",
                        location.world, location.chunkX, location.chunkZ, factionId
                    ) > 0
                }
            }

            "removeClaim" -> {
                val location = params["location"] as? FLocation ?: return false
                withConnection { executor ->
                    executor.update(
                        "DELETE FROM claims WHERE world = ? AND chunk_x = ? AND chunk_z = ?",
                        location.world, location.chunkX, location.chunkZ
                    ) > 0
                }
            }

            "setRelation" -> {
                val factionId = params["factionId"] as? String ?: return false
                val otherFactionId = params["otherFactionId"] as? String ?: return false
                val relation = params["relation"] as? FactionRelation ?: return false

                withConnection { executor ->
                    executor.update(
                        "DELETE FROM relations WHERE faction_id = ? AND other_faction_id = ?",
                        factionId, otherFactionId
                    )

                    executor.update(
                        "INSERT INTO relations (faction_id, other_faction_id, relation) VALUES (?, ?, ?)",
                        factionId, otherFactionId, relation.name
                    ) > 0
                }
            }

            "getRelation" -> {
                val factionId = params["factionId"] as? String ?: return null
                val otherFactionId = params["otherFactionId"] as? String ?: return null

                withConnection { executor ->
                    executor.query(
                        "SELECT relation FROM relations WHERE faction_id = ? AND other_faction_id = ?",
                        { rs ->
                            if (rs.next()) {
                                try {
                                    FactionRelation.valueOf(rs.getString("relation"))
                                } catch (e: Exception) {
                                    FactionRelation.NEUTRAL
                                }
                            } else null
                        },
                        factionId, otherFactionId
                    )
                }
            }

            else -> null
        }
    }

    override fun findByName(name: String): Faction? {
        return query("findByName", mapOf("name" to name)) { it as? Faction }
    }

    override fun findByLocation(location: FLocation): Faction? {
        return query("findByLocation", mapOf("location" to location)) { it as? Faction }
    }

    override fun getClaimsFor(factionId: String): Set<FLocation> {
        return query("getClaimsFor", mapOf("factionId" to factionId)) { it as? Set<FLocation> } ?: emptySet()
    }

    override fun addClaim(factionId: String, location: FLocation): Boolean {
        return query("addClaim", mapOf("factionId" to factionId, "location" to location)) { it as? Boolean } ?: false
    }

    override fun removeClaim(location: FLocation): Boolean {
        return query("removeClaim", mapOf("location" to location)) { it as? Boolean } ?: false
    }

    override fun setRelation(factionId: String, otherFactionId: String, relation: FactionRelation): Boolean {
        return query(
            "setRelation", mapOf(
                "factionId" to factionId,
                "otherFactionId" to otherFactionId,
                "relation" to relation
            )
        ) { it as? Boolean } ?: false
    }

    override fun getRelation(factionId: String, otherFactionId: String): FactionRelation? {
        return query(
            "getRelation", mapOf(
                "factionId" to factionId,
                "otherFactionId" to otherFactionId
            )
        ) { it as? FactionRelation }
    }

    private fun extractFaction(rs: java.sql.ResultSet, executor: SQLExecutor): Faction? {
        try {
            val id = rs.getString("id")
            val leaderIdStr = rs.getString("leader_id")
            val leaderId = if (leaderIdStr != null) UUID.fromString(leaderIdStr) else UUID(0, 0)
            val homeStr = rs.getString("home")
            val home = homeStr?.let { SerializationUtil.deserializeLocation(it) }

            val relations = mutableMapOf<String, FactionRelation>()
            executor.query(
                "SELECT other_faction_id, relation FROM relations WHERE faction_id = ?",
                { relationsRs ->
                    while (relationsRs.next()) {
                        val otherFactionId = relationsRs.getString("other_faction_id")
                        val relationStr = relationsRs.getString("relation")
                        try {
                            relations[otherFactionId] = FactionRelation.valueOf(relationStr)
                        } catch (e: Exception) {
                            logger.warning("Invalid relation type: $relationStr for faction $id")
                        }
                    }
                },
                id
            )

            val claims = getClaimsFor(id)

            return Faction(
                id = id,
                name = rs.getString("name"),
                description = rs.getString("description") ?: "",
                leaderId = leaderId,
                home = home,
                isOpen = rs.getBoolean("open"),
                power = rs.getDouble("power"),
                maxPower = rs.getDouble("max_power"),
                claims = claims,
                relations = relations
            )
        } catch (e: Exception) {
            logger.warning("Failed to extract faction: ${e.message}")
            return null
        }
    }

    private fun initTables() {
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
    }

    private fun <T> withConnection(action: (SQLExecutor) -> T): T {
        dataSource.connection.use { connection ->
            val executor = SQLExecutor(logger, connection)
            return action(executor)
        }
    }
}