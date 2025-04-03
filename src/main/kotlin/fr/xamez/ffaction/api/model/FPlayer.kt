package fr.xamez.ffaction.api.model

import org.bukkit.Bukkit
import java.util.*

data class FPlayer(
    val uuid: UUID,
    val name: String,
    val factionId: String? = null,
    val role: FactionRole = FactionRole.MEMBER,
    val power: Double = 10.0,
    val maxPower: Double = 20.0
) {

    fun isInFaction(): Boolean {
        return factionId != null
    }

    fun isLeader(): Boolean {
        return role == FactionRole.LEADER
    }

    fun hasPower(): Boolean {
        return power > 0
    }

    fun isOnline(): Boolean {
        return Bukkit.getPlayer(uuid) != null
    }

}