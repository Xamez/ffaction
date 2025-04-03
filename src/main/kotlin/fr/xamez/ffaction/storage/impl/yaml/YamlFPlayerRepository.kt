package fr.xamez.ffaction.storage.impl.yaml

import fr.xamez.ffaction.api.model.FPlayer
import fr.xamez.ffaction.api.model.FactionRole
import fr.xamez.ffaction.api.repository.FPlayerRepository
import fr.xamez.ffaction.storage.impl.AbstractRepository
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.*
import java.util.logging.Logger

class YamlFPlayerRepository(
    logger: Logger,
    private val config: YamlConfiguration,
    private val configFile: File,
    useCache: Boolean = true
) : AbstractRepository<FPlayer, UUID>(logger, useCache), FPlayerRepository {

    init {
        clearCache()
    }

    private fun saveConfig() {
        try {
            config.save(configFile)
        } catch (e: Exception) {
            logger.warning("Failed to save player data to ${configFile.name}")
            e.printStackTrace()
        }
    }

    override fun fetchById(id: UUID): FPlayer? {
        val section = config.getConfigurationSection("$id") ?: return null
        return loadPlayerFromSection(id, section)
    }

    override fun fetchAll(): List<FPlayer> {
        val result = mutableListOf<FPlayer>()

        for (idStr in config.getKeys(false)) {
            try {
                val id = UUID.fromString(idStr)
                val section = config.getConfigurationSection(idStr) ?: continue
                loadPlayerFromSection(id, section)?.let { result.add(it) }
            } catch (e: IllegalArgumentException) {
                logger.warning("Invalid UUID format in player data: $idStr")
            }
        }

        return result
    }

    override fun persist(entity: FPlayer): Boolean {
        return try {
            val path = "${entity.uuid}"

            config.set("$path.name", entity.name)
            config.set("$path.factionId", entity.factionId)
            config.set("$path.role", entity.role.name)
            config.set("$path.power", entity.power)
            config.set("$path.maxPower", entity.maxPower)

            saveConfig()
            true
        } catch (e: Exception) {
            logger.warning("Failed to save player ${entity.uuid}")
            e.printStackTrace()
            false
        }
    }

    override fun removeById(id: UUID): Boolean {
        return try {
            config.set("$id", null)
            saveConfig()
            true
        } catch (e: Exception) {
            logger.warning("Failed to delete player $id")
            e.printStackTrace()
            false
        }
    }

    override fun getEntityId(entity: FPlayer): UUID = entity.uuid

    override fun executeQuery(queryType: String, params: Map<String, Any?>): Any? {
        return when (queryType) {
            "findByName" -> {
                val name = params["name"] as? String ?: return null

                for (idStr in config.getKeys(false)) {
                    try {
                        val id = UUID.fromString(idStr)
                        val section = config.getConfigurationSection(idStr) ?: continue
                        if (section.getString("name")?.equals(name, ignoreCase = true) == true) {
                            return loadPlayerFromSection(id, section)
                        }
                    } catch (e: IllegalArgumentException) {
                        continue
                    }
                }
                null
            }

            else -> null
        }
    }

    override fun findByName(name: String): FPlayer? {
        return query("findByName", mapOf("name" to name)) { it as? FPlayer }
    }

    private fun loadPlayerFromSection(id: UUID, section: org.bukkit.configuration.ConfigurationSection): FPlayer? {
        try {
            val name = section.getString("name") ?: return null
            val factionId = section.getString("factionId")
            val roleStr = section.getString("role") ?: FactionRole.MEMBER.name
            val role = try {
                FactionRole.valueOf(roleStr)
            } catch (e: Exception) {
                logger.warning("Invalid faction role: $roleStr for player $name")
                FactionRole.MEMBER
            }
            val power = section.getDouble("power", 10.0)
            val maxPower = section.getDouble("maxPower", 20.0)

            return FPlayer(
                uuid = id,
                name = name,
                factionId = factionId,
                role = role,
                power = power,
                maxPower = maxPower
            )
        } catch (e: Exception) {
            logger.warning("Failed to load player $id: ${e.message}")
            e.printStackTrace()
            return null
        }
    }

}