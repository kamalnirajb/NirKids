package com.nirkids.model

/**
 * Represents a child's profile in the NirKids app.
 * This can be used to track individual progress for multiple children.
 */
data class KidProfile(
    val id: String,
    val name: String,
    val age: Int,
    val avatarUrl: String? = null,
    val lastActive: Long = System.currentTimeMillis()
)
