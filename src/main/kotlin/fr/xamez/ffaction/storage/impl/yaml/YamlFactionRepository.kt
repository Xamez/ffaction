package fr.xamez.ffaction.storage.impl.yaml

import fr.xamez.ffaction.api.model.FLocation
import fr.xamez.ffaction.api.model.Faction
import fr.xamez.ffaction.api.model.FactionRelation
import fr.xamez.ffaction.api.repository.FactionRepository
import fr.xamez.ffaction.storage.impl.AbstractRepository
import fr.xamez.ffaction.util.SerializationUtil
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.*
import java.util.logging.Logger

class YamlFactionRepository(
    logger: Logger,
    private val config: YamlConfiguration,
    private val configFile: File,
    useCache: Boolean = true
) : AbstractRepository<Faction, String>(logger, useCache), FactionRepository {

    init {
        clearCache()
    }

    private fun saveConfig() {
        try {
            config.save(configFile)
        } catch (e: Exception) {
            logger.warning("Failed to save faction data to ${configFile.name}")
            e.printStackTrace()
        }
    }

    override fun fetchById(id: String): Faction? {
        val section = config.getConfigurationSection(id) ?: return null
        return loadFactionFromSection(id, section)
    }

    override fun fetchAll(): List<Faction> {
        val result = mutableListOf<Faction>()

        for (id in config.getKeys(false)) {
            val section = config.getConfigurationSection(id) ?: continue
            loadFactionFromSection(id, section)?.let { result.add(it) }
        }

        return result
    }

    override fun persist(entity: Faction): Boolean {
        return try {
            val path = entity.id

            config.set("$path.name", entity.name)
            config.set("$path.description", entity.description)
            config.set("$path.leaderId", entity.leaderId.toString())
            config.set("$path.open", entity.isOpen)
            config.set("$path.power", entity.power)
            config.set("$path.maxPower", entity.maxPower)

            if (entity.home != null) {
                config.set("$path.home.location", SerializationUtil.serializeLocation(entity.home))
            } else {
                config.set("$path.home", null)
            }

            config.set("$path.relations", null)
            entity.relations.forEach { (otherFactionId, relation) ->
                config.set("$path.relations.$otherFactionId", relation.name)
            }

            config.set("$path.claims", null)
            entity.claims.forEachIndexed { index, location ->
                config.set("$path.claims.$index.world", location.world)
                config.set("$path.claims.$index.chunkX", location.chunkX)
                config.set("$path.claims.$index.chunkZ", location.chunkZ)
            }

            config.set("$path.members", null)
            entity.members.forEachIndexed { index, uuid ->
                config.set("$path.members.$index", uuid.toString())
            }

            saveConfig()
            true
        } catch (e: Exception) {
            logger.warning("Failed to save faction ${entity.id}")
            e.printStackTrace()
            false
        }
    }

    override fun removeById(id: String): Boolean {
        return try {
            config.set(id, null)
            saveConfig()
            true
        } catch (e: Exception) {
            logger.warning("Failed to delete faction $id")
            e.printStackTrace()
            false
        }
    }

    override fun getEntityId(entity: Faction): String = entity.id

    override fun executeQuery(queryType: String, params: Map<String, Any?>): Any? {
        return when (queryType) {
            "findByName" -> {
                val name = params["name"] as? String ?: return null

                for (id in config.getKeys(false)) {
                    val section = config.getConfigurationSection(id) ?: continue
                    if (section.getString("name")?.equals(name, ignoreCase = true) == true) {
                        return loadFactionFromSection(id, section)
                    }
                }
                null
            }

            "findByLocation" -> {
                val location = params["location"] as? FLocation ?: return null

                for (id in config.getKeys(false)) {
                    val section = config.getConfigurationSection(id) ?: continue
                    val claimsSection = section.getConfigurationSection("claims") ?: continue

                    for (claimKey in claimsSection.getKeys(false)) {
                        val claim = claimsSection.getConfigurationSection(claimKey) ?: continue
                        val world = claim.getString("world") ?: continue
                        val chunkX = claim.getInt("chunkX")
                        val chunkZ = claim.getInt("chunkZ")

                        if (world == location.world && chunkX == location.chunkX && chunkZ == location.chunkZ) {
                            return loadFactionFromSection(id, section)
                        }
                    }
                }
                null
            }

            "getClaimsFor" -> {
                val factionId = params["factionId"] as? String ?: return emptySet<FLocation>()
                val claimsSection = config.getConfigurationSection("$factionId.claims") ?: return emptySet<FLocation>()

                val claims = mutableSetOf<FLocation>()
                for (key in claimsSection.getKeys(false)) {
                    val claim = claimsSection.getConfigurationSection(key) ?: continue
                    val world = claim.getString("world") ?: continue
                    val chunkX = claim.getInt("chunkX")
                    val chunkZ = claim.getInt("chunkZ")
                    claims.add(FLocation(world, chunkX, chunkZ))
                }
                claims
            }

            "addClaim" -> {
                val factionId = params["factionId"] as? String ?: return false
                val location = params["location"] as? FLocation ?: return false

                for (id in config.getKeys(false)) {
                    val claimsSection = config.getConfigurationSection("$id.claims") ?: continue
                    var claimToRemove: String? = null

                    for (claimKey in claimsSection.getKeys(false)) {
                        val claim = claimsSection.getConfigurationSection(claimKey) ?: continue
                        val world = claim.getString("world") ?: continue
                        val chunkX = claim.getInt("chunkX")
                        val chunkZ = claim.getInt("chunkZ")

                        if (world == location.world && chunkX == location.chunkX && chunkZ == location.chunkZ) {
                            claimToRemove = claimKey
                            break
                        }
                    }

                    if (claimToRemove != null) {
                        config.set("$id.claims.$claimToRemove", null)
                    }
                }

                val claimsSection = config.getConfigurationSection("$factionId.claims")
                    ?: config.createSection("$factionId.claims")
                val nextIndex = claimsSection.getKeys(false).size
                config.set("$factionId.claims.$nextIndex.world", location.world)
                config.set("$factionId.claims.$nextIndex.chunkX", location.chunkX)
                config.set("$factionId.claims.$nextIndex.chunkZ", location.chunkZ)

                saveConfig()
                true
            }

            "removeClaim" -> {
                val location = params["location"] as? FLocation ?: return false
                var success = false

                for (id in config.getKeys(false)) {
                    val claimsSection = config.getConfigurationSection("$id.claims") ?: continue
                    var claimToRemove: String? = null

                    for (claimKey in claimsSection.getKeys(false)) {
                        val claim = claimsSection.getConfigurationSection(claimKey) ?: continue
                        val world = claim.getString("world") ?: continue
                        val chunkX = claim.getInt("chunkX")
                        val chunkZ = claim.getInt("chunkZ")

                        if (world == location.world && chunkX == location.chunkX && chunkZ == location.chunkZ) {
                            claimToRemove = claimKey
                            break
                        }
                    }

                    if (claimToRemove != null) {
                        config.set("$id.claims.$claimToRemove", null)
                        success = true
                        break
                    }
                }

                if (success) {
                    saveConfig()
                }
                success
            }

            "setRelation" -> {
                val factionId = params["factionId"] as? String ?: return false
                val otherFactionId = params["otherFactionId"] as? String ?: return false
                val relation = params["relation"] as? FactionRelation ?: return false

                config.set("$factionId.relations.$otherFactionId", relation.name)
                saveConfig()
                true
            }

            "getRelation" -> {
                val factionId = params["factionId"] as? String ?: return null
                val otherFactionId = params["otherFactionId"] as? String ?: return null

                val relationName = config.getString("$factionId.relations.$otherFactionId") ?: return null
                try {
                    FactionRelation.valueOf(relationName)
                } catch (e: Exception) {
                    logger.warning("Invalid relation type: $relationName for faction $factionId")
                    FactionRelation.NEUTRAL
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

    private fun loadFactionFromSection(id: String, section: ConfigurationSection): Faction? {
        try {
            val leaderIdStr = section.getString("leaderId")
            val leaderId = if (leaderIdStr != null) UUID.fromString(leaderIdStr) else UUID(0, 0)

            val homeSection = section.getConfigurationSection("home")
            val home = if (homeSection != null) {
                val locationStr = homeSection.getString("location")
                if (locationStr != null) SerializationUtil.deserializeLocation(locationStr) else null
            } else null

            val relationsSection = section.getConfigurationSection("relations")
            val relations = mutableMapOf<String, FactionRelation>()
            if (relationsSection != null) {
                for (otherFactionId in relationsSection.getKeys(false)) {
                    val relationStr = relationsSection.getString(otherFactionId) ?: continue
                    try {
                        relations[otherFactionId] = FactionRelation.valueOf(relationStr)
                    } catch (e: Exception) {
                        logger.warning("Invalid relation type: $relationStr for faction $id")
                    }
                }
            }

            val claims = getClaimsFor(id)

            val members = mutableSetOf<UUID>()
            val membersSection = section.getConfigurationSection("members")
            if (membersSection != null) {
                for (key in membersSection.getKeys(false)) {
                    val uuidStr = membersSection.getString(key) ?: continue
                    try {
                        members.add(UUID.fromString(uuidStr))
                    } catch (e: Exception) {
                        logger.warning("Invalid UUID format for faction member in $id")
                    }
                }
            }

            return Faction(
                id = id,
                name = section.getString("name") ?: id,
                description = section.getString("description") ?: "",
                leaderId = leaderId,
                home = home,
                isOpen = section.getBoolean("open", false),
                power = section.getDouble("power", 0.0),
                maxPower = section.getDouble("maxPower", 10.0),
                claims = claims,
                relations = relations,
                members = members
            )
        } catch (e: Exception) {
            logger.warning("Failed to load faction $id: ${e.message}")
            e.printStackTrace()
            return null
        }
    }

}