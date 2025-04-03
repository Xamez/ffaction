package fr.xamez.ffaction.api.repository

import fr.xamez.ffaction.api.model.FPlayer
import java.util.*

interface FPlayerRepository : Repository<FPlayer, UUID> {

    fun findByName(name: String): FPlayer?

}