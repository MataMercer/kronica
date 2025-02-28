package org.matamercer.domain.models

import java.util.*

class Follow(
    val id: Long? = null,
    val followerId: Long,
    val followeeId: Long,
    val createdAt: Date? = null,
) {
}