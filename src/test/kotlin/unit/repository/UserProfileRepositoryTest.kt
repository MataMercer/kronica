package unit.repository

import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.matamercer.domain.jdbc.txn
import org.matamercer.domain.models.FileModel
import org.matamercer.domain.models.Profile
import org.matamercer.domain.repository.FileModelRepository
import org.matamercer.domain.repository.UserProfileRepository

@ExtendWith(MockKExtension::class)
class UserProfileRepositoryTest {
    @MockK(relaxUnitFun = true)
    private lateinit var userProfileRepository: UserProfileRepository


    @BeforeEach
    fun beforeEach() {
        clearAllMocks()
        mockkStatic("org.matamercer.domain.dao.TransactionManagerKt")
        every { txn<Any>(any()) } answers { firstArg<() -> Any>().invoke() }
    }

    @Test
    fun `test update user profile repository with no picture`() {

        val fileModelRepo = mockk<FileModelRepository>(relaxed = true)
        userProfileRepository = UserProfileRepository(
            fileRepo = fileModelRepo,
            db = mockk(relaxed = true)
        )
        val profile = Profile(
            id = 1L,
            description = "Test User",
            picture = null
        )
        every { userProfileRepository.findById(any()) } returns Profile(
            id = 1L,
            description = "Test User",
            picture = null
        )
        userProfileRepository.updateProfile(profile)
        verify {
            userProfileRepository.findById(profile.id!!)
            userProfileRepository.updateProfile(profile)

        }
        verify(exactly = 0) {
            fileModelRepo.deleteById(any())
            fileModelRepo.deleteJoinUserProfile(any(), any())
            fileModelRepo.create(any())
            fileModelRepo.joinUserProfile(any(), any())
        }
        confirmVerified(userProfileRepository, fileModelRepo)

    }

    @Test
    fun `test update user profile repository with picture`() {

        val fileRepo = mockk<FileModelRepository>(relaxed = true)
        userProfileRepository = UserProfileRepository(
            fileRepo = fileRepo,
            db = mockk(relaxed = true)
        )
        every { userProfileRepository.findById(any()) } returns Profile(
            id = 1L,
            description = "Test User",
            picture = null
        )
        val profile = Profile(
            id = 1L,
            description = "Test User",
            picture = mockk(relaxed = true)
        )
        userProfileRepository.updateProfile(profile)
        verify {
            userProfileRepository.findById(profile.id!!)
            userProfileRepository.updateProfile(profile)
            fileRepo.create(any())
            fileRepo.joinUserProfile(any(), any())
        }
        confirmVerified(userProfileRepository, fileRepo)
    }

    @Test
    fun `test update user profile repository with picture and delete existing picture`() {
        val existingPicture = FileModel(
            id = 2L,
            name = "existing_picture.jpg",
            sizeBytes = 12345L,
            mimeType = "image/jpeg",
            storageId = "storage-id",
        )
        val newPicture = FileModel(
            id = null,
            name = "new_picture.jpg",
            sizeBytes = 12345L,
            mimeType = "image/jpeg",
            storageId = "new-storage-id",
        )

        val fileRepo = mockk<FileModelRepository>(relaxed = true)
        userProfileRepository = UserProfileRepository(
            fileRepo = fileRepo,
            db = mockk(relaxed = true)
        )
        every { userProfileRepository.findById(any()) } returns Profile(
            id = 1L,
            description = "Test User",
            picture = existingPicture
        )
        val profile = Profile(
            id = 1L,
            description = "Test User",
            picture = newPicture
        )
        userProfileRepository.updateProfile(profile)
        verify {
            userProfileRepository.findById(profile.id!!)
            userProfileRepository.updateProfile(profile)
            fileRepo.deleteById(any())
            fileRepo.deleteJoinUserProfile(any(), any())
            fileRepo.create(any())
            fileRepo.joinUserProfile(any(), any())
        }
        confirmVerified(userProfileRepository, fileRepo)
    }

}