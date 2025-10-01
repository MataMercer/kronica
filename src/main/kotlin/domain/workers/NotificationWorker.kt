package org.matamercer.domain.workers

import io.javalin.http.sse.SseClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.matamercer.config.AppConfig
import org.matamercer.domain.dao.NotificationDao
import org.matamercer.domain.models.NewNotification
import org.matamercer.domain.services.NotificationService
import java.util.HashMap
import kotlin.time.Duration.Companion.days

//worker to handle notifications
//receive notifcations and make sure user is notified
//clean up notifications after a certain time up to the last 20 notifications
//hand it over to email worker if email notification is needed

class NotificationWorker(
    private val notificationService: NotificationService,
    private val notificationDao: NotificationDao,
) {
    private val jobsChannel = Channel<NewNotification>()
    private val completedChannel = Channel<NewNotification>()
    private val clientMapChannel = Channel<HashMap<Long, SseClient>>()

    private var coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
    var isCleanerActive: Boolean = true
    private val cleanerFrequency: Long = 1.days.inWholeMilliseconds

    fun start() {
        coroutineScope.launch { collectJobs() }
        coroutineScope.launch { distributeNotifications() }
        coroutineScope.launch { cleaner() }
    }

    private suspend fun collectJobs() = jobsChannel.receiveAsFlow().collect {
        notificationService.create(it)
        completedChannel.send(it)
    }

    fun dispatch(notificationJob: NewNotification) = runBlocking {
        jobsChannel.send(notificationJob)
    }

    fun addClient(userId: Long, client: SseClient) = runBlocking {
        val clientMap = clientMapChannel.receive()
        clientMap[userId] = client
        clientMapChannel.send(clientMap)
    }

    fun removeClient(userId: Long) = runBlocking {
        val clientMap = clientMapChannel.receive()
        clientMap.remove(userId)
        clientMapChannel.send(clientMap)
    }

    private suspend fun distributeNotifications() {
        val clientMap = clientMapChannel.receive()
        completedChannel.receiveAsFlow().collect {
            it.recipients.forEach { recipientId ->
                notifyClient(clientMap, recipientId)
            }
        }
    }

    private fun notifyClient(clientMap: Map<Long, SseClient>, recipientId: Long) {
        val client = clientMap[recipientId] ?: return
        notificationDao.findUnreadCount(recipientId)
            .also {
                client.sendEvent("$it")
            }
    }

    private suspend fun cleaner() {
        while (isCleanerActive) {
            notificationDao.deleteOldAndRead(AppConfig.maxNotificationAgeDays!!)
            delay(cleanerFrequency)
        }
    }
}