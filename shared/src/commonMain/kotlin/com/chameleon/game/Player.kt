package com.chameleon.game

import kotlinx.serialization.Serializable

@Serializable
data class Player(
    val id: String,
    val name: String,
    val score: Int = 0,
    val isChameleon: Boolean = false
)
