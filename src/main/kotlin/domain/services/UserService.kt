package org.matamercer.domain.services

import io.javalin.http.*
import org.matamercer.domain.models.FileModel
import org.matamercer.domain.models.Profile
import org.matamercer.domain.models.User
import org.matamercer.domain.models.UserDto
import org.matamercer.domain.repository.UserRepository
import org.matamercer.security.UserRole
import org.matamercer.security.hashPassword
import org.matamercer.security.verifyPassword
import org.matamercer.web.LoginRequestForm
import org.matamercer.web.RegisterUserForm
import org.matamercer.web.UpdateProfileForm
import org.matamercer.web.UpdateUserForm

class UserService(
    private val userRepository: UserRepository,
    private val fileModelService: FileModelService) {

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
        return userRepository.findByEmail(email)
    }

    fun getById(id: Long?): User {
        if (id == null) throw BadRequestResponse()
        return userRepository.findById(id) ?: throw NotFoundResponse()
    }

    fun authenticateUser(loginRequestForm: LoginRequestForm): User {
        val foundUser = getByEmail(loginRequestForm.email) ?: throw NotFoundResponse()
        if (loginRequestForm.password.isNullOrBlank()) {
            throw BadRequestResponse()
        }
        if (foundUser.hashedPassword != null && !verifyPassword(loginRequestForm.password, foundUser.hashedPassword)) {
            throw UnauthorizedResponse()
        }
        return foundUser
    }

    fun registerUser(registerUserForm: RegisterUserForm, userRole: UserRole = UserRole.AUTHENTICATED_USER): User {
        if (!validateRegisterUserForm(registerUserForm)) {
            throw BadRequestResponse()
        }
         val id = userRepository.create(
            User(
                name = registerUserForm.name!!,
                email = registerUserForm.email,
                hashedPassword = hashPassword(registerUserForm.password!!),
                role = userRole
            )
        )
        return getById(id)
    }

    fun update(currentUser: User, updateUserForm: UpdateUserForm){
        val user = User(
            id = updateUserForm.id.toLong(),
            name = updateUserForm.name!!,
            email = updateUserForm.email,
            hashedPassword = updateUserForm.hashedPassword,
            role = UserRole.valueOf(updateUserForm.role)
        )
        authCheck(currentUser, user.id!!)
        userRepository.update(user)
    }

    fun updateProfile(currentUser: User, updateProfileForm: UpdateProfileForm){
        val profile = updateProfileForm.description?.let {
            Profile(
                id = currentUser.id,
                description = it,
                avatar = FileModel(
                    name = updateProfileForm.avatar.filename(),
                    author = currentUser
                )
            )
        }
        if (profile == null) {
            throw BadRequestResponse()
        }
        userRepository.updateProfile(profile)

        try {
            fileModelService.uploadFiles(listOf(profile.avatar), updateProfileForm.avatar)
        } catch (e: StorageException) {
            rollbackProfileUpdate(profile.id)
        }
    }

    private fun rollbackProfileUpdate(id: Long) {
//        userRepository.deleteProfile(id)
        //TODO wtf to do lol
        throw InternalServerErrorResponse()
    }

    fun delete(currentUser: User, id: Long){
        authCheck(currentUser, id)
        userRepository.delete(id)
    }

    private fun validateRegisterUserForm(registerUserForm: RegisterUserForm): Boolean {
        return (registerUserForm.name != null && registerUserForm.email != null && registerUserForm.password != null)
    }
    private fun authCheck(currentUser: User, userId: Long){
        val user = getById(userId)
        if (currentUser.id != user.id && currentUser.role.authLevel < UserRole.ADMIN.authLevel) {
            throw ForbiddenResponse()
        }
        if (currentUser.role.authLevel <= user.role.authLevel) {
            throw ForbiddenResponse()
        }
    }


}