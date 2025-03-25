package fr.xamez

import com.destroystokyo.paper.event.player.PlayerJumpEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

class FFaction : JavaPlugin(), Listener {

    override fun onEnable() {
        // Plugin startup logic
        println("Hello, World!")
        server.pluginManager.registerEvents(this, this)
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    fun onJump(event: PlayerJumpEvent) {
        event.player.sendMessage("Test")
    }

}
