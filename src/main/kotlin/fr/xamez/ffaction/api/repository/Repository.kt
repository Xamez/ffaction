package fr.xamez.ffaction.api.repository

interface Repository<T, ID> {
    fun findById(id: ID): T?
    fun findAll(): List<T>
    fun save(entity: T): Boolean
    fun delete(id: ID): Boolean
    fun <R> query(queryType: String, params: Map<String, Any?>, resultMapper: (Any?) -> R): R?
}