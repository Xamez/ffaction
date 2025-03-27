package fr.xamez.ffaction.storage.impl.sql

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.util.logging.Logger

class SQLExecutor(private val logger: Logger, private val connection: Connection) {

    fun <T> query(sql: String, mapper: (ResultSet) -> T, vararg params: Any?): T? {
        return try {
            prepareStatement(sql, *params).use { stmt ->
                val rs = stmt.executeQuery()
                if (rs.next()) {
                    mapper(rs)
                } else {
                    null
                }
            }
        } catch (e: SQLException) {
            logger.warning("Error while executing query: $sql")
            null
        }
    }

    fun <T> queryList(sql: String, mapper: (ResultSet) -> T, vararg params: Any?): List<T> {
        val result = mutableListOf<T>()
        try {
            prepareStatement(sql, *params).use { stmt ->
                val rs = stmt.executeQuery()
                while (rs.next()) {
                    result.add(mapper(rs))
                }
            }
        } catch (e: SQLException) {
            logger.warning("Error while executing query list: $sql")
            listOf<T>()
        }
        return result
    }

    fun update(sql: String, vararg params: Any?): Int {
        return try {
            prepareStatement(sql, *params).use { stmt ->
                stmt.executeUpdate()
            }
        } catch (e: SQLException) {
            logger.warning("Error while executing update query: $sql")
            0
        }
    }

    fun execute(sql: String) {
        try {
            connection.createStatement().use { stmt ->
                stmt.execute(sql)
            }
        } catch (e: SQLException) {
            logger.warning("Error while executing sql query: $sql")
        }
    }

    fun batchUpdate(sql: String, batchParams: List<Array<Any?>>): IntArray {
        return try {
            connection.prepareStatement(sql).use { stmt ->
                for (params in batchParams) {
                    setParameters(stmt, *params)
                    stmt.addBatch()
                }
                stmt.executeBatch()
            }
        } catch (e: SQLException) {
            logger.warning("Error while executing batch: $sql")
            IntArray(0)
        }
    }

    private fun prepareStatement(sql: String, vararg params: Any?): PreparedStatement {
        val stmt = connection.prepareStatement(sql)
        setParameters(stmt, *params)
        return stmt
    }

    private fun setParameters(stmt: PreparedStatement, vararg params: Any?) {
        for (i in params.indices) {
            when (val param = params[i]) {
                null -> stmt.setNull(i + 1, java.sql.Types.NULL)
                is String -> stmt.setString(i + 1, param)
                is Int -> stmt.setInt(i + 1, param)
                is Long -> stmt.setLong(i + 1, param)
                is Double -> stmt.setDouble(i + 1, param)
                is Boolean -> stmt.setBoolean(i + 1, param)
                else -> stmt.setObject(i + 1, param)
            }
        }
    }
}