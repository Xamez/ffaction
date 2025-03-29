package fr.xamez.ffaction.storage.impl

import fr.xamez.ffaction.api.repository.Repository
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Logger

abstract class AbstractRepository<T, ID>(
    protected val logger: Logger,
    private val useCache: Boolean = true
) : Repository<T, ID> {

    private val cache = ConcurrentHashMap<ID, T>()
    private val listCache = ConcurrentHashMap<String, List<T>>()

    override fun findById(id: ID): T? {
        if (useCache) {
            val cached = cache[id]
            if (cached != null) return cached
        }

        val result = fetchById(id)
        if (result != null && useCache) {
            cache[id] = result
        }
        return result
    }

    override fun findAll(): List<T> {
        if (useCache) {
            val cached = listCache["all"]
            if (cached != null) return cached
        }

        val result = fetchAll()
        if (useCache) {
            listCache["all"] = result
            result.forEach { entity ->
                cache[getEntityId(entity)] = entity
            }
        }
        return result
    }

    override fun save(entity: T): Boolean {
        val id = getEntityId(entity)
        val result = persist(entity)
        if (result && useCache) {
            cache[id] = entity
            listCache.clear()
        }
        return result
    }

    override fun delete(id: ID): Boolean {
        val result = removeById(id)
        if (result && useCache) {
            cache.remove(id)
            listCache.clear()
        }
        return result
    }

    override fun <R> query(queryType: String, params: Map<String, Any?>, resultMapper: (Any?) -> R): R? {
        val cacheKey = if (useCache) "$queryType:${params.hashCode()}" else null

        if (useCache && cacheKey != null) {
            val cached = listCache[cacheKey]
            if (cached != null) return resultMapper(cached)
        }

        val result = executeQuery(queryType, params)

        if (useCache && cacheKey != null && result != null) {
            if (result is List<*>) {
                listCache[cacheKey] = result as List<T>
            }

            if (result is Map<*, *> && result["entity"] != null) {
                val entity = result["entity"] as? T
                if (entity != null) {
                    cache[getEntityId(entity)] = entity
                }
            }
        }

        return resultMapper(result)
    }

    fun clearCache() {
        cache.clear()
        listCache.clear()
    }

    protected abstract fun fetchById(id: ID): T?
    protected abstract fun fetchAll(): List<T>
    protected abstract fun persist(entity: T): Boolean
    protected abstract fun removeById(id: ID): Boolean
    protected abstract fun getEntityId(entity: T): ID
    protected abstract fun executeQuery(queryType: String, params: Map<String, Any?>): Any?
}