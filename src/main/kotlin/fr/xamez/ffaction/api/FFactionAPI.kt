package fr.xamez.ffaction.api

import fr.xamez.ffaction.api.model.*
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.*

interface FFactionAPI {

    fun getPlayer(uuid: UUID): FPlayer?
    fun getPlayer(player: Player): FPlayer?
    fun getPlayerByName(name: String): FPlayer?
    fun createPlayerIfNotExists(player: Player): FPlayer
    fun savePlayer(player: FPlayer): Boolean
    fun getPlayerFaction(player: FPlayer): Faction?
    fun getPlayerFaction(player: Player): Faction?

    fun getFaction(id: String): Faction?
    fun getFactionByName(name: String): Faction?
    fun getFactionMembers(faction: Faction): List<FPlayer>
    fun addFactionMember(faction: Faction, player: FPlayer): Boolean
    fun removeFactionMember(faction: Faction, player: FPlayer): Boolean
    fun getAllFactions(): List<Faction>
    fun createFaction(name: String, player: Player): Faction?
    fun disbandFaction(faction: Faction): Boolean
    fun setFactionHome(faction: Faction, location: Location): Boolean
    fun setFactionDescription(faction: Faction, description: String): Boolean
    fun setFactionOpenStatus(faction: Faction, isOpen: Boolean): Boolean
    fun setFactionName(faction: Faction, name: String): Boolean
    fun setFactionLeader(faction: Faction, player: FPlayer): Boolean

    fun getFactionAt(location: Location): Faction?
    fun getFactionAt(chunk: Chunk): Faction?
    fun getFactionAt(fLocation: FLocation): Faction?
    fun claimLand(faction: Faction, chunk: Chunk): Boolean
    fun claimLand(faction: Faction, fLocation: FLocation): Boolean
    fun unclaimLand(chunk: Chunk): Boolean
    fun unclaimLand(fLocation: FLocation): Boolean
    fun getClaimsFor(faction: Faction): Set<FLocation>

    fun setRelation(faction: Faction, otherFaction: Faction, relation: FactionRelation): Boolean
    fun getRelation(faction: Faction, otherFaction: Faction): FactionRelation

    fun getFactionsPlayers(faction: Faction): List<FPlayer>
    fun setPlayerRole(player: FPlayer, role: FactionRole): Boolean
    fun setPlayerFaction(player: FPlayer, faction: Faction?): Boolean

}