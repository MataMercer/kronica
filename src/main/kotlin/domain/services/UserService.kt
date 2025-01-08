package org.matamercer.domain.services

import io.javalin.http.*
import org.matamercer.domain.models.*
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
    private val fileModelService: FileModelService
) {

    fun toDto(user: User, currentUser: CurrentUser? = null): UserDto {
        if (user.id == null) throw InternalServerErrorResponse("Cannot convert to Dto. User id is null")

        val followerCount = userRepository.findFollowerCount(user.id)
        var youFollowed: Boolean? = null
        var followingYou: Boolean? = null
        if (currentUser != null && currentUser.id != user.id) {
            youFollowed = isFollowing(currentUser.id, user.id)
            followingYou = isFollowing(user.id, currentUser.id)
        }

        return UserDto(
            id = user.id,
            name = user.name,
            createdAt = user.createdAt,
            role = user.role,
            followerCount = followerCount,
            youFollowed = youFollowed,
            followingYou = followingYou,
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

    fun update(currentUser: CurrentUser, updateUserForm: UpdateUserForm) {
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

    fun updateProfile(currentUser: CurrentUser, updateProfileForm: UpdateProfileForm) {
        val avatar = fileModelService.uploadFiles(listOf(updateProfileForm.avatar)).first()
        val profile = updateProfileForm.description?.let {
            Profile(
                id = currentUser.id,
                description = it,
                avatar = avatar
            )
        }
        if (profile == null) {
            throw BadRequestResponse()
        }
        userRepository.updateProfile(profile)
    }

    fun delete(currentUser: CurrentUser, id: Long) {
        authCheck(currentUser, id)
        userRepository.delete(id)
    }

    fun follow(currentUser: CurrentUser, id: Long) {
        if (currentUser.id == id) {
            throw BadRequestResponse()
        }
        val follow = userRepository.findFollow(currentUser.id, id)
        if (follow != null) {
            throw BadRequestResponse()
        }
        userRepository.follow(currentUser.id, id)
    }

    fun unfollow(currentUser: CurrentUser, id: Long) {
        if (currentUser.id == id) {
            throw BadRequestResponse()
        }
        val follow = userRepository.findFollow(currentUser.id, id) ?: throw BadRequestResponse()
        userRepository.unfollow(currentUser.id, id)
    }

    fun getFollowers(id: Long): List<Follow> {
        return userRepository.findFollowers(id)
    }

    fun getFollowings(id: Long): List<Follow> {
        return userRepository.findFollowings(id)
    }

    fun isFollowing(userIdA: Long, userIdB: Long): Boolean {
        return userRepository.findFollow(userIdA, userIdB) != null
    }

    private fun validateRegisterUserForm(registerUserForm: RegisterUserForm): Boolean {
        return (registerUserForm.name != null && registerUserForm.email != null && registerUserForm.password != null)
    }

    private fun authCheck(currentUser: CurrentUser, userId: Long) {
        val user = getById(userId)
        if (currentUser.id != user.id && currentUser.role.authLevel < UserRole.ADMIN.authLevel) {
            throw ForbiddenResponse()
        }
        if (currentUser.role.authLevel <= user.role.authLevel) {
            throw ForbiddenResponse()
        }
    }


}