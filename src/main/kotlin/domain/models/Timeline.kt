package org.matamercer.domain.models

data class Timeline(
    val id: Long? = null,
    val name: String,
    val description: String,
    val author: User? = null,
    )