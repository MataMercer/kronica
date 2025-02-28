package org.matamercer.domain.repository

import org.matamercer.domain.dao.CharacterDao
import org.matamercer.domain.dao.FileModelDao
import org.matamercer.domain.dao.TransactionManager
import org.matamercer.domain.models.Character
import org.matamercer.domain.models.CharacterQuery
import java.sql.Connection
import javax.sql.DataSource

class CharacterRepository(
    private val characterDao: CharacterDao,
    private val fileModelDao: FileModelDao,
    private val transact: TransactionManager,
    private val dataSource: DataSource
) {
    fun findById(id: Long) = transact.wrap { conn ->
        val c = characterDao.findById(conn, id)
        return@wrap c?.let { aggregate(conn, it) }
    }

    fun findAll(query: CharacterQuery) = transact.wrap { conn ->
        return@wrap characterDao.findAll(conn, query).map {
            aggregate(conn, it)
        }
    }

    fun create(character: Character) = transact.wrap { conn ->
        val newCharacterId = characterDao.create(conn, character)
        val c = characterDao.findById(conn, newCharacterId)
        character.attachments.forEachIndexed { index, it ->
            val id = fileModelDao.create(conn, it)
            if (id != null) fileModelDao.joinCharacter(conn, id, newCharacterId, index)

        }
        character.profilePictures.forEachIndexed { index, it ->
            val id = fileModelDao.create(conn, it)
            if (id != null) fileModelDao.joinCharacterProfile(conn, id, newCharacterId, index, "test caption")
        }
        return@wrap c?.let { aggregate(conn, it) }
    }

    fun deleteById(id: Long) = dataSource.connection.use { conn ->
        characterDao.deleteById(conn, id)
    }

    private fun aggregate(conn: Connection, c: Character): org.matamercer.domain.models.Character {
        val attachments = c.id?.let { fileModelDao.findCharacterAttachments(conn, it) }
        if (attachments != null) c.attachments = attachments
        val profilePictures = c.id?.let { fileModelDao.findCharacterProfilePictures(conn, it) }
        if (profilePictures != null) c.profilePictures = profilePictures
        return c
    }
}