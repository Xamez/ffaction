package fr.xamez.ffaction.storage.impl.sql

import fr.xamez.ffaction.api.model.FPlayer
import fr.xamez.ffaction.api.model.FactionRole
import fr.xamez.ffaction.api.repository.FPlayerRepository
import fr.xamez.ffaction.storage.impl.cache.FPlayerCache
import java.util.*

class CachedSQLFPlayerRepository(private val repository: FPlayerRepository) : FPlayerRepository {

    private val cache = FPlayerCache()

    override fun getPlayer(uuid: UUID): FPlayer? {
        return cache.getPlayer(uuid) ?: repository.getPlayer(uuid)?.also { cache.cachePlayer(it) }
    }

    override fun getPlayerByName(name: String): FPlayer? {
        return cache.getPlayerByName(name) ?: repository.getPlayerByName(name)?.also { cache.cachePlayer(it) }
    }

    override fun getAllPlayers(): List<FPlayer> {
        val cachedPlayers = cache.getAllPlayers()
        if (cachedPlayers.isNotEmpty()) {
            return cachedPlayers
        }

        val allPlayers = repository.getAllPlayers()
        allPlayers.forEach { cache.cachePlayer(it) }
        return allPlayers
    }

    override fun savePlayer(player: FPlayer): Boolean {
        val result = repository.savePlayer(player)
        if (result) {
            cache.updatePlayer(player)
        }
        return result
    }

    override fun deletePlayer(uuid: UUID): Boolean {
        val result = repository.deletePlayer(uuid)
        if (result) {
            cache.removePlayer(uuid)
        }
        return result
    }

    override fun getPlayersInFaction(factionId: String): List<FPlayer> {
        val cachedPlayers = cache.getPlayersInFaction(factionId)
        if (cachedPlayers.isNotEmpty()) {
            return cachedPlayers
        }

        val factionPlayers = repository.getPlayersInFaction(factionId)
        factionPlayers.forEach { cache.cachePlayer(it) }
        return factionPlayers
    }

    fun setFaction(uuid: UUID, factionId: String?): Boolean {
        val player = getPlayer(uuid) ?: return false
        val updatedPlayer = player.copy(factionId = factionId)
        return savePlayer(updatedPlayer)
    }

    fun setRole(uuid: UUID, role: FactionRole): Boolean {
        val player = getPlayer(uuid) ?: return false
        val updatedPlayer = player.copy(role = role)
        return savePlayer(updatedPlayer)
    }

    fun setPower(uuid: UUID, power: Double): Boolean {
        val player = getPlayer(uuid) ?: return false
        val updatedPlayer = player.copy(power = power)
        return savePlayer(updatedPlayer)
    }

    fun setMaxPower(uuid: UUID, maxPower: Double): Boolean {
        val player = getPlayer(uuid) ?: return false
        val updatedPlayer = player.copy(maxPower = maxPower)
        return savePlayer(updatedPlayer)
    }

}