package org.matamercer.domain.models

import java.util.Date

abstract class Content {
    abstract val id: Long
    abstract val author: User
    abstract val createdAt: Date?
    abstract val updatedAt: Date?
    abstract val nsfw: Boolean
}