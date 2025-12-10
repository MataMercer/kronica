package unit.workers

import fixtures.Fixtures
import io.javalin.http.sse.SseClient
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.matamercer.domain.dao.NotificationDao
import org.matamercer.domain.models.NewNotification
import org.matamercer.domain.models.NotificationType
import org.matamercer.domain.services.NotificationService
import org.matamercer.domain.workers.NotificationWorker
import org.matamercer.setupConfig


@ExtendWith(MockKExtension::class)
class NotificationWorkerTest {

    @MockK(relaxUnitFun = true)
    private lateinit var notificationService: NotificationService

    @MockK(relaxUnitFun = true)
    private lateinit var notificationDao: NotificationDao

    @InjectMockKs
    private lateinit var notificationWorker: NotificationWorker

    @MockK(relaxUnitFun = true)
    private lateinit var sseClient: SseClient

    @BeforeEach
    fun beforeEach() {
        clearAllMocks()
        setupConfig(emptyArray<String>())

        every { sseClient.sendEvent(any()) } returns Unit
        every { notificationDao.findUnreadCount(any()) } returns 0
        notificationWorker.start()

    }

    fun afterEachTest() {

    }

    @Test
    fun `dispatch works`() {
        notificationWorker.addClient(Fixtures.rootUser.id, sseClient)
        notificationWorker.dispatch(
            NewNotification(
                notificationType = NotificationType.MENTIONED,
                subject = Fixtures.rootUser,
                subjectId = Fixtures.rootUser.id,
                targetContentId = 1,
                message = "Derp",
                recipients = listOf(Fixtures.rootUser.id),
            )
        )

    }


}