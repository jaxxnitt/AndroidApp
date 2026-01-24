package com.jaxxnitt.myapplication.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import com.jaxxnitt.myapplication.ui.theme.CelebrationCyan
import com.jaxxnitt.myapplication.ui.theme.CelebrationOrange
import com.jaxxnitt.myapplication.ui.theme.CelebrationPink
import com.jaxxnitt.myapplication.ui.theme.CelebrationPurple
import com.jaxxnitt.myapplication.ui.theme.CelebrationYellow
import com.jaxxnitt.myapplication.ui.theme.Success
import com.jaxxnitt.myapplication.ui.theme.SuccessLight
import kotlin.random.Random

data class ConfettiPiece(
    val x: Float,
    val y: Float,
    val size: Float,
    val color: Color,
    val rotation: Float,
    val velocityX: Float,
    val velocityY: Float,
    val rotationSpeed: Float,
    val shape: ConfettiShape
)

enum class ConfettiShape {
    RECTANGLE, CIRCLE, SQUARE
}

@Composable
fun ConfettiAnimation(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    key: Int = 0
) {
    if (!isVisible) return

    val animationProgress = remember { Animatable(0f) }

    val confettiColors = listOf(
        CelebrationPink,
        CelebrationYellow,
        CelebrationPurple,
        CelebrationOrange,
        CelebrationCyan,
        Success,
        SuccessLight,
        Color(0xFF00FF7F),
        Color(0xFFFFE066),
        Color(0xFFFF6B9D)
    )

    val confettiPieces = remember(key) {
        List(150) {
            ConfettiPiece(
                x = Random.nextFloat(),
                y = Random.nextFloat() * -0.5f - 0.5f,
                size = Random.nextFloat() * 15f + 8f,
                color = confettiColors[Random.nextInt(confettiColors.size)],
                rotation = Random.nextFloat() * 360f,
                velocityX = (Random.nextFloat() - 0.5f) * 0.4f,
                velocityY = Random.nextFloat() * 0.3f + 0.5f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 15f,
                shape = ConfettiShape.entries[Random.nextInt(ConfettiShape.entries.size)]
            )
        }
    }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            animationProgress.snapTo(0f)
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 3500,
                    easing = LinearEasing
                )
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val progress = animationProgress.value
        val fadeOutStart = 0.7f
        val alpha = if (progress > fadeOutStart) {
            1f - ((progress - fadeOutStart) / (1f - fadeOutStart))
        } else {
            1f
        }

        confettiPieces.forEach { piece ->
            val currentX = (piece.x + piece.velocityX * progress) * size.width
            val currentY = (piece.y + piece.velocityY * progress * 2.5f) * size.height
            val currentRotation = piece.rotation + piece.rotationSpeed * progress * 360f

            if (currentY > -50 && currentY < size.height + 50) {
                rotate(
                    degrees = currentRotation,
                    pivot = Offset(currentX, currentY)
                ) {
                    when (piece.shape) {
                        ConfettiShape.RECTANGLE -> {
                            drawRect(
                                color = piece.color.copy(alpha = alpha),
                                topLeft = Offset(currentX - piece.size / 2, currentY - piece.size),
                                size = Size(piece.size, piece.size * 2.5f)
                            )
                        }
                        ConfettiShape.CIRCLE -> {
                            drawCircle(
                                color = piece.color.copy(alpha = alpha),
                                radius = piece.size / 2,
                                center = Offset(currentX, currentY)
                            )
                        }
                        ConfettiShape.SQUARE -> {
                            drawRect(
                                color = piece.color.copy(alpha = alpha),
                                topLeft = Offset(currentX - piece.size / 2, currentY - piece.size / 2),
                                size = Size(piece.size, piece.size)
                            )
                        }
                    }
                }
            }
        }
    }
}
