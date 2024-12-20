package org.matamercer.domain.repository

import org.matamercer.domain.dao.CharacterDao
import org.matamercer.domain.dao.FileModelDao
import org.matamercer.domain.dao.TransactionManager
import org.matamercer.domain.models.Character
import org.matamercer.domain.models.CharacterQuery
import org.matamercer.domain.models.FileModel
import java.sql.Connection
import javax.sql.DataSource

class CharacterRepository(
    private val characterDao: CharacterDao,
    private val fileModelDao: FileModelDao,
    private val transactionManager: TransactionManager,
    private val dataSource: DataSource
) {
    fun findById(id: Long): Character? {
        var c: Character? = null
        transactionManager.wrap { conn ->
            c = characterDao.findById(conn, id)
            c?.let { aggregate(conn, it) }

        }
        return c
    }

    fun findAll(query: CharacterQuery): List<Character>{
       var characters = emptyList<Character>()
        transactionManager.wrap { conn ->
            characters = characterDao.findAll(conn, query).map{
                aggregate(conn, it)
            }
        }
        return characters
    }

    fun create(character: Character): Character?{
      var c: Character? = null
        transactionManager.wrap { conn ->
            val newCharacterId = characterDao.create(conn, character)
            c = characterDao.findById(conn, newCharacterId)

            val fileModels = character.attachments.map { FileModel(
                name = it.name,
                author = it.author,
            ) }

            fileModels.forEach{
                fileModelDao.create(conn, it)
            }

            fileModels.map { it.id }.forEach{
                if (it != null) {
                    fileModelDao.joinCharacter(conn, it, newCharacterId )
                }
            }
            c = c?.let { aggregate(conn, it) }
        }
        return c
    }

    fun deleteById(id: Long){
        dataSource.connection.use { conn ->
           characterDao.deleteById(conn, id )
        }
    }

    private fun aggregate(conn:Connection, c: Character): org.matamercer.domain.models.Character
    {
        val files = c.id?.let { fileModelDao.findByOwningCharacterId(conn, it) }
        if (files != null) {
            c.attachments = files
        }
        return c
    }

}