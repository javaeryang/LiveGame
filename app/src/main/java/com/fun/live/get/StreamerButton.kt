package com.`fun`.live.get

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Streamer Quality Button (流光质感按钮)
 *
 * Features:
 * 1. Traveling diagonal streamer light pass (流光扫掠)
 * 2. Metallic golden-purple gradient outline with deep shadow
 * 3. Press-scale feedback bounce
 * 4. Adds +2 spirit stone count on click
 */
@Composable
fun StreamerButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "凝 炼 灵 石 (+2)"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Button press scale bounce animation
    val scaleAnim = remember { Animatable(1f) }
    LaunchedEffect(isPressed) {
        if (isPressed) {
            scaleAnim.animateTo(0.92f, spring(dampingRatio = 0.5f, stiffness = 400f))
        } else {
            scaleAnim.animateTo(1.0f, spring(dampingRatio = 0.4f, stiffness = 300f))
        }
    }

    // Continuous Streamer Light (流光) animation phase (0f to 1f)
    val infiniteTransition = rememberInfiniteTransition(label = "ButtonStreamerAnimation")
    val streamerProgress by infiniteTransition.animateFloat(
        initialValue = -0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ButtonStreamerProgress"
    )

    // Outer glow pulse
    val borderPulse by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ButtonBorderPulse"
    )

    val shape = RoundedCornerShape(28.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .graphicsLayer {
                scaleX = scaleAnim.value
                scaleY = scaleAnim.value
            }
            .shadow(
                elevation = 16.dp,
                shape = shape,
                spotColor = Color(0xFFE040FB),
                ambientColor = Color(0xFFFFD700)
            )
            .clip(shape)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF2D0055),
                        Color(0xFF4A148C),
                        Color(0xFF6A1B9A),
                        Color(0xFF4A148C),
                        Color(0xFF2D0055)
                    )
                )
            )
            .border(
                width = 2.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFFFD700).copy(alpha = borderPulse),
                        Color(0xFFE040FB).copy(alpha = borderPulse),
                        Color(0xFF00E5FF).copy(alpha = borderPulse),
                        Color(0xFFFFD700).copy(alpha = borderPulse)
                    )
                ),
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // --- Flowing Light Streamer Layer (流光层) ---
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Calculate diagonal streamer position based on progress
            val startX = width * streamerProgress
            val streamerWidth = width * 0.45f

            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0x22FFFFFF),
                        Color(0xBBFFFFFF), // White streamer beam center
                        Color(0xAAFFD700), // Gold sheen trail
                        Color.Transparent
                    ),
                    start = Offset(startX, 0f),
                    end = Offset(startX + streamerWidth, height)
                )
            )
        }

        // --- Content Text & Sparkle Icon ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            // Sparkle icon
            Canvas(modifier = Modifier.size(20.dp)) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val r = size.width / 2f
                drawCircle(color = Color(0xFFFFD700), radius = r * 0.4f, center = center)
                drawLine(Color.White, Offset(0f, center.y), Offset(size.width, center.y), strokeWidth = 2f)
                drawLine(Color.White, Offset(center.x, 0f), Offset(center.x, size.height), strokeWidth = 2f)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = text,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
        }
    }
}
