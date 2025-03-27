package fr.xamez.ffaction.util

import fr.xamez.ffaction.api.model.FLocation
import org.bukkit.Bukkit
import org.bukkit.Location

class SerializationUtil {

    companion object {

        fun serializeFLocation(fLocation: FLocation): String {
            return fLocation.world + ";" + fLocation.chunkX + ";" + fLocation.chunkZ
        }

        fun deserializeFLocation(serialized: String): FLocation {
            val parts = serialized.split(";")
            return FLocation(parts[0], parts[1].toInt(), parts[2].toInt())
        }

        fun serializeLocation(location: Location): String {
            return location.world.name + ";" + location.x + ";" + location.y + ";" + location.z + ";" + location.yaw + ";" + location.pitch
        }

        fun deserializeLocation(serialized: String): Location {
            val parts = serialized.split(";")
            return Location(Bukkit.getWorld(parts[0]), parts[1].toDouble(), parts[2].toDouble(), parts[3].toDouble(), parts[4].toFloat(), parts[5].toFloat())
        }

    }

}