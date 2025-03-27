package fr.xamez.ffaction.storage.impl.sql

import fr.xamez.ffaction.api.model.FLocation
import fr.xamez.ffaction.api.model.Faction
import fr.xamez.ffaction.api.model.FactionRelation
import fr.xamez.ffaction.api.repository.FactionRepository
import fr.xamez.ffaction.storage.impl.cache.FactionCache
import org.bukkit.Location

class CachedSQLFactionRepository(private val repository: FactionRepository) : FactionRepository {

    private val cache = FactionCache()

    override fun getFaction(id: String): Faction? {
        return cache.getFaction(id) ?: repository.getFaction(id)?.also { cache.cacheFaction(it) }
    }

    override fun getFactionByName(name: String): Faction? {
        return cache.getFactionByName(name) ?: repository.getFactionByName(name)?.also { cache.cacheFaction(it) }
    }

    override fun getFactionAt(location: FLocation): Faction? {
        return cache.getFactionAt(location) ?: repository.getFactionAt(location)?.also { cache.cacheFaction(it) }
    }

    override fun getAllFactions(): List<Faction> {
        val cachedFactions = cache.getAllFactions()
        if (cachedFactions.isNotEmpty()) {
            return cachedFactions
        }

        val allFactions = repository.getAllFactions()
        allFactions.forEach { cache.cacheFaction(it) }
        return allFactions
    }

    override fun saveFaction(faction: Faction): Boolean {
        val result = repository.saveFaction(faction)
        if (result) {
            cache.updateFaction(faction)
        }
        return result
    }

    override fun deleteFaction(id: String): Boolean {
        val result = repository.deleteFaction(id)
        if (result) {
            cache.removeFaction(id)
        }
        return result
    }

    override fun getClaimedChunks(factionId: String): Set<FLocation> {
        return cache.getFaction(factionId)?.claims ?: repository.getClaimedChunks(factionId)
    }

    override fun addClaim(factionId: String, location: FLocation): Boolean {
        val result = repository.addClaim(factionId, location)
        if (result) {
            cache.addClaim(factionId, location)
        }
        return result
    }

    override fun removeClaim(location: FLocation): Boolean {
        val result = repository.removeClaim(location)
        if (result) {
            cache.removeClaim(location)
        }
        return result
    }

    override fun setHome(factionId: String, location: Location): Boolean {
        val result = repository.setHome(factionId, location)
        if (result) {
            cache.updateFactionField(factionId) { it.copy(home = location) }
        }
        return result
    }

    override fun setPower(factionId: String, power: Double): Boolean {
        val result = repository.setPower(factionId, power)
        if (result) {
            cache.updateFactionField(factionId) { it.copy(power = power) }
        }
        return result
    }

    override fun setMaxPower(factionId: String, maxPower: Double): Boolean {
        val result = repository.setMaxPower(factionId, maxPower)
        if (result) {
            cache.updateFactionField(factionId) { it.copy(maxPower = maxPower) }
        }
        return result
    }

    override fun setOpen(factionId: String, open: Boolean): Boolean {
        val result = repository.setOpen(factionId, open)
        if (result) {
            cache.updateFactionField(factionId) { it.copy(isOpen = open) }
        }
        return result
    }

    override fun setLeader(factionId: String, leaderId: String): Boolean {
        val result = repository.setLeader(factionId, leaderId)
        if (result) {
            cache.updateFactionField(factionId) { it.copy(leaderId = java.util.UUID.fromString(leaderId)) }
        }
        return result
    }

    override fun setDescription(factionId: String, description: String): Boolean {
        val result = repository.setDescription(factionId, description)
        if (result) {
            cache.updateFactionField(factionId) { it.copy(description = description) }
        }
        return result
    }

    override fun setRelation(factionId: String, otherFactionId: String, relation: FactionRelation): Boolean {
        val result = repository.setRelation(factionId, otherFactionId, relation)
        if (result) {
            cache.updateFactionField(factionId) {
                it.copy(relations = it.relations + (otherFactionId to relation))
            }
            cache.updateFactionField(otherFactionId) {
                it.copy(relations = it.relations + (factionId to relation))
            }
        }
        return result
    }

    override fun removeRelation(factionId: String, otherFactionId: String): Boolean {
        val result = repository.removeRelation(factionId, otherFactionId)
        if (result) {
            cache.updateFactionField(factionId) {
                it.copy(relations = it.relations - otherFactionId)
            }
            cache.updateFactionField(otherFactionId) {
                it.copy(relations = it.relations - factionId)
            }
        }
        return result
    }

    override fun getRelation(factionId: String, otherFactionId: String): FactionRelation? {
        val faction = cache.getFaction(factionId)
        if (faction != null) {
            val relation = faction.relations[otherFactionId]
            if (relation != null) {
                return relation
            }
        }
        return repository.getRelation(factionId, otherFactionId)
    }
}