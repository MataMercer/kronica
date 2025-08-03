package unit.repository

import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.matamercer.domain.dao.FileModelDao
import org.matamercer.domain.dao.TransactionManager
import org.matamercer.domain.dao.UserProfileDao
import org.matamercer.domain.models.FileModel
import org.matamercer.domain.models.Profile
import org.matamercer.domain.repository.UserProfileRepository
import java.sql.Connection

@ExtendWith(MockKExtension::class)
class UserProfileRepositoryTest {


    @MockK(relaxUnitFun = true)
    private lateinit var userProfileRepository: UserProfileRepository

    @MockK(relaxUnitFun = true)
    private lateinit var transactionManager: TransactionManager


    @BeforeEach
    fun beforeEach() {
        clearAllMocks()
        every { transactionManager.wrap<Any>(any()) } answers { firstArg<(conn: Connection) -> Any>().invoke(mockk(relaxed = true)) }
    }

    @Test
    fun `test update user profile repository with no picture`() {
        val userProfileDao = mockk<UserProfileDao>(relaxed = true)
        every { userProfileDao.findById(any(), any()) } returns Profile(
            id = 1L,
            description = "Test User",
            picture = null
        )
        val fileModelDao = mockk<FileModelDao>(relaxed = true)
        userProfileRepository = UserProfileRepository(
            userProfileDao = userProfileDao,
            fileModelDao = fileModelDao,
            transactionManager = transactionManager,
            dataSource = mockk(relaxed = true)
        )
        val profile = Profile(
            id = 1L,
            description = "Test User",
            picture = null
        )
        userProfileRepository.updateProfile(profile)
        verify {
            userProfileDao.findById(any(), profile.id!!)
            userProfileDao.updateProfile(any(), profile)

        }
        verify(exactly = 0) {
            fileModelDao.deleteById(any(), any())
            fileModelDao.deleteJoinUserProfile(any(), any(), any())
            fileModelDao.create(any(), any())
            fileModelDao.joinUserProfile(any(), any(), any())
        }
        confirmVerified(userProfileDao)

    }

    @Test
    fun `test update user profile repository with picture`() {
        val userProfileDao = mockk<UserProfileDao>(relaxed = true)
        every { userProfileDao.findById(any(), any()) } returns Profile(
            id = 1L,
            description = "Test User",
            picture = null
        )
        val fileModelDao = mockk<FileModelDao>(relaxed = true)
        userProfileRepository = UserProfileRepository(
            userProfileDao = userProfileDao,
            fileModelDao = fileModelDao,
            transactionManager = transactionManager,
            dataSource = mockk(relaxed = true)
        )
        val profile = Profile(
            id = 1L,
            description = "Test User",
            picture = mockk(relaxed = true)
        )
        userProfileRepository.updateProfile(profile)
        verify {
            userProfileDao.findById(any(), profile.id!!)
            userProfileDao.updateProfile(any(), profile)
            fileModelDao.create(any(), any())
            fileModelDao.joinUserProfile(any(), any(), any())
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
        every { userProfileDao.findById(any(), any()) } returns Profile(
            id = 1L,
            description = "Test User",
            picture = existingPicture
        )
        val fileModelDao = mockk<FileModelDao>(relaxed = true)
        userProfileRepository = UserProfileRepository(
            userProfileDao = userProfileDao,
            fileModelDao = fileModelDao,
            transactionManager = transactionManager,
            dataSource = mockk(relaxed = true)
        )
        val profile = Profile(
            id = 1L,
            description = "Test User",
            picture = newPicture
        )
        userProfileRepository.updateProfile(profile)
        verify {
            userProfileDao.findById(any(), profile.id!!)
            userProfileDao.updateProfile(any(), profile)
            fileModelDao.deleteById(any(), any())
            fileModelDao.deleteJoinUserProfile(any(), any(), any())
            fileModelDao.create(any(), any())
            fileModelDao.joinUserProfile(any(), any(), any())
        }
        confirmVerified(userProfileDao, fileModelDao)
    }

}