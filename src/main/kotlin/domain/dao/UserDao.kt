package org.matamercer.domain.dao

import org.matamercer.domain.models.User

interface UserDao {
    fun findAll():List<User>
    fun findByEmail(email:String): User?
    fun findById(id:Long): User?
    fun create(user: User): Long?
    fun update(id: Long): User?
    fun delete(id: Long)
}