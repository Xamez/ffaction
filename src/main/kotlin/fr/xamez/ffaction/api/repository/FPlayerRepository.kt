package fr.xamez.ffaction.api.repository

import fr.xamez.ffaction.api.model.FPlayer
import java.util.*

interface FPlayerRepository {

    fun getPlayer(uuid: UUID): FPlayer?
    fun getPlayerByName(name: String): FPlayer?
    fun getAllPlayers(): List<FPlayer>
    fun getPlayersInFaction(factionId: String): List<FPlayer>
    fun savePlayer(player: FPlayer): Boolean
    fun deletePlayer(uuid: UUID): Boolean

}
