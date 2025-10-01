package org.matamercer.domain.models

import java.util.*


data class NewFollow(
    val followerId: Long,
    val followeeId: Long,
    val notificationsEnabled: Boolean,
    val muted: Boolean,
)

data class Follow(
    val id: Long,
    val followerId: Long,
    val followeeId: Long,
    val createdAt: Date,
    val notificationsEnabled: Boolean,
    val muted: Boolean,
)