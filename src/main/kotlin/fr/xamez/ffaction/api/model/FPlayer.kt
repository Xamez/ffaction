package fr.xamez.ffaction.api.model

import java.util.*

data class FPlayer(
    val uuid: UUID,
    val name: String,
    val factionId: String? = null,
    val role: FactionRole = FactionRole.MEMBER,
    val power: Double = 10.0,
    val maxPower: Double = 20.0
)