package fr.xamez.ffaction.api.service

import fr.xamez.ffaction.api.model.*
import fr.xamez.ffaction.api.repository.FPlayerRepository
import fr.xamez.ffaction.api.repository.FactionRepository
import org.bukkit.Location
import java.util.*

class FactionService(
    private val factionRepository: FactionRepository,
    private val fPlayerRepository: FPlayerRepository
) {

    // TODO: FIX ERRORS

    fun getPlayer(uuid: UUID): FPlayer? = fPlayerRepository.getPlayer(uuid)

    fun getPlayerByName(name: String): FPlayer? = fPlayerRepository.getPlayerByName(name)

    fun savePlayer(player: FPlayer): Boolean = fPlayerRepository.savePlayer(player)

    fun getPlayerFaction(player: FPlayer): Faction? {
        return player.factionId?.let { factionRepository.getFaction(it) }
    }

    fun setPlayerFaction(player: FPlayer, faction: Faction?): Boolean {
        val updatedPlayer = player.copy(
            factionId = faction?.id,
            role = if (faction == null) FactionRole.MEMBER else player.role
        )
        return fPlayerRepository.savePlayer(updatedPlayer)
    }

    fun setPlayerRole(player: FPlayer, role: FactionRole): Boolean {
        return fPlayerRepository.savePlayer(player.copy(role = role))
    }

    fun getFaction(id: String): Faction? = factionRepository.getFaction(id)

    fun getFactionByName(name: String): Faction? = factionRepository.getFactionByName(name)

    fun getAllFactions(): List<Faction> = factionRepository.getAllFactions()

    fun createFaction(name: String, player: FPlayer): Faction? {
        if (getFactionByName(name) != null) {
            return null
        }

        val id = generateUniqueId(name)

        val faction = Faction(
            id = id,
            name = name,
            leaderId = player.uuid
        )

        if (!factionRepository.saveFaction(faction)) {
            return null
        }

        val updatedPlayer = player.copy(
            factionId = id,
            role = FactionRole.LEADER
        )

        if (!fPlayerRepository.savePlayer(updatedPlayer)) {
            factionRepository.deleteFaction(id)
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
            fPlayerRepository.savePlayer(updatedMember)
        }

        return factionRepository.deleteFaction(faction.id)
    }

    fun setRelation(faction: Faction, otherFactionId: String, relation: FactionRelation): Boolean {
        val relations = faction.relations.toMutableMap()
        relations[otherFactionId] = relation

        val updatedFaction = faction.copy(relations = relations)
        return factionRepository.saveFaction(updatedFaction)
    }

    fun getRelation(faction: Faction, otherFactionId: String): FactionRelation {
        return faction.relations[otherFactionId] ?: FactionRelation.NEUTRAL
    }

    fun claimLand(faction: Faction, location: FLocation): Boolean {
        val existingFaction = factionRepository.getFactionAt(location)
        if (existingFaction != null) {
            return false
        }

        if (!factionRepository.addClaim(faction.id, location)) {
            return false
        }

        val updatedClaims = faction.claims + location
        val updatedFaction = faction.copy(claims = updatedClaims)
        return factionRepository.saveFaction(updatedFaction)
    }

    fun unclaimLand(location: FLocation): Boolean {
        return factionRepository.removeClaim(location)
    }

    fun setHome(faction: Faction, location: Location): Boolean {
        val updatedFaction = faction.copy(home = location)
        return factionRepository.saveFaction(updatedFaction)
    }

    fun getFactionsPlayers(faction: Faction): List<FPlayer> {
        return fPlayerRepository.getPlayersInFaction(faction.id)
    }

    private fun generateUniqueId(name: String): String {
        val baseId = name.lowercase().replace(Regex("[^a-z0-9]"), "")
        var id = baseId.ifEmpty { "faction" }

        var counter = 1
        while (factionRepository.getFaction(id) != null) {
            id = "$baseId$counter"
            counter++
        }

        return id
    }
}