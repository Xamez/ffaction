package fr.xamez.ffaction.api

import fr.xamez.ffaction.api.model.*
import fr.xamez.ffaction.api.service.FactionService
import fr.xamez.ffaction.storage.StorageManager
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.*

class FFactionAPIImpl(
    private val storageManager: StorageManager,
    private val plugin: Plugin
) : FFactionAPI {

    private val factionService: FactionService by lazy {
        val provider = storageManager.getProvider()
            ?: throw IllegalStateException("Storage provider not initialized")

        FactionService(
            provider.getFactionRepository(),
            provider.getFPlayerRepository()
        )
    }

    override fun getPlayer(uuid: UUID): FPlayer? = factionService.getPlayer(uuid)

    override fun getPlayer(player: Player): FPlayer? = getPlayer(player.uniqueId)

    override fun getPlayerByName(name: String): FPlayer? = factionService.getPlayerByName(name)

    override fun savePlayer(player: FPlayer): Boolean = factionService.savePlayer(player)

    override fun getPlayerFaction(player: FPlayer): Faction? = factionService.getPlayerFaction(player)

    override fun getPlayerFaction(player: Player): Faction? {
        val fPlayer = getPlayer(player.uniqueId) ?: return null
        return factionService.getPlayerFaction(fPlayer)
    }

    override fun getFaction(id: String): Faction? = factionService.getFaction(id)

    override fun getFactionByName(name: String): Faction? = factionService.getFactionByName(name)

    override fun getAllFactions(): List<Faction> = factionService.getAllFactions()

    override fun createFaction(name: String, player: Player): Faction? {
        val fPlayer = getPlayer(player)
        if (fPlayer == null) {
            plugin.logger.warning("Player ${player.name} not found")
            return null
        }

        val currentFaction = getPlayerFaction(fPlayer)
        if (currentFaction != null) {
            plugin.logger.info("Player ${player.name} already has a faction: ${currentFaction.name}")
            return null
        }

        return factionService.createFaction(name, fPlayer)
    }

    override fun disbandFaction(faction: Faction): Boolean = factionService.disbandFaction(faction)

    override fun setFactionHome(faction: Faction, location: Location): Boolean {
        return factionService.setFactionHome(faction, location)
    }

    override fun setFactionDescription(faction: Faction, description: String): Boolean {
        return factionService.setFactionDescription(faction, description)
    }

    override fun setFactionOpenStatus(faction: Faction, isOpen: Boolean): Boolean {
        return factionService.setFactionOpenStatus(faction, isOpen)
    }

    override fun setFactionName(faction: Faction, name: String): Boolean {
        return factionService.setFactionName(faction, name)
    }

    override fun setFactionLeader(faction: Faction, player: FPlayer): Boolean {
        return factionService.setFactionLeader(faction, player)
    }

    override fun getFactionAt(location: Location): Faction? {
        val fLocation = FLocation(location.world.name, location.blockX, location.blockZ)
        return getFactionAt(fLocation)
    }

    override fun getFactionAt(chunk: Chunk): Faction? {
        val fLocation = FLocation(chunk.world.name, chunk.x, chunk.z)
        return getFactionAt(fLocation)
    }

    override fun getFactionAt(fLocation: FLocation): Faction? {
        return factionService.getFactionAt(fLocation)
    }

    override fun claimLand(faction: Faction, chunk: Chunk): Boolean {
        val fLocation = FLocation(chunk.world.name, chunk.x, chunk.z)
        return claimLand(faction, fLocation)
    }

    override fun claimLand(faction: Faction, fLocation: FLocation): Boolean {
        return factionService.claimLand(faction, fLocation)
    }

    override fun unclaimLand(chunk: Chunk): Boolean {
        val fLocation = FLocation(chunk.world.name, chunk.x, chunk.z)
        return factionService.unclaimLand(fLocation)
    }

    override fun unclaimLand(fLocation: FLocation): Boolean {
        return factionService.unclaimLand(fLocation)
    }

    override fun getClaimsFor(faction: Faction): Set<FLocation> {
        val provider = storageManager.getProvider() ?: return emptySet()
        return provider.getFactionRepository().getClaimsFor(faction.id)
    }

    override fun setRelation(faction: Faction, otherFaction: Faction, relation: FactionRelation): Boolean {
        return factionService.setRelation(faction, otherFaction, relation)
    }

    override fun getRelation(faction: Faction, otherFaction: Faction): FactionRelation {
        return factionService.getRelation(faction, otherFaction)
    }

    override fun getFactionsPlayers(faction: Faction): List<FPlayer> {
        return factionService.getFactionsPlayers(faction)
    }

    override fun setPlayerRole(player: FPlayer, role: FactionRole): Boolean {
        return factionService.setPlayerRole(player, role)
    }

    override fun setPlayerFaction(player: FPlayer, faction: Faction?): Boolean {
        return factionService.setPlayerFaction(player, faction)
    }

}