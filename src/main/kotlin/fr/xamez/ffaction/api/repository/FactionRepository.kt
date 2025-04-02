package fr.xamez.ffaction.api.repository

import fr.xamez.ffaction.api.model.FLocation
import fr.xamez.ffaction.api.model.Faction
import fr.xamez.ffaction.api.model.FactionRelation

interface FactionRepository : Repository<Faction, String> {

    fun findByName(name: String): Faction?
    fun findByLocation(location: FLocation): Faction?
    fun getClaimsFor(factionId: String): Set<FLocation>
    fun addClaim(factionId: String, location: FLocation): Boolean
    fun removeClaim(location: FLocation): Boolean
    fun setRelation(factionId: String, otherFactionId: String, relation: FactionRelation): Boolean
    fun getRelation(factionId: String, otherFactionId: String): FactionRelation?

}