package com.zionhuang.music.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Animated gradient background with subtle motion effect
 * Creates a live, breathing background that matches with Raj Music branding
 */
@Composable
fun AnimatedGradientBackground(
    modifier: Modifier = Modifier,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.secondary,
    tertiaryColor: Color = MaterialTheme.colorScheme.tertiary,
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val infiniteTransition = rememberInfiniteTransition(label = "bg_animation")
    
    // Animate offset for motion effect
    val offsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offsetX"
    )
    
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offsetY"
    )
    
    // Animate alpha for subtle breathing effect
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.12f,
        targetValue = 0.32f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val starSeed = remember { Random(42) }
    val stars = remember {
        List(140) {
            Star(
                x = starSeed.nextFloat(),
                y = starSeed.nextFloat(),
                r = 0.6f + starSeed.nextFloat() * 1.4f,
                phase = starSeed.nextFloat() * 360f
            )
        }
    }
    val bubbleSeed = remember { Random(84) }
    val dayBubbles = remember {
        val palette = listOf(
            Color(0xFF9BE2FF),
            Color(0xFFFFB3E6),
            Color(0xFFB8F2E6),
            Color(0xFFFFD3A5),
            Color(0xFFB9C8FF),
            Color(0xFFFFE1F2)
        )
        List(120) {
            Bubble(
                x = bubbleSeed.nextFloat(),
                y = bubbleSeed.nextFloat(),
                r = 8f + bubbleSeed.nextFloat() * 28f,
                phase = bubbleSeed.nextFloat() * 360f,
                color = palette[bubbleSeed.nextInt(palette.size)]
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Calculate animated center points for gradient
        val centerX1 = width * (0.3f + 0.2f * sin(Math.toRadians(offsetX.toDouble())).toFloat())
        val centerY1 = height * (0.3f + 0.2f * cos(Math.toRadians(offsetY.toDouble())).toFloat())
        
        val centerX2 = width * (0.7f + 0.2f * cos(Math.toRadians(offsetX.toDouble())).toFloat())
        val centerY2 = height * (0.7f + 0.2f * sin(Math.toRadians(offsetY.toDouble())).toFloat())
        
        if (isDark) {
            // Night sky base (deeper, no purple)
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF050A16),
                        Color(0xFF0A1224)
                    )
                )
            )

            // Cool nebula glow (blue + teal, no purple)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0D2A4D).copy(alpha = 0.60f),
                        Color.Transparent
                    ),
                    center = Offset(centerX1, centerY1),
                    radius = width * 0.9f
                ),
                radius = width * 0.9f,
                center = Offset(centerX1, centerY1)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0B3B3A).copy(alpha = 0.50f),
                        Color.Transparent
                    ),
                    center = Offset(centerX2, centerY2),
                    radius = width * 0.75f
                ),
                radius = width * 0.75f,
                center = Offset(centerX2, centerY2)
            )

            // Stars with subtle twinkle + slow drift
            val driftX = (offsetX / 360f) * 0.08f
            val driftY = (offsetY / 360f) * 0.06f
            stars.forEach { star ->
                val twinkle = 0.4f + 0.6f * sin(Math.toRadians((offsetX + star.phase).toDouble())).toFloat()
                val sx = wrap01(star.x + driftX)
                val sy = wrap01(star.y + driftY)
                drawCircle(
                    color = Color.White.copy(alpha = 0.3f + 0.4f * twinkle),
                    radius = star.r,
                    center = Offset(width * sx, height * sy)
                )
            }
        } else {
            // Day base: soft pastel gradient
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFEEE1FF),
                        Color(0xFFDCEAFF),
                        Color(0xFFF6E1F0)
                    )
                )
            )

            // Subtle fixed purple haze
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFCDBBFF).copy(alpha = 0.40f),
                        Color.Transparent
                    ),
                    start = Offset(width * 0.1f, height * 0.1f),
                    end = Offset(width * 0.9f, height * 0.9f)
                )
            )

            // Floating colorful bubbles (day mode)
            val driftX = (offsetX / 360f) * 0.06f
            val driftY = (offsetY / 360f) * 0.05f
            dayBubbles.forEach { bubble ->
                val sx = wrap01(bubble.x + driftX + 0.02f * sin(Math.toRadians(bubble.phase.toDouble())).toFloat())
                val sy = wrap01(bubble.y + driftY + 0.02f * cos(Math.toRadians(bubble.phase.toDouble())).toFloat())
                val pulse = 0.5f + 0.5f * sin(Math.toRadians((offsetY + bubble.phase).toDouble())).toFloat()

                drawCircle(
                    color = bubble.color.copy(alpha = 0.18f + 0.22f * pulse),
                    radius = bubble.r * (0.9f + 0.2f * pulse),
                    center = Offset(width * sx, height * sy)
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.25f),
                            Color.Transparent
                        ),
                        center = Offset(width * sx, height * sy),
                        radius = bubble.r * 1.1f
                    ),
                    radius = bubble.r * 1.1f,
                    center = Offset(width * sx, height * sy)
                )
            }
        }
    }
}

private data class Star(
    val x: Float,
    val y: Float,
    val r: Float,
    val phase: Float
)

private data class Bubble(
    val x: Float,
    val y: Float,
    val r: Float,
    val phase: Float,
    val color: Color
)

private fun wrap01(value: Float): Float {
    val v = value % 1f
    return if (v < 0f) v + 1f else v
}
