package fr.xamez.ffaction.api.service

import fr.xamez.ffaction.api.model.*
import fr.xamez.ffaction.api.repository.FPlayerRepository
import fr.xamez.ffaction.api.repository.FactionRepository
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.*

class FactionService(
    private val factionRepository: FactionRepository,
    private val fPlayerRepository: FPlayerRepository
) {

    fun getPlayer(uuid: UUID): FPlayer? = fPlayerRepository.findById(uuid)

    fun getPlayer(player: Player): FPlayer? = getPlayer(player.uniqueId)

    fun getPlayerByName(name: String): FPlayer? = fPlayerRepository.findByName(name)

    fun createPlayerIfNotExists(player: Player): FPlayer {
        val existingPlayer = getPlayer(player.uniqueId)
        if (existingPlayer != null) {
            return existingPlayer
        }

        val newPlayer = FPlayer(
            uuid = player.uniqueId,
            name = player.name,
            role = FactionRole.MEMBER,
            factionId = null
        )

        savePlayer(newPlayer)
        return newPlayer
    }

    fun savePlayer(player: FPlayer): Boolean = fPlayerRepository.save(player)

    fun getPlayerFaction(player: FPlayer): Faction? {
        return player.factionId?.let { factionRepository.findById(it) }
    }

    fun getPlayerFaction(player: Player): Faction? {
        return getPlayer(player)?.let { getPlayerFaction(it) }
    }

    fun setPlayerFaction(player: FPlayer, faction: Faction?): Boolean {
        val updatedPlayer = player.copy(
            factionId = faction?.id,
            role = if (faction == null) FactionRole.MEMBER else player.role
        )
        return fPlayerRepository.save(updatedPlayer)
    }

    fun setPlayerRole(player: FPlayer, role: FactionRole): Boolean {
        return fPlayerRepository.save(player.copy(role = role))
    }

    fun getFaction(id: String): Faction? = factionRepository.findById(id)

    fun getFactionByName(name: String): Faction? = factionRepository.findByName(name)

    fun getAllFactions(): List<Faction> = factionRepository.findAll()

    fun createFaction(name: String, player: FPlayer): Faction? {
        if (getFactionByName(name) != null) {
            return null
        }

        val id = generateUniqueId(name)

        val faction = Faction(
            id = id,
            name = name,
            description = "",
            leaderId = player.uuid,
            home = null,
            isOpen = false,
            power = 0.0,
            maxPower = 10.0,
            claims = emptySet(),
            relations = emptyMap()
        )

        if (!factionRepository.save(faction)) {
            return null
        }

        val updatedPlayer = player.copy(
            factionId = id,
            role = FactionRole.LEADER
        )

        if (!fPlayerRepository.save(updatedPlayer)) {
            factionRepository.delete(id)
            return null
        }

        return faction
    }

    fun disbandFaction(faction: Faction): Boolean {
        val members = getFactionsPlayers(faction)
        for (member in members) {
            val updatedMember = member.copy(
                factionId = null,
                role = FactionRole.MEMBER
            )
            fPlayerRepository.save(updatedMember)
        }

        return factionRepository.delete(faction.id)
    }

    fun setRelation(faction: Faction, otherFaction: Faction, relation: FactionRelation): Boolean {
        return factionRepository.setRelation(faction.id, otherFaction.id, relation)
    }

    fun getRelation(faction: Faction, otherFaction: Faction): FactionRelation {
        return factionRepository.getRelation(faction.id, otherFaction.id) ?: FactionRelation.NEUTRAL
    }

    fun getFactionAt(location: Location): Faction? {
        val fLocation = FLocation(location.world.name, location.blockX, location.blockZ)
        return getFactionAt(fLocation)
    }

    fun getFactionAt(chunk: Chunk): Faction? {
        val fLocation = FLocation(chunk.world.name, chunk.x, chunk.z)
        return getFactionAt(fLocation)
    }

    fun getFactionAt(fLocation: FLocation): Faction? {
        return factionRepository.findByLocation(fLocation)
    }

    fun claimLand(faction: Faction, chunk: Chunk): Boolean {
        val location = FLocation(chunk.world.name, chunk.x, chunk.z)
        return claimLand(faction, location)
    }

    fun claimLand(faction: Faction, fLocation: FLocation): Boolean {
        val existingFaction = getFactionAt(fLocation)
        if (existingFaction != null) {
            return false
        }

        return factionRepository.addClaim(faction.id, fLocation)
    }

    fun unclaimLand(fLocation: FLocation): Boolean {
        return factionRepository.removeClaim(fLocation)
    }

    fun setFactionHome(faction: Faction, location: Location): Boolean {
        val updatedFaction = faction.copy(home = location)
        return factionRepository.save(updatedFaction)
    }

    fun getFactionsPlayers(faction: Faction): List<FPlayer> {
        return fPlayerRepository.findByFaction(faction.id)
    }

    fun setFactionDescription(faction: Faction, description: String): Boolean {
        val updatedFaction = faction.copy(description = description)
        return factionRepository.save(updatedFaction)
    }

    fun setFactionOpenStatus(faction: Faction, isOpen: Boolean): Boolean {
        val updatedFaction = faction.copy(isOpen = isOpen)
        return factionRepository.save(updatedFaction)
    }

    fun setFactionName(faction: Faction, name: String): Boolean {
        if (getFactionByName(name) != null) {
            return false
        }
        val updatedFaction = faction.copy(name = name)
        return factionRepository.save(updatedFaction)
    }

    fun setFactionLeader(faction: Faction, player: FPlayer): Boolean {
        val currentLeader = getFactionsPlayers(faction).find { it.role == FactionRole.LEADER }

        val updatedFaction = faction.copy(leaderId = player.uuid)
        if (!factionRepository.save(updatedFaction)) {
            return false
        }

        val updatedPlayer = player.copy(role = FactionRole.LEADER)
        if (!fPlayerRepository.save(updatedPlayer)) {
            factionRepository.save(faction)
            return false
        }

        if (currentLeader != null && currentLeader.uuid != player.uuid) {
            val demotedLeader = currentLeader.copy(role = FactionRole.OFFICER)
            fPlayerRepository.save(demotedLeader)
        }

        return true
    }

    private fun generateUniqueId(name: String): String {
        val baseId = name.lowercase().replace(Regex("[^a-z0-9]"), "")
        var id = baseId.ifEmpty { "faction" }

        var counter = 1
        while (factionRepository.findById(id) != null) {
            id = "$baseId$counter"
            counter++
        }

        return id
    }

}