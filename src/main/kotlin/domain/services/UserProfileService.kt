package org.matamercer.domain.services

import org.matamercer.domain.models.CurrentUser
import org.matamercer.domain.models.Profile
import org.matamercer.domain.repository.UserProfileRepository
import org.matamercer.domain.services.upload.image.ImagePresetSize
import org.matamercer.web.FileUploadForm
import org.matamercer.web.UpdateProfileForm

class UserProfileService(
    private val userProfileRepository: UserProfileRepository,
    private val fileModelService: FileModelService
) {
    private val imagePresetSizes = setOf(
        ImagePresetSize.SMALL,
        ImagePresetSize.TINY
    )

    fun updateProfile(currentUser: CurrentUser, form: UpdateProfileForm) {
        val originalProfile = userProfileRepository.findProfileByUserId(currentUser.id)
        val picture = form.picture?.let {
            fileModelService.uploadImages(
                forms = listOf(FileUploadForm(it)),
                imagePresetSizes,
                currentUser = currentUser
            ).first()
        }
        if (picture != null || form.deletePicture == true) {
            originalProfile?.picture?.let { originalPicture ->
                fileModelService.deleteFiles(listOf(originalPicture))
            }
        }
        Profile(
            id = originalProfile?.id,
            description = form.description ?: "",
            picture =
            if (form.deletePicture == true) {
                null
            } else {
                picture ?: originalProfile?.picture
            },
        ).also {
            userProfileRepository.updateProfile(it)
        }
    }
}