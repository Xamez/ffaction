package fr.xamez.ffaction.storage

import fr.xamez.ffaction.api.repository.FactionRepository
import fr.xamez.ffaction.api.repository.FPlayerRepository

interface StorageProvider {
    fun initialize(): Boolean
    fun shutdown()
    fun isConnected(): Boolean

    fun getFPlayerRepository(): FPlayerRepository
    fun getFactionRepository(): FactionRepository
}