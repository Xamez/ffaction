package fr.xamez.ffaction.storage.impl.yaml

import fr.xamez.ffaction.api.model.FPlayer
import fr.xamez.ffaction.api.model.FactionRole
import fr.xamez.ffaction.api.repository.FPlayerRepository
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import java.util.*
import java.util.logging.Logger

class YamlFPlayerRepository(
    private val logger: Logger,
    private val config: YamlConfiguration
) : FPlayerRepository {

    // TODO: IMPLEMENT MISSING METHOD AND ADD CACHE

    override fun getPlayer(uuid: UUID): FPlayer? {
        val section = config.getConfigurationSection("players.${uuid}") ?: return null
        return loadPlayerFromSection(uuid, section)
    }

    override fun getPlayerByName(name: String): FPlayer? {
        val playersSection = config.getConfigurationSection("players") ?: return null

        for (key in playersSection.getKeys(false)) {
            val section = playersSection.getConfigurationSection(key) ?: continue
            if (section.getString("name")?.equals(name, ignoreCase = true) == true) {
                return loadPlayerFromSection(UUID.fromString(key), section)
            }
        }

        return null
    }

    override fun getAllPlayers(): List<FPlayer> {
        val result = mutableListOf<FPlayer>()
        val playersSection = config.getConfigurationSection("players") ?: return result

        for (key in playersSection.getKeys(false)) {
            val section = playersSection.getConfigurationSection(key) ?: continue
            try {
                val uuid = UUID.fromString(key)
                loadPlayerFromSection(uuid, section)?.let { result.add(it) }
            } catch (e: Exception) {
                logger.warning("Invalid UUID found in players section: $key")
                e.printStackTrace()
            }
        }

        return result
    }

    override fun getPlayersInFaction(factionId: String): List<FPlayer> {
        return getAllPlayers().filter { it.factionId == factionId }
    }

    override fun savePlayer(player: FPlayer): Boolean {
        return try {
            val path = "players.${player.uuid}"
            config.set("$path.name", player.name)
            config.set("$path.factionId", player.factionId)
            config.set("$path.role", player.role.name)
            config.set("$path.power", player.power)
            config.set("$path.maxPower", player.maxPower)
            true
        } catch (e: Exception) {
            logger.warning("Error while saving player ${player.name}")
            e.printStackTrace()
            false
        }
    }

    override fun deletePlayer(uuid: UUID): Boolean {
        return try {
            config.set("players.$uuid", null)
            true
        } catch (e: Exception) {
            logger.warning("Could not delete player $uuid")
            e.printStackTrace()
            false
        }
    }

    private fun loadPlayerFromSection(uuid: UUID, section: ConfigurationSection): FPlayer? {
        return try {
            FPlayer(
                uuid = uuid,
                name = section.getString("name") ?: return null,
                factionId = section.getString("factionId"),
                role = section.getString("role")?.let {
                    try {
                        FactionRole.valueOf(it)
                    } catch (e: Exception) {
                        FactionRole.MEMBER
                    }
                } ?: FactionRole.MEMBER,
                power = section.getDouble("power", 10.0),
                maxPower = section.getDouble("maxPower", 20.0)
            )
        } catch (e: Exception) {
            logger.warning("Failed to load player $uuid")
            e.printStackTrace()
            null
        }
    }
}