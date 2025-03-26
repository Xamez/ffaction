package fr.xamez.ffaction.localization

import java.io.File
import java.util.jar.JarFile


class LangFileExtractor {

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
    }
}
