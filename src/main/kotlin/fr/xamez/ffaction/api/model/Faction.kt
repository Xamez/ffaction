package fr.xamez.ffaction.api.model

import org.bukkit.Location
import java.util.*

data class Faction(
    val id: String,
    val name: String,
    val description: String,
    val leaderId: UUID = UUID(0, 0),
    val home: Location? = null,
    val isOpen: Boolean = false,
    val power: Double = 0.0,
    val maxPower: Double = 10.0,
    val claims: Set<FLocation> = emptySet(),
    val relations: Map<String, FactionRelation> = emptyMap(),
    val members: Set<UUID> = mutableSetOf()
)