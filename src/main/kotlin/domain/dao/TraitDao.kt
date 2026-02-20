package org.matamercer.domain.dao

import org.matamercer.domain.jdbc.JdbcExecutor
import org.matamercer.domain.models.Trait

class TraitDao {

    private val jdbc = JdbcExecutor { rs ->
        Trait(
            id = rs.getLong("id"),
            name = rs.getString("name"),
            value = rs.getString("val")
        )

    }

    fun createTrait(name: String, value: String, characterId: Long): Long = jdbc.updateForId(
        """
            INSERT INTO traits
            (
                name,
                val,
                character_id
            )
            VALUES (?, ?, ?)
        """.trimIndent()){
        var i = 0
        setString(++i, name)
        setString(++i, value)
        setLong(++i, characterId)
    }

    fun deleteTrait(name: String, characterId: Long) = jdbc.update("""
       DELETE FROM traits
        WHERE character_id = ?
        AND name = ?
    """.trimIndent()) {
        var i = 0
        setLong(++i, characterId)
        setString(++i, name)
    }

    fun updateTrait( name: String, value: String, characterId: Long): Long = jdbc.updateForId(
        """
            UPDATE traits
            SET val = ?
            WHERE name = ?
            AND character_id = ?
        """.trimIndent()){
        var i = 0
        setString(++i, value)
        setString(++i, name)
        setLong(++i, characterId)
    }

    fun findTraitsByCharacter( characterId: Long): List<Trait> = jdbc.queryForObjectList(
        """
            SELECT 
                id,
                name,
                val
            FROM traits
            WHERE traits.character_id = ?
        """.trimIndent(),{
        setLong(1, characterId)
    })
}