package org.matamercer.domain.workers

import io.javalin.http.sse.SseClient
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.matamercer.config.AppConfig
import org.matamercer.domain.dao.NotificationDao
import org.matamercer.domain.models.NewNotification
import org.matamercer.domain.services.NotificationService
import kotlin.time.Duration.Companion.days

//worker to handle notifications
//receive notifcations and make sure user is notified
//clean up notifications after a certain time up to the last 20 notifications
//hand it over to email worker if email notification is needed

class NotificationWorker(
    private val notificationService: NotificationService,
    private val notificationDao: NotificationDao,
) {
    private val notifFlow = MutableStateFlow<List<NewNotification>>(emptyList())
    private val completedFlow = MutableStateFlow<List<NewNotification>>(emptyList())
    private val clientMapFlow = MutableStateFlow<Map<Long, List<SseClient>>>(emptyMap<Long, List<SseClient>>())

    private var coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
    var isCleanerActive: Boolean = true
    private val cleanerFrequency: Long = 1.days.inWholeMilliseconds

    fun start() {
        coroutineScope.launch { collectJobs() }
        coroutineScope.launch { distributeNotifications() }
        coroutineScope.launch { cleaner() }
    }

    private suspend fun collectJobs(): Unit = notifFlow.collect { newNotifications ->
        if (newNotifications.isNotEmpty()) {
            val newNotif = newNotifications.last()
            notificationService.create(newNotif)
            completedFlow.update { it + newNotif }
        }
    }

    fun dispatch(notificationJob: NewNotification) = coroutineScope.launch {
        notifFlow.update { it + notificationJob }
    }

    fun addClient(userId: Long, client: SseClient) = runBlocking {
        clientMapFlow.update {
            if (it.contains(userId)) {
                return@update it + (userId to (it[userId]!! + client))
            } else {
                return@update it + (userId to (listOf<SseClient>(client)))
            }
        }
    }

    fun removeClient(userId: Long) = runBlocking {
//        val clientMap = clientMapChannel.receive()
//        clientMap.remove(userId)
//        clientMapChannel.send(clientMap)
    }

    private suspend fun distributeNotifications() {
        val clientMap = clientMapFlow.value

        completedFlow.collect { completed ->
            if (completed.isNotEmpty()) {
                completed.last().recipients.forEach { recipientId ->
                    notifyClient(clientMap, recipientId)
                }
            }
        }
    }

    private fun notifyClient(clientMap: Map<Long, List<SseClient>>, recipientId: Long) {
        val clients = clientMap[recipientId] ?: return
        notificationDao.findUnreadCount(recipientId)
            .also { unreadCount ->
                clients.forEach { client ->
                    client.sendEvent("$unreadCount")
                }
            }
    }

    private suspend fun cleaner() {
        while (isCleanerActive) {
            notificationDao.deleteOldAndRead(AppConfig.maxNotificationAgeDays!!)
            delay(cleanerFrequency)
        }
    }

}