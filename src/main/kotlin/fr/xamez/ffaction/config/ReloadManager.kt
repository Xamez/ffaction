package fr.xamez.ffaction.config

import org.bukkit.plugin.Plugin

class ReloadManager(private val plugin: Plugin) {

    private val reloadables = mutableMapOf<String, Reloadable>()

    fun register(name: String, reloadable: Reloadable) {
        reloadables[name] = reloadable
    }

    fun unregister(name: String) {
        reloadables.remove(name)
    }

    fun reloadAll(): Map<String, Boolean> {
        val results = mutableMapOf<String, Boolean>()
        reloadables.forEach { (name, reloadable) ->
            try {
                results[name] = reloadable.reload()
                if (results[name] == true) {
                    plugin.logger.info("Reloaded $name successfully.")
                } else {
                    plugin.logger.warning("Failed to reload $name.")
                }
            } catch (e: Exception) {
                plugin.logger.severe("Error reloading $name: ${e.message}")
                e.printStackTrace()
                results[name] = false
            }
        }
        return results
    }

    fun reload(name: String): Boolean {
        val result = reloadables[name]?.reload()
        if (result == true) {
            plugin.logger.info("Reloaded $name successfully.")
        } else {
            plugin.logger.warning("Failed to reload $name.")
        }
        return result ?: false
    }

}