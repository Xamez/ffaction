package fr.xamez.ffaction.storage

data class DatabaseCredentials(
    val host: String = "localhost",
    val port: Int = 3306,
    val database: String = "ffaction",
    val username: String = "root",
    val password: String = "",
    val tablePrefix: String = "ffaction_"
)