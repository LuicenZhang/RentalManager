package com.morgen.rentalmanager.ui.components

import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import kotlinx.coroutines.flow.collectLatest

/**
 * A Liquid Glass styled card component.
 * 
 * Provides a frosted glass background effect using the Backdrop library.
 * When backdrop is null or on older Android versions, uses a glass-like fallback 
 * with gradient and border styling.
 * 
 * Based on the Stock app's LiquidGlassCard design.
 * 
 * @param modifier Modifier for the card
 * @param backdrop The Backdrop instance to use for glass effects (optional)
 * @param shape The shape of the card
 * @param tintColor Optional tint color for the glass (e.g., for status indicators)
 * @param onClick Optional click handler
 * @param content The content to display inside the card
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    backdrop: Backdrop? = null,
    shape: Shape = RoundedCornerShape(16.dp),
    tintColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val isBlurSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val effectiveBackdrop = if (isBlurSupported) backdrop else null
    
    val isLightTheme = !isSystemInDarkTheme()
    
    // Base container color (can be tinted)
    val baseContainerColor = if (isLightTheme) {
        Color(0xFFFAFAFA)
    } else {
        Color(0xFF1E1E1E)
    }
    
    // Apply tint if provided (blend with base color)
    val containerColor = if (tintColor != null) {
        Color(
            red = baseContainerColor.red * 0.6f + tintColor.red * 0.4f,
            green = baseContainerColor.green * 0.6f + tintColor.green * 0.4f,
            blue = baseContainerColor.blue * 0.6f + tintColor.blue * 0.4f,
            alpha = 0.45f
        )
    } else {
        baseContainerColor.copy(alpha = 0.35f)
    }
    
    // Glass-like fallback colors
    val fallbackBackground = if (tintColor != null) {
        val baseColor = if (isLightTheme) Color(0xFFFAFAFA) else Color(0xFF2A2A2A)
        Color(
            red = baseColor.red * 0.5f + tintColor.red * 0.5f,
            green = baseColor.green * 0.5f + tintColor.green * 0.5f,
            blue = baseColor.blue * 0.5f + tintColor.blue * 0.5f,
            alpha = 0.5f
        )
    } else if (isLightTheme) {
        Color(0xFFFAFAFA).copy(alpha = 0.7f)
    } else {
        Color(0xFF2A2A2A).copy(alpha = 0.7f)
    }
    
    val fallbackBorderColor = if (tintColor != null) {
        tintColor.copy(alpha = if (isLightTheme) 0.3f else 0.2f)
    } else if (isLightTheme) {
        Color.White.copy(alpha = 0.5f)
    } else {
        Color.White.copy(alpha = 0.15f)
    }
    
    val interactionSource = remember { MutableInteractionSource() }
    
    Box(
        modifier = modifier
            .then(
                if (effectiveBackdrop != null) {
                    Modifier.drawBackdrop(
                        backdrop = effectiveBackdrop,
                        shape = { shape },
                        effects = {
                            vibrancy()
                            blur(12f.dp.toPx())
                        },
                        onDrawSurface = { drawRect(containerColor) }
                    )
                } else {
                    // Glass-like fallback without Backdrop
                    Modifier
                        .clip(shape)
                        .background(fallbackBackground)
                        .border(
                            width = 1.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    fallbackBorderColor,
                                    fallbackBorderColor.copy(alpha = 0.1f)
                                )
                            ),
                            shape = shape
                        )
                }
            )
            .then(
                if (onClick != null) {
                    Modifier
                        .interactiveHighlight(interactionSource)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = onClick
                        )
                } else {
                    Modifier
                }
            )
    ) {
        content()
    }
}

/**
 * Interactive highlight effect for glass components.
 * Shows a glow overlay when pressed.
 */
@Composable
fun Modifier.interactiveHighlight(
    interactionSource: MutableInteractionSource
): Modifier {
    val pressProgress = remember { Animatable(0f) }
    var pressPosition by remember { mutableStateOf(Offset.Zero) }
    
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collectLatest { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    pressPosition = interaction.pressPosition
                    pressProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f)
                    )
                }
                is PressInteraction.Release, is PressInteraction.Cancel -> {
                    pressProgress.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f)
                    )
                }
            }
        }
    }
    
    return this.drawWithContent {
        val progress = pressProgress.value
        
        if (progress > 0f) {
            // Draw white overlay on entire component
            drawRect(
                color = Color.White.copy(alpha = 0.08f * progress),
                blendMode = BlendMode.Plus
            )
        }
        
        drawContent()
    }
}
