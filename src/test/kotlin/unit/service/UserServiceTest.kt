package unit.service

import fixtures.Fixtures
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import okhttp3.OkHttpClient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.matamercer.domain.repository.FollowRepository
import org.matamercer.domain.repository.UserRepository
import org.matamercer.domain.services.UserService
import org.matamercer.domain.workers.NotificationWorker
import kotlin.test.assertEquals


@ExtendWith(MockKExtension::class)
class UserServiceTest {

    @MockK(relaxUnitFun = true)
    private lateinit var userRepository: UserRepository

    @MockK(relaxUnitFun = true)
    private lateinit var followRepository: FollowRepository

    @MockK(relaxUnitFun = true)
    private lateinit var notificationWorker: NotificationWorker

    @MockK(relaxUnitFun = true)
    private lateinit var okHttpClient: OkHttpClient

    @InjectMockKs
    private lateinit var userService: UserService

    @BeforeEach
    fun beforeEach() {
        clearAllMocks()
        every { userRepository.findByName(Fixtures.rootUser.name) } returns (Fixtures.rootUser)
        every { userRepository.findByName(Fixtures.maliciousUser.name) } returns (Fixtures.maliciousUser)
    }

    @Test
    fun `test mentioned users`() {
        var foundUsers = userService.getMentionedUsers("derp derpa")
        assert(foundUsers.isEmpty()).equals(true)
        foundUsers = userService.getMentionedUsers("Hello to @${Fixtures.rootUser.name} and @${Fixtures.maliciousUser.name}")
        assertEquals(foundUsers.size, 2)
    }

}