package org.matamercer.domain.services

import io.javalin.http.BadRequestResponse
import org.matamercer.domain.models.CurrentUser
import org.matamercer.domain.models.Profile
import org.matamercer.domain.repository.UserRepository
import org.matamercer.domain.services.upload.image.ImagePresetSize
import org.matamercer.web.FileUploadForm
import org.matamercer.web.UpdateProfileForm

class UserProfileService(
    private val userService: UserService,
    private val userRepository: UserRepository,
    private val fileModelService: FileModelService
) {

    fun updateProfile(currentUser: CurrentUser, updateProfileForm: UpdateProfileForm) {
        val foundUser = userService.getById(currentUser.id)

        val avatar = fileModelService.uploadImages(forms = listOf(FileUploadForm(updateProfileForm.avatar)),
            setOf(ImagePresetSize.SMALL, ImagePresetSize.TINY)
            , currentUser = currentUser).first()

        //TODO: DELETE OLD AVATAR IF EXISTS

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
}