package com.morgen.rentalmanager.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.vibrancy

/**
 * Center sheet with Liquid Glass blur effect.
 * Based on Stock app's GlassCenterSheet implementation.
 * Displays content in center with scale animation and blur background.
 */
@Composable
fun GlassCenterSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    backdrop: Backdrop?,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val isLightTheme = !isSystemInDarkTheme()
    val shape = RoundedCornerShape(24.dp)
    
    // Background color - more transparent for glass effect
    val backgroundColor = if (isLightTheme) {
        Color(0xFFF8F8F8).copy(alpha = 0.3f)
    } else {
        Color(0xFF1E1E1E).copy(alpha = 0.3f)
    }
    
    // Handle back button press to dismiss
    androidx.activity.compose.BackHandler(enabled = visible) {
        onDismiss()
    }
    
    // Scrim overlay
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .pointerInput(Unit) {
                    detectTapGestures { onDismiss() }
                }
        )
    }
    
    // Center content with scale animation
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)) +
                scaleIn(
                    initialScale = 0.9f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ),
        exit = fadeOut(animationSpec = tween(200)) +
               scaleOut(
                   targetScale = 0.9f,
                   animationSpec = tween(200)
               ),
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight()
                    .then(
                        if (backdrop != null) {
                            Modifier.drawBackdrop(
                                backdrop = backdrop,
                                shape = { shape },
                                effects = {
                                    vibrancy()
                                    blur(24f.dp.toPx())
                                },
                                onDrawSurface = { drawRect(backgroundColor) }
                            )
                        } else {
                            Modifier
                                .clip(shape)
                                .background(backgroundColor)
                        }
                    )
                    .pointerInput(Unit) {
                        detectTapGestures { /* Block tap from reaching scrim */ }
                    }
            ) {
                // Content
                content()
            }
        }
    }
}
