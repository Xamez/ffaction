package fr.xamez.ffaction.api.repository

import fr.xamez.ffaction.api.model.FLocation
import fr.xamez.ffaction.api.model.Faction
import fr.xamez.ffaction.api.model.FactionRelation
import org.bukkit.Location

interface FactionRepository {

    fun getFaction(id: String): Faction?
    fun getFactionByName(name: String): Faction?
    fun getFactionAt(location: FLocation): Faction?
    fun getAllFactions(): List<Faction>
    fun saveFaction(faction: Faction): Boolean
    fun deleteFaction(id: String): Boolean
    fun getClaimedChunks(factionId: String): Set<FLocation>
    fun addClaim(factionId: String, location: FLocation): Boolean
    fun removeClaim(location: FLocation): Boolean
    fun setHome(factionId: String, location: Location): Boolean
    fun setPower(factionId: String, power: Double): Boolean
    fun setMaxPower(factionId: String, maxPower: Double): Boolean
    fun setOpen(factionId: String, open: Boolean): Boolean
    fun setLeader(factionId: String, leaderId: String): Boolean
    fun setDescription(factionId: String, description: String): Boolean
    fun setRelation(factionId: String, otherFactionId: String, relation: FactionRelation): Boolean
    fun removeRelation(factionId: String, otherFactionId: String): Boolean
    fun getRelation(factionId: String, otherFactionId: String): FactionRelation?

}