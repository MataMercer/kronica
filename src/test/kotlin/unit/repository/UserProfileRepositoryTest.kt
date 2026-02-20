package unit.repository

import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.matamercer.domain.dao.FileModelDao
import org.matamercer.domain.dao.UserProfileDao
import org.matamercer.domain.jdbc.txn
import org.matamercer.domain.models.FileModel
import org.matamercer.domain.models.Profile
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
        val userProfileDao = mockk<UserProfileDao>(relaxed = true)
        every { userProfileDao.findById(any()) } returns Profile(
            id = 1L,
            description = "Test User",
            picture = null
        )
        val fileModelDao = mockk<FileModelDao>(relaxed = true)
        userProfileRepository = UserProfileRepository(
            userProfileDao = userProfileDao,
            fileModelDao = fileModelDao,
        )
        val profile = Profile(
            id = 1L,
            description = "Test User",
            picture = null
        )
        userProfileRepository.updateProfile(profile)
        verify {
            userProfileDao.findById(profile.id!!)
            userProfileDao.updateProfile(profile)

        }
        verify(exactly = 0) {
            fileModelDao.deleteById(any())
            fileModelDao.deleteJoinUserProfile(any(), any())
            fileModelDao.create(any())
            fileModelDao.joinUserProfile(any(), any())
        }
        confirmVerified(userProfileDao)

    }

    @Test
    fun `test update user profile repository with picture`() {
        val userProfileDao = mockk<UserProfileDao>(relaxed = true)
        every { userProfileDao.findById(any()) } returns Profile(
            id = 1L,
            description = "Test User",
            picture = null
        )
        val fileModelDao = mockk<FileModelDao>(relaxed = true)
        userProfileRepository = UserProfileRepository(
            userProfileDao = userProfileDao,
            fileModelDao = fileModelDao,
        )
        val profile = Profile(
            id = 1L,
            description = "Test User",
            picture = mockk(relaxed = true)
        )
        userProfileRepository.updateProfile(profile)
        verify {
            userProfileDao.findById(profile.id!!)
            userProfileDao.updateProfile(profile)
            fileModelDao.create(any())
            fileModelDao.joinUserProfile(any(), any())
        }
        confirmVerified(userProfileDao, fileModelDao)
    }

    @Test
    fun `test update user profile repository with picture and delete existing picture`() {
        val userProfileDao = mockk<UserProfileDao>(relaxed = true)
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
        every { userProfileDao.findById(any()) } returns Profile(
            id = 1L,
            description = "Test User",
            picture = existingPicture
        )
        val fileModelDao = mockk<FileModelDao>(relaxed = true)
        userProfileRepository = UserProfileRepository(
            userProfileDao = userProfileDao,
            fileModelDao = fileModelDao,
        )
        val profile = Profile(
            id = 1L,
            description = "Test User",
            picture = newPicture
        )
        userProfileRepository.updateProfile(profile)
        verify {
            userProfileDao.findById(profile.id!!)
            userProfileDao.updateProfile(profile)
            fileModelDao.deleteById(any())
            fileModelDao.deleteJoinUserProfile(any(), any())
            fileModelDao.create(any())
            fileModelDao.joinUserProfile(any(), any())
        }
        confirmVerified(userProfileDao, fileModelDao)
    }

}