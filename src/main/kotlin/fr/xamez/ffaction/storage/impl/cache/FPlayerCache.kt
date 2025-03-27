package fr.xamez.ffaction.storage.impl.cache

import fr.xamez.ffaction.api.model.FPlayer
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class FPlayerCache {

    private val playersByUuid = ConcurrentHashMap<UUID, FPlayer>()
    private val playersByName = ConcurrentHashMap<String, UUID>()
    private val playersByFaction = ConcurrentHashMap<String, MutableSet<UUID>>()

    fun getPlayer(uuid: UUID): FPlayer? = playersByUuid[uuid]

    fun getPlayerByName(name: String): FPlayer? {
        val uuid = playersByName[name.lowercase()] ?: return null
        return playersByUuid[uuid]
    }

    fun getPlayersInFaction(factionId: String): List<FPlayer> {
        val uuids = playersByFaction[factionId] ?: return emptyList()
        return uuids.mapNotNull { playersByUuid[it] }
    }

    fun getAllPlayers(): List<FPlayer> = playersByUuid.values.toList()

    fun cachePlayer(player: FPlayer) {
        playersByUuid[player.uuid] = player
        playersByName[player.name.lowercase()] = player.uuid
        
        if (player.factionId != null) {
            val factionPlayers = playersByFaction.getOrPut(player.factionId) { mutableSetOf() }
            factionPlayers.add(player.uuid)
        }
    }

    fun updatePlayer(player: FPlayer) {
        val oldPlayer = playersByUuid[player.uuid]

        if (oldPlayer != null) {
            // Update name mapping if needed
            if (oldPlayer.name != player.name) {
                playersByName.remove(oldPlayer.name.lowercase())
                playersByName[player.name.lowercase()] = player.uuid
            }

            // Update faction mapping if needed
            if (oldPlayer.factionId != player.factionId) {
                oldPlayer.factionId?.let { oldFactionId ->
                    playersByFaction[oldFactionId]?.remove(player.uuid)
                }

                player.factionId?.let { newFactionId ->
                    val factionPlayers = playersByFaction.getOrPut(newFactionId) { mutableSetOf() }
                    factionPlayers.add(player.uuid)
                }
            }
        }

        playersByUuid[player.uuid] = player
    }

    fun removePlayer(uuid: UUID) {
        val player = playersByUuid[uuid] ?: return

        playersByUuid.remove(uuid)
        playersByName.remove(player.name.lowercase())
        
        player.factionId?.let { factionId ->
            playersByFaction[factionId]?.remove(uuid)
        }
    }

    fun updatePlayerField(uuid: UUID, updater: (FPlayer) -> FPlayer) {
        val player = playersByUuid[uuid] ?: return
        val updatedPlayer = updater(player)
        updatePlayer(updatedPlayer)
    }

    fun clear() {
        playersByUuid.clear()
        playersByName.clear()
        playersByFaction.clear()
    }

}