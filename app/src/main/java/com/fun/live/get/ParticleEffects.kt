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

/**
 * Data class for Elemental Light Particles (五行灵石光点) converging from Five Elements Turntable to Spirit Stone center.
 */
data class ConvergingElementalParticle(
    val id: Long = System.nanoTime() + Random.nextLong(0, 100000),
    val elementName: String,
    val color: Color,
    val startAngleRad: Float,              // Initial angle on turntable circle
    val startRadiusRatio: Float = 0.38f,  // Distance from turntable center
    val targetYOffsetDp: Float = -25f,    // Target spirit stone center offset relative to turntable center
    val durationMs: Float = 480f,          // Animation trajectory duration in ms
    val curveOffsetDp: Float = (Random.nextFloat() - 0.5f) * 35f, // Curved arc offset
    val creationTimeMs: Long = System.currentTimeMillis()
)
