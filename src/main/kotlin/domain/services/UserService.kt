package org.matamercer.domain.services

import io.javalin.http.BadRequestResponse
import io.javalin.http.NotFoundResponse
import io.javalin.http.UnauthorizedResponse
import org.matamercer.domain.dao.UserDao
import org.matamercer.domain.models.User
import org.matamercer.domain.models.UserDto
import org.matamercer.security.UserRole
import org.matamercer.security.hashPassword
import org.matamercer.security.verifyPassword
import org.matamercer.web.LoginRequestForm
import org.matamercer.web.RegisterUserForm
import javax.sql.DataSource

class UserService(val userDao: UserDao, val dataSource: DataSource) {

    fun toDto(user: User): UserDto {
        return UserDto(
            id = user.id,
            name = user.name,
            createdAt = user.createdAt,
            role = user.role
        )
    }

    fun getByEmail(email: String?): User? {
        if (email.isNullOrBlank()) throw BadRequestResponse()
        dataSource.connection.use { conn ->
            val user = userDao.findByEmail(conn, email)
            return user
        }
    }

    fun getById(id: Long?): User {
        if (id == null) throw BadRequestResponse()
        dataSource.connection.use { conn ->
            val user = userDao.findById(conn, id)
            user ?: throw NotFoundResponse()
            return user
        }
    }
    fun authenticateUser(loginRequestForm: LoginRequestForm): User{
        val foundUser = getByEmail(loginRequestForm.email) ?: throw NotFoundResponse()
        if (loginRequestForm.password.isNullOrBlank()){
            throw BadRequestResponse()
        }
        if (foundUser.hashedPassword != null && !verifyPassword(loginRequestForm.password, foundUser.hashedPassword)){
            throw UnauthorizedResponse()
        }
        return foundUser
    }

    fun registerUser(registerUserForm: RegisterUserForm, userRole: UserRole = UserRole.AUTHENTICATED_USER): User{
        if (registerUserForm.name != null && registerUserForm.email != null && registerUserForm.password != null){
            dataSource.connection.use { conn ->
                val newUserId = userDao.create(conn,
                    User(name = registerUserForm.name,
                        email = registerUserForm.email,
                        hashedPassword = hashPassword(registerUserForm.password),
                        role = userRole))
                if (newUserId != null) {
                    val newUser = userDao.findById(conn, newUserId)
                    if (newUser != null){
                        return newUser
                    }
                    else{
                        throw NotFoundResponse()
                    }
                }else{
                    throw BadRequestResponse()
                }
            }
        }else{
            throw BadRequestResponse()
        }

    }
}