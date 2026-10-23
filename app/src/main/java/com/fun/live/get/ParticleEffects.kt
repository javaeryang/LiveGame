package com.`fun`.live.get

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

/**
 * Lightweight data class for floating "+2" particle item metadata.
 */
data class FloatingParticle(
    val id: Long = System.nanoTime() + Random.nextLong(0, 100000),
    val xOffsetDp: Float = (Random.nextFloat() - 0.5f) * 140f,
    val color: Color = if (Random.nextBoolean()) Color(0xFFE040FB) else Color(0xFFFFD700)
)

/**
 * Data class for ambient background star dust particles.
 */
data class StarParticle(
    val xRatio: Float = Random.nextFloat(),
    val yRatio: Float = Random.nextFloat(),
    val radius: Float = Random.nextFloat() * 3f + 1f,
    val baseAlpha: Float = Random.nextFloat() * 0.7f + 0.3f,
    val color: Color = if (Random.nextBoolean()) Color(0xFFE040FB) else Color(0xFF80DEEA)
)
