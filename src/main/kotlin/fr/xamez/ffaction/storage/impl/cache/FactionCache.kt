package fr.xamez.ffaction.storage.impl.cache

import fr.xamez.ffaction.api.model.FLocation
import fr.xamez.ffaction.api.model.Faction
import java.util.concurrent.ConcurrentHashMap

class FactionCache {
    private val factionsById = ConcurrentHashMap<String, Faction>()
    private val factionsByName = ConcurrentHashMap<String, String>()
    private val factionsByLocation = ConcurrentHashMap<FLocation, String>()

    fun getFaction(id: String): Faction? = factionsById[id]

    fun getFactionByName(name: String): Faction? {
        val id = factionsByName[name.lowercase()] ?: return null
        return factionsById[id]
    }

    fun getFactionAt(location: FLocation): Faction? {
        val id = factionsByLocation[location] ?: return null
        return factionsById[id]
    }

    fun getAllFactions(): List<Faction> = factionsById.values.toList()

    fun cacheFaction(faction: Faction) {
        factionsById[faction.id] = faction
        factionsByName[faction.name.lowercase()] = faction.id
        faction.claims.forEach { claim ->
            factionsByLocation[claim] = faction.id
        }
    }

    fun updateFaction(faction: Faction) {
        val oldFaction = factionsById[faction.id]

        if (oldFaction != null && oldFaction.name != faction.name) {
            factionsByName.remove(oldFaction.name.lowercase())
        }

        oldFaction?.claims?.forEach { claim ->
            factionsByLocation.remove(claim)
        }

        cacheFaction(faction)
    }

    fun removeFaction(id: String) {
        val faction = factionsById[id] ?: return

        factionsById.remove(id)
        factionsByName.remove(faction.name.lowercase())
        faction.claims.forEach { claim ->
            factionsByLocation.remove(claim)
        }
    }

    fun addClaim(factionId: String, location: FLocation) {
        val faction = factionsById[factionId] ?: return
        val updatedFaction = faction.copy(claims = faction.claims + location)
        factionsById[factionId] = updatedFaction
        factionsByLocation[location] = factionId
    }

    fun removeClaim(location: FLocation) {
        val factionId = factionsByLocation[location] ?: return
        val faction = factionsById[factionId] ?: return
        val updatedFaction = faction.copy(claims = faction.claims - location)
        factionsById[factionId] = updatedFaction
        factionsByLocation.remove(location)
    }

    fun updateFactionField(factionId: String, updater: (Faction) -> Faction) {
        val faction = factionsById[factionId] ?: return
        val updatedFaction = updater(faction)
        updateFaction(updatedFaction)
    }

    fun clear() {
        factionsById.clear()
        factionsByName.clear()
        factionsByLocation.clear()
    }

}