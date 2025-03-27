package fr.xamez.ffaction.storage.impl.yaml

import fr.xamez.ffaction.api.model.FLocation
import fr.xamez.ffaction.api.model.Faction
import fr.xamez.ffaction.api.model.FactionRelation
import fr.xamez.ffaction.api.repository.FactionRepository
import fr.xamez.ffaction.util.SerializationUtil
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import java.util.UUID
import java.util.logging.Logger

// TODO: IMPLEMENT MISSING METHOD AND ADD CACHE

class YamlFactionRepository(
    private val logger: Logger,
    private val config: YamlConfiguration
) : FactionRepository {

    override fun getFaction(id: String): Faction? {
        val section = config.getConfigurationSection("factions.$id") ?: return null
        return loadFactionFromSection(id, section)
    }
    
    override fun getFactionByName(name: String): Faction? {
        val factionsSection = config.getConfigurationSection("factions") ?: return null
        
        for (key in factionsSection.getKeys(false)) {
            val section = factionsSection.getConfigurationSection(key) ?: continue
            if (section.getString("name")?.equals(name, ignoreCase = true) == true) {
                return loadFactionFromSection(key, section)
            }
        }
        
        return null
    }
    
    override fun getFactionAt(location: FLocation): Faction? {
        val claimsSection = config.getConfigurationSection("claims") ?: return null
        val chunkKey = "${location.world}_${location.chunkX}_${location.chunkZ}"
        
        val factionId = claimsSection.getString(chunkKey) ?: return null
        return getFaction(factionId)
    }
    
    override fun getAllFactions(): List<Faction> {
        val factions = mutableListOf<Faction>()
        val factionsSection = config.getConfigurationSection("factions") ?: return factions
        
        for (key in factionsSection.getKeys(false)) {
            val section = factionsSection.getConfigurationSection(key) ?: continue
            loadFactionFromSection(key, section)?.let { factions.add(it) }
        }
        
        return factions
    }
    
    override fun saveFaction(faction: Faction): Boolean {
        return try {
            val path = "factions.${faction.id}"
            config.set("$path.name", faction.name)
            config.set("$path.description", faction.description)
            config.set("$path.leaderId", faction.leaderId?.toString())
            
            faction.home?.let {
                config.set("$path.home.location", SerializationUtil.serializeLocation(it))
            }

            val relationsSection = config.createSection("$path.relations")
            faction.relations.forEach { (factionId, relation) ->
                relationsSection.set(factionId, relation.name)
            }

            faction.claims.forEach { location ->
                val chunkKey = "${location.world}_${location.chunkX}_${location.chunkZ}"
                config.set("claims.$chunkKey", faction.id)
            }
            
            true
        } catch (e: Exception) {
            logger.warning("Failed to save faction ${faction.id}")
            e.printStackTrace()
            false
        }
    }
    
    override fun deleteFaction(id: String): Boolean {
        return try {
            config.set("factions.$id", null)

            val claimsSection = config.getConfigurationSection("claims") ?: return true
            val claimsToRemove = mutableListOf<String>()
            
            for (key in claimsSection.getKeys(false)) {
                if (claimsSection.getString(key) == id) {
                    claimsToRemove.add(key)
                }
            }
            
            claimsToRemove.forEach { key ->
                config.set("claims.$key", null)
            }
            
            true
        } catch (e: Exception) {
            logger.warning("Failed to delete faction $id")
            e.printStackTrace()
            false
        }
    }
    
    override fun getClaimedChunks(factionId: String): Set<FLocation> {
        val result = mutableSetOf<FLocation>()
        val claimsSection = config.getConfigurationSection("claims") ?: return result
        
        for (key in claimsSection.getKeys(false)) {
            if (claimsSection.getString(key) == factionId) {
                val parts = key.split("_")
                if (parts.size == 3) {
                    try {
                        val world = parts[0]
                        val chunkX = parts[1].toInt()
                        val chunkZ = parts[2].toInt()
                        result.add(FLocation(world, chunkX, chunkZ))
                    } catch (e: Exception) {
                        logger.warning("Failed to get chunks $key")
                        e.printStackTrace()
                    }
                }
            }
        }
        
        return result
    }
    
    override fun addClaim(factionId: String, location: FLocation): Boolean {
        return try {
            val chunkKey = "${location.world}_${location.chunkX}_${location.chunkZ}"
            config.set("claims.$chunkKey", factionId)
            true
        } catch (e: Exception) {
            logger.warning("Failed to add claim $location")
            e.printStackTrace()
            false
        }
    }
    
    override fun removeClaim(location: FLocation): Boolean {
        return try {
            val chunkKey = "${location.world}_${location.chunkX}_${location.chunkZ}"
            config.set("claims.$chunkKey", null)
            true
        } catch (e: Exception) {
            logger.warning("Failed to remove claim $location")
            e.printStackTrace()
            false
        }
    }
    
    private fun loadFactionFromSection(id: String, section: ConfigurationSection): Faction? {
        return try {
            val leaderId = section.getString("leaderId")?.let { UUID.fromString(it) }
            
            val home = section.getConfigurationSection("home")?.let {
                SerializationUtil.deserializeLocation(it.getString("location")!!)
            }

            val relations = mutableMapOf<String, FactionRelation>()
            section.getConfigurationSection("relations")?.let { relationsSection ->
                for (key in relationsSection.getKeys(false)) {
                    val relationName = relationsSection.getString(key) ?: continue
                    try {
                        relations[key] = FactionRelation.valueOf(relationName)
                    } catch (e: Exception) {
                        logger.warning("Failed to get relation $key")
                        e.printStackTrace()
                    }
                }
            }
            
            Faction(
                id = id,
                name = section.getString("name") ?: return null,
                description = section.getString("description") ?: "",
                leaderId = leaderId,
                home = home,
                relations = relations,
                claims = getClaimedChunks(id)
            )
        } catch (e: Exception) {
            logger.warning("Failed to load faction $id")
            e.printStackTrace()
            null
        }
    }
}