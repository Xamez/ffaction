package fr.xamez.ffaction.storage

import fr.xamez.ffaction.api.repository.FPlayerRepository
import fr.xamez.ffaction.api.repository.FactionRepository

interface StorageProvider {

    val storageDirectoryName: String
        get() = "storage"

    fun initialize(): Boolean
    fun shutdown()
    fun isConnected(): Boolean

    fun getFPlayerRepository(): FPlayerRepository
    fun getFactionRepository(): FactionRepository

    fun reload(): Boolean {
        shutdown()
        return initialize()
    }

}