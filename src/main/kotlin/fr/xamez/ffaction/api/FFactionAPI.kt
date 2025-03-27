package fr.xamez.ffaction.api

import fr.xamez.ffaction.api.model.FLocation
import fr.xamez.ffaction.api.model.FPlayer
import fr.xamez.ffaction.api.model.Faction
import fr.xamez.ffaction.api.model.FactionRelation
import fr.xamez.ffaction.api.model.FactionRole
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.UUID

interface FFactionAPI {

    // TODO: ADD MISSING METHODS FROM REPOSITORIES

    fun getPlayer(uuid: UUID): FPlayer?
    fun getPlayer(player: Player): FPlayer?
    fun getPlayerByName(name: String): FPlayer?
    fun getPlayerFaction(player: FPlayer): Faction?
    fun getPlayerFaction(player: Player): Faction?

    fun getFaction(id: String): Faction?
    fun getFactionByName(name: String): Faction?
    fun getAllFactions(): List<Faction>
    fun createFaction(name: String, player: Player): Faction?
    fun disbandFaction(faction: Faction): Boolean
    fun setFactionHome(faction: Faction, location: Location): Boolean

    fun getFactionAt(location: Location): Faction?
    fun getFactionAt(chunk: Chunk): Faction?
    fun getFactionAt(fLocation: FLocation): Faction?
    fun claimLand(faction: Faction, chunk: Chunk): Boolean
    fun unclaimLand(chunk: Chunk): Boolean

    fun setRelation(faction: Faction, otherFaction: Faction, relation: FactionRelation): Boolean
    fun getRelation(faction: Faction, otherFaction: Faction): FactionRelation

    fun getFactionsPlayers(faction: Faction): List<FPlayer>
    fun setPlayerRole(player: FPlayer, role: FactionRole): Boolean
    fun setPlayerFaction(player: FPlayer, faction: Faction?): Boolean

}