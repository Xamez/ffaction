package fr.xamez.ffaction.listener

import fr.xamez.ffaction.api.FFactionAPI
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerListener(private val factionApi: FFactionAPI) : Listener {

    @EventHandler
    fun playerJoinEvent(event: PlayerJoinEvent) {
        val fPlayer = factionApi.createPlayerIfNotExists(event.player)
        val faction = factionApi.getPlayerFaction(fPlayer)
        event.joinMessage(
            Component.text(
                "Player ${event.player.name} joined the server. Faction: ${faction?.name ?: "None"}",
                NamedTextColor.YELLOW
            )
        )
    }

}