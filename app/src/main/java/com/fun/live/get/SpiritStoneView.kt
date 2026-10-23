package com.`fun`.live.get

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Floating Purple Spirit Stone & Supreme Crystal (悬空紫色灵石 / 至尊水晶)
 *
 * Requirements:
 * 1. Floating hovering animation (悬空动画)
 * 2. Size volume grows as spirit stone count increases (随着灵石增加，体积增大)
 * 3. Deep purple & multi-colored Supreme Crystal texture (紫色质感 & 至尊水晶)
 * 4. Flowing light sheen streamer quality (流光品质质感)
 * 5. Level scales up to Level 100 (支持 1 到 100 阶至尊水晶进化)
 */
@Composable
fun SpiritStoneView(
    spiritStoneCount: Int,
    level: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SpiritStoneAnimation")

    // 1. Floating vertical bobbing animation
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FloatBob"
    )

    // 2. Subtle rocking/sway rotation angle (-4° to +4°)
    val swayAnim by infiniteTransition.animateFloat(
        initialValue = -3.5f,
        targetValue = 3.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "SwayBob"
    )

    // 3. Flowing Light Streamer sheen position (0.0 to 1.0 sweep)
    val streamerAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "StreamerSheen"
    )

    // 4. Orbiting rune ring rotation
    val orbitRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbitRotation"
    )

    // 5. Volume/Scale Growth based on Spirit Stone count (0 to 100)
    val targetScale = 1.0f + (spiritStoneCount.coerceIn(0, 100) / 100f) * 0.45f
    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(stiffness = 200f, dampingRatio = 0.6f),
        label = "VolumeScaleGrowth"
    )

    val isSupremeCrystal = level >= 90

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val verticalOffset = (sin(floatAnim * PI).toFloat() - 0.5f) * 28.dp.toPx()
        val stoneCenter = Offset(center.x, center.y + verticalOffset)

        val baseRadius = minOf(size.width, size.height) * 0.22f * animatedScale

        // --- A. Draw Outer Ambient Energy Aura (紫气蒸腾 / 至尊光辉) ---
        val auraColors = if (isSupremeCrystal) {
            listOf(
                Color(0xFFFFD700).copy(alpha = 0.7f),
                Color(0xFFE040FB).copy(alpha = 0.5f),
                Color(0xFF00E5FF).copy(alpha = 0.3f),
                Color.Transparent
            )
        } else {
            listOf(
                Color(0xFFE040FB).copy(alpha = 0.6f),
                Color(0xFF8E24AA).copy(alpha = 0.35f),
                Color(0xFF310052).copy(alpha = 0.15f),
                Color.Transparent
            )
        }

        drawCircle(
            brush = Brush.radialGradient(
                colors = auraColors,
                center = stoneCenter,
                radius = baseRadius * 2.4f
            ),
            radius = baseRadius * 2.4f,
            center = stoneCenter
        )

        // --- B. Orbiting Celestial Spirit Rings (Level 2+ to 100) ---
        if (level >= 2) {
            rotate(degrees = orbitRotation, pivot = stoneCenter) {
                drawOval(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color(0xFFE040FB),
                            Color(0x00E040FB),
                            Color(0xFF00E5FF),
                            Color(0x0000E5FF),
                            Color(0xFFFFD700),
                            Color(0xFFE040FB)
                        ),
                        center = stoneCenter
                    ),
                    topLeft = Offset(stoneCenter.x - baseRadius * 1.5f, stoneCenter.y - baseRadius * 0.4f),
                    size = androidx.compose.ui.geometry.Size(baseRadius * 3.0f, baseRadius * 0.8f),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        if (level >= 3 || isSupremeCrystal) {
            rotate(degrees = -orbitRotation * 1.4f, pivot = stoneCenter) {
                drawOval(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color(0xFFFFD700),
                            Color(0x22FFD700),
                            Color(0xFFE040FB),
                            Color(0x00E040FB)
                        ),
                        center = stoneCenter
                    ),
                    topLeft = Offset(stoneCenter.x - baseRadius * 1.3f, stoneCenter.y - baseRadius * 0.6f),
                    size = androidx.compose.ui.geometry.Size(baseRadius * 2.6f, baseRadius * 1.2f),
                    style = Stroke(width = 1.5f.dp.toPx())
                )
            }
        }

        // Level 50+ / Supreme Crystal Triple Ring
        if (level >= 50) {
            rotate(degrees = orbitRotation * 2.0f, pivot = stoneCenter) {
                drawOval(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color(0xFF00E5FF),
                            Color(0xFFFFD700),
                            Color(0xFFE040FB),
                            Color(0xFF00E5FF)
                        ),
                        center = stoneCenter
                    ),
                    topLeft = Offset(stoneCenter.x - baseRadius * 1.1f, stoneCenter.y - baseRadius * 0.8f),
                    size = androidx.compose.ui.geometry.Size(baseRadius * 2.2f, baseRadius * 1.6f),
                    style = Stroke(width = 1.5f.dp.toPx())
                )
            }
        }

        // --- C. Draw 3D Multifaceted Crystal Geometry (多面灵石 / 至尊水晶) ---
        rotate(degrees = swayAnim, pivot = stoneCenter) {
            drawFacetCrystal(
                scope = this,
                center = stoneCenter,
                radius = baseRadius,
                level = level,
                streamerProgress = streamerAnim,
                isSupremeCrystal = isSupremeCrystal
            )
        }
    }
}

/**
 * Renders the 3D faceted crystal geometry for levels up to 100 (至尊水晶).
 */
private fun drawFacetCrystal(
    scope: DrawScope,
    center: Offset,
    radius: Float,
    level: Int,
    streamerProgress: Float,
    isSupremeCrystal: Boolean
) {
    val topApex = Offset(center.x, center.y - radius * 1.35f)
    val bottomApex = Offset(center.x, center.y + radius * 1.35f)

    val beltRadiusX = radius * 0.95f
    val beltRadiusY = radius * 0.35f
    val beltY = center.y - radius * 0.1f

    val numFacetSides = when {
        isSupremeCrystal -> 10
        level >= 50 -> 9
        level >= 20 -> 8
        level >= 5 -> 7
        else -> 6
    }

    val beltPoints = List(numFacetSides) { i ->
        val angleRad = (i * 2.0 * PI / numFacetSides) - (PI / 2.0)
        Offset(
            x = center.x + (beltRadiusX * cos(angleRad)).toFloat(),
            y = beltY + (beltRadiusY * sin(angleRad)).toFloat()
        )
    }

    // Color Palette
    val darkPurpleShadow = Color(0xFF2D0055)
    val deepAmethyst = if (isSupremeCrystal) Color(0xFF510080) else Color(0xFF4A148C)
    val royalPurple = if (isSupremeCrystal) Color(0xFF00E5FF) else Color(0xFF7B1FA2)
    val brightViolet = if (isSupremeCrystal) Color(0xFFE040FB) else Color(0xFFAB47BC)
    val neonMagenta = if (isSupremeCrystal) Color(0xFFFFD700) else Color(0xFFE040FB)
    val crystalHighlight = if (isSupremeCrystal) Color.White else Color(0xFFF3E5F5)

    // --- 1. Upper Pyramid Facets ---
    for (i in beltPoints.indices) {
        val p1 = beltPoints[i]
        val p2 = beltPoints[(i + 1) % beltPoints.size]
        val isFrontFacing = (p1.y > beltY || p2.y > beltY) || (p1.x > center.x - beltRadiusX * 0.8f)

        if (isFrontFacing) {
            val upperFacetPath = Path().apply {
                moveTo(topApex.x, topApex.y)
                lineTo(p1.x, p1.y)
                lineTo(p2.x, p2.y)
                close()
            }

            val facetLightFactor = ((i.toFloat() / numFacetSides) + 0.3f) % 1.0f
            val baseColor = when {
                facetLightFactor < 0.25f -> neonMagenta
                facetLightFactor < 0.5f -> brightViolet
                facetLightFactor < 0.75f -> royalPurple
                else -> deepAmethyst
            }

            scope.drawPath(
                path = upperFacetPath,
                brush = Brush.linearGradient(
                    colors = listOf(
                        crystalHighlight.copy(alpha = 0.95f),
                        baseColor,
                        darkPurpleShadow
                    ),
                    start = topApex,
                    end = Offset((p1.x + p2.x) / 2f, (p1.y + p2.y) / 2f)
                )
            )

            scope.drawPath(
                path = upperFacetPath,
                color = if (isSupremeCrystal) Color(0xFFFFD700) else Color(0x99EA80FC),
                style = Stroke(width = if (isSupremeCrystal) 1.6f else 1.2f)
            )
        }
    }

    // --- 2. Lower Pyramid Facets ---
    for (i in beltPoints.indices) {
        val p1 = beltPoints[i]
        val p2 = beltPoints[(i + 1) % beltPoints.size]
        val isFrontFacing = (p1.y > beltY || p2.y > beltY) || (p1.x > center.x - beltRadiusX * 0.8f)

        if (isFrontFacing) {
            val lowerFacetPath = Path().apply {
                moveTo(bottomApex.x, bottomApex.y)
                lineTo(p1.x, p1.y)
                lineTo(p2.x, p2.y)
                close()
            }

            val facetLightFactor = ((i.toFloat() / numFacetSides) + 0.5f) % 1.0f
            val baseColor = when {
                facetLightFactor < 0.3f -> brightViolet
                facetLightFactor < 0.7f -> royalPurple
                else -> darkPurpleShadow
            }

            scope.drawPath(
                path = lowerFacetPath,
                brush = Brush.linearGradient(
                    colors = listOf(
                        baseColor,
                        darkPurpleShadow,
                        Color(0xFF140026)
                    ),
                    start = Offset((p1.x + p2.x) / 2f, (p1.y + p2.y) / 2f),
                    end = bottomApex
                )
            )

            scope.drawPath(
                path = lowerFacetPath,
                color = if (isSupremeCrystal) Color(0xFF00E5FF) else Color(0x66BA68C8),
                style = Stroke(width = if (isSupremeCrystal) 1.6f else 1.2f)
            )
        }
    }

    // --- 3. Flowing Light Streamer Sheen Pass (流光质感扫掠) ---
    val streamerStartX = center.x - radius * 2f + (streamerProgress * radius * 4f)
    val streamerStartY = center.y - radius * 1.5f
    val streamerEndX = streamerStartX + radius * 1.2f
    val streamerEndY = streamerStartY + radius * 3.0f

    val streamerPath = Path().apply {
        moveTo(topApex.x, topApex.y)
        beltPoints.forEach { lineTo(it.x, it.y) }
        lineTo(bottomApex.x, bottomApex.y)
        close()
    }

    scope.drawPath(
        path = streamerPath,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.Transparent,
                Color(0x22FFFFFF),
                Color.White, // Intense white stream light core
                if (isSupremeCrystal) Color(0xFFFFD700) else Color(0x99E040FB),
                Color.Transparent
            ),
            start = Offset(streamerStartX, streamerStartY),
            end = Offset(streamerEndX, streamerEndY)
        )
    )

    // --- 4. Apex Star Flare (顶端至尊灵光璀璨) ---
    drawStarSparkle(
        scope = scope,
        center = topApex,
        size = if (isSupremeCrystal) radius * 0.45f else radius * 0.35f,
        color = if (isSupremeCrystal) Color(0xFFFFD700) else Color.White
    )

    drawStarSparkle(
        scope = scope,
        center = bottomApex,
        size = if (isSupremeCrystal) radius * 0.3f else radius * 0.2f,
        color = if (isSupremeCrystal) Color(0xFF00E5FF) else Color(0xFFE040FB)
    )
}

/**
 * Draws a 4-point lens flare star sparkle.
 */
private fun drawStarSparkle(
    scope: DrawScope,
    center: Offset,
    size: Float,
    color: Color
) {
    scope.drawCircle(
        color = color.copy(alpha = 0.5f),
        radius = size * 0.6f,
        center = center
    )

    scope.drawLine(
        color = color,
        start = Offset(center.x - size, center.y),
        end = Offset(center.x + size, center.y),
        strokeWidth = 2.5f,
        cap = StrokeCap.Round
    )

    scope.drawLine(
        color = color,
        start = Offset(center.x, center.y - size),
        end = Offset(center.x, center.y + size),
        strokeWidth = 2.5f,
        cap = StrokeCap.Round
    )
}
