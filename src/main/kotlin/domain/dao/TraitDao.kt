package org.matamercer.domain.dao

import org.matamercer.domain.models.Trait
import java.sql.Connection

class TraitDao {

    private val mapper = RowMapper { rs ->
        Trait(
            id = rs.getLong("id"),
            name = rs.getString("name"),
            value = rs.getString("val")
        )

    }

    fun createTrait(conn: Connection, name: String, value: String, characterId: Long): Long = mapper.updateForId(
        """
            INSERT INTO traits
            (
                name,
                val,
                character_id
            )
            VALUES (?, ?, ?)
        """.trimIndent(), conn){
        var i = 0
        it.setString(++i, name)
        it.setString(++i, value)
        it.setLong(++i, characterId)
    }

    fun deleteTrait(conn: Connection, name: String, characterId: Long) = mapper.update("""
       DELETE FROM traits
        WHERE character_id = ?
        AND name = ?
    """.trimIndent(), conn) {
        var i = 0
        it.setLong(1, characterId)
        it.setString(2, name)
    }

    fun findTraitsByCharacter(conn: Connection, characterId: Long): List<Trait> = mapper.queryForObjectList(
        """
            SELECT 
                id,
                name,
                val
            FROM traits
            WHERE traits.character_id = ?
        """.trimIndent(), conn){
        it.setLong(1, characterId)
    }
}