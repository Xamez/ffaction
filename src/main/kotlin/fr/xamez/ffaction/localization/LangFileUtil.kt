package fr.xamez.ffaction.localization

import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.Reader
import java.util.jar.JarFile


class LangFileUtil {

    companion object {

        fun extractLangFilenames(): List<String> {
            val langFiles = mutableListOf<String>()
            val pluginJar = getJarFile()
            JarFile(pluginJar).use { jar ->
                val entries = jar.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    if (entry.name.startsWith("langs/") && entry.name.endsWith(".yml"))
                        langFiles.add(entry.name.substringAfterLast("/"))
                }
            }
            return langFiles
        }

        private fun getJarFile(): File? {
            return runCatching {
                File(Companion::class.java.getProtectionDomain().codeSource.location.toURI());
            }.getOrNull()
        }

        // NOTE: We 'recreate' the YamlConfiguration.loadConfiguration() method because we want to handle exceptions ourselves (especially to create a backup file)
        @Throws(FileNotFoundException::class, IOException::class, InvalidConfigurationException::class)
        fun loadConfiguration(file: File): YamlConfiguration {
            val config = YamlConfiguration()
            config.load(file)
            return config
        }

        @Throws(FileNotFoundException::class, IOException::class, InvalidConfigurationException::class)
        fun loadConfiguration(reader: Reader): YamlConfiguration {
            val config = YamlConfiguration()
            config.load(reader)
            return config
        }

    }

}
