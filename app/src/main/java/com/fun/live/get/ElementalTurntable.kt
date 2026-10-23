package com.`fun`.live.get

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Elemental Turntable (五行转盘)
 * Features 金, 木, 水, 火, 土 five elements, pentagram array lines,
 * rotating streamer ring (流光光轮), and glowing elemental nodes.
 */
data class ElementNode(
    val name: String,
    val color: Color,
    val glowColor: Color,
    val angleDegrees: Float
)

val FIVE_ELEMENTS = listOf(
    ElementNode("金", Color(0xFFFFD700), Color(0xFFFFF176), -90f),         // Top: Gold/Metal
    ElementNode("木", Color(0xFF00E676), Color(0xFFB9F6CA), -90f + 72f),   // Right top: Wood
    ElementNode("水", Color(0xFF00E5FF), Color(0xFF80D8FF), -90f + 144f),  // Right bottom: Water
    ElementNode("火", Color(0xFFFF3D00), Color(0xFFFF8A80), -90f + 216f),  // Left bottom: Fire
    ElementNode("土", Color(0xFFFFAB00), Color(0xFFFFE082), -90f + 288f)   // Left top: Earth
)

@Composable
fun ElementalTurntable(
    modifier: Modifier = Modifier,
    activeElementIndex: Int = -1
) {
    val infiniteTransition = rememberInfiniteTransition(label = "TurntableAnimation")

    // Continuous 360-degree rotation for the turntable
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "TurntableRotation"
    )

    // Reverse rotation angle for text stability or inner ring counter-rotation
    val counterRotationAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "TurntableCounterRotation"
    )

    // Flowing light sweep beam angle (流光扫掠)
    val streamerSweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "TurntableStreamerSweep"
    )

    // Pulse factor for array lines
    val pulseFactor by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "TurntablePulse"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val outerRadius = minOf(size.width, size.height) * 0.42f
        val innerRadius = outerRadius * 0.72f
        val coreRadius = outerRadius * 0.35f

        // 1. Draw outer glowing ambient aura
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x66AB47BC),
                    Color(0x334A148C),
                    Color.Transparent
                ),
                center = center,
                radius = outerRadius * 1.3f
            ),
            radius = outerRadius * 1.3f,
            center = center
        )

        // 2. Draw rotating outer streamer ring (流光外轮)
        rotate(degrees = rotationAngle, pivot = center) {
            // Base metallic purple ring
            drawCircle(
                color = Color(0xFF6A1B9A),
                radius = outerRadius,
                center = center,
                style = Stroke(width = 4.dp.toPx())
            )

            // Outer decorative dashed rune track
            drawCircle(
                color = Color(0xAAEA80FC),
                radius = outerRadius + 6.dp.toPx(),
                center = center,
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                )
            )

            // Inner dashed ring track
            drawCircle(
                color = Color(0x88BA68C8),
                radius = innerRadius,
                center = center,
                style = Stroke(
                    width = 1.5f.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                )
            )

            // Pentagram array lines connecting the 5 elements (五行连线阵法)
            val elementPositions = FIVE_ELEMENTS.map { node ->
                val rad = Math.toRadians(node.angleDegrees.toDouble())
                Offset(
                    x = center.x + (innerRadius * cos(rad)).toFloat(),
                    y = center.y + (innerRadius * sin(rad)).toFloat()
                )
            }

            // Draw Pentagon outer lines
            for (i in elementPositions.indices) {
                val p1 = elementPositions[i]
                val p2 = elementPositions[(i + 1) % elementPositions.size]
                drawLine(
                    color = Color(0x77E040FB),
                    start = p1,
                    end = p2,
                    strokeWidth = 2.dp.toPx() * pulseFactor
                )
            }

            // Draw Pentagram star lines (connect i to (i+2))
            for (i in elementPositions.indices) {
                val p1 = elementPositions[i]
                val p2 = elementPositions[(i + 2) % elementPositions.size]
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            FIVE_ELEMENTS[i].color.copy(alpha = 0.6f * pulseFactor),
                            FIVE_ELEMENTS[(i + 2) % FIVE_ELEMENTS.size].color.copy(alpha = 0.6f * pulseFactor)
                        ),
                        start = p1,
                        end = p2
                    ),
                    start = p1,
                    end = p2,
                    strokeWidth = 1.5f.dp.toPx()
                )
            }
        }

        // 3. Counter-rotating inner Taiji/Bagua Core (五行法阵核心)
        rotate(degrees = counterRotationAngle, pivot = center) {
            drawCircle(
                color = Color(0xFF4A148C),
                radius = coreRadius,
                center = center
            )
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color(0xFFFFD700),
                        Color(0xFF00E676),
                        Color(0xFF00E5FF),
                        Color(0xFFFF3D00),
                        Color(0xFFFFAB00),
                        Color(0xFFFFD700)
                    ),
                    center = center
                ),
                radius = coreRadius,
                center = center,
                style = Stroke(width = 2f.dp.toPx())
            )
            // Core central symbol ring
            drawCircle(
                color = Color(0xEEFFFFFF),
                radius = coreRadius * 0.4f,
                center = center,
                style = Stroke(width = 1.5f.dp.toPx())
            )
        }

        // 4. Flowing Light Streamer Sweep (流光扫掠光束)
        rotate(degrees = streamerSweepAngle, pivot = center) {
            val sweepPath = Path().apply {
                moveTo(center.x, center.y)
                val sweepRad1 = Math.toRadians(-25.0)
                val sweepRad2 = Math.toRadians(25.0)
                lineTo(
                    center.x + (outerRadius * 1.15f * cos(sweepRad1)).toFloat(),
                    center.y + (outerRadius * 1.15f * sin(sweepRad1)).toFloat()
                )
                lineTo(
                    center.x + (outerRadius * 1.15f * cos(sweepRad2)).toFloat(),
                    center.y + (outerRadius * 1.15f * sin(sweepRad2)).toFloat()
                )
                close()
            }
            drawPath(
                path = sweepPath,
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x66FFFFFF),
                        Color(0x33E040FB),
                        Color.Transparent
                    ),
                    center = center,
                    radius = outerRadius * 1.2f
                )
            )
        }

        // 5. Draw the 5 Elemental Nodes (金, 木, 水, 火, 土)
        FIVE_ELEMENTS.forEachIndexed { index, node ->
            val totalAngle = rotationAngle + node.angleDegrees
            val rad = Math.toRadians(totalAngle.toDouble())
            val nodePos = Offset(
                x = center.x + (innerRadius * cos(rad)).toFloat(),
                y = center.y + (innerRadius * sin(rad)).toFloat()
            )

            val isSelected = (index == activeElementIndex)
            val nodeRadius = if (isSelected) 22.dp.toPx() else 18.dp.toPx()

            // Outer node glow (流光晕)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        node.glowColor.copy(alpha = 0.8f),
                        node.color.copy(alpha = 0.4f),
                        Color.Transparent
                    ),
                    center = nodePos,
                    radius = nodeRadius * 2f
                ),
                radius = nodeRadius * 2f,
                center = nodePos
            )

            // Metallic Node background disk
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        node.glowColor,
                        node.color,
                        Color(0xFF1A0033)
                    ),
                    center = nodePos,
                    radius = nodeRadius
                ),
                radius = nodeRadius,
                center = nodePos
            )

            // Golden/Elemental border
            drawCircle(
                color = if (isSelected) Color.White else node.glowColor,
                radius = nodeRadius,
                center = nodePos,
                style = Stroke(width = if (isSelected) 3.dp.toPx() else 1.5f.dp.toPx())
            )

            // Draw Element Chinese Character Text (金 / 木 / 水 / 火 / 土)
            drawElementText(
                scope = this,
                text = node.name,
                position = nodePos,
                textColor = Color(0xFF10002B),
                fontSizePx = nodeRadius * 1.1f
            )
        }
    }
}

private fun drawElementText(
    scope: DrawScope,
    text: String,
    position: Offset,
    textColor: Color,
    fontSizePx: Float
) {
    scope.drawContext.canvas.nativeCanvas.apply {
        val paint = Paint().apply {
            color = textColor.toArgb()
            textSize = fontSizePx
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            // Shadow / glow behind text
            setShadowLayer(6f, 0f, 0f, android.graphics.Color.WHITE)
        }
        val textBaselineY = position.y - (paint.descent() + paint.ascent()) / 2f
        drawText(text, position.x, textBaselineY, paint)
    }
}
