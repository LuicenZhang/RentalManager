package com.morgen.rentalmanager.ui.components

import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy

/**
 * Liquid Glass styled button component.
 * 
 * A frosted glass button with blur effect and press animation.
 * Falls back to glass-like styling on older Android versions.
 */
@Composable
fun LiquidGlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backdrop: Backdrop? = null,
    shape: Shape = RoundedCornerShape(12.dp),
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val isBlurSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val isLightTheme = !isSystemInDarkTheme()
    
    val containerColor = if (isLightTheme) {
        Color(0xFFFAFAFA).copy(alpha = 0.5f)
    } else {
        Color(0xFF2A2A2A).copy(alpha = 0.5f)
    }
    
    val fallbackBackground = if (isLightTheme) {
        Color(0xFFFAFAFA).copy(alpha = 0.8f)
    } else {
        Color(0xFF2A2A2A).copy(alpha = 0.8f)
    }
    
    val borderColor = if (isLightTheme) {
        Color.White.copy(alpha = 0.5f)
    } else {
        Color.White.copy(alpha = 0.2f)
    }
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f),
        label = "buttonScale"
    )
    
    Row(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = if (enabled) 1f else 0.5f
            }
            .then(
                if (backdrop != null && isBlurSupported) {
                    Modifier.drawBackdrop(
                        backdrop = backdrop,
                        shape = { shape },
                        effects = {
                            vibrancy()
                            blur(12f.dp.toPx())
                        },
                        onDrawSurface = { drawRect(containerColor) }
                    )
                } else {
                    Modifier
                        .clip(shape)
                        .background(fallbackBackground)
                        .border(
                            width = 1.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(borderColor, borderColor.copy(alpha = 0.1f))
                            ),
                            shape = shape
                        )
                }
            )
            .then(
                if (enabled) {
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

/**
 * Liquid Glass styled FAB (Floating Action Button).
 * 
 * A circular frosted glass button with blur effect.
 */
@Composable
fun LiquidGlassFAB(
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    backdrop: Backdrop? = null,
    size: Dp = 56.dp,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    contentDescription: String? = null
) {
    val isBlurSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val isLightTheme = !isSystemInDarkTheme()
    
    val containerColor = if (isLightTheme) {
        Color(0xFFFAFAFA).copy(alpha = 0.6f)
    } else {
        Color(0xFF1E1E1E).copy(alpha = 0.6f)
    }
    
    val fallbackBackground = if (isLightTheme) {
        Color(0xFFFAFAFA).copy(alpha = 0.9f)
    } else {
        Color(0xFF2A2A2A).copy(alpha = 0.9f)
    }
    
    val borderColor = if (isLightTheme) {
        Color.White.copy(alpha = 0.6f)
    } else {
        Color.White.copy(alpha = 0.25f)
    }
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 400f),
        label = "fabScale"
    )
    
    Box(
        modifier = modifier
            // 匹配 TopBar 菜单的尺寸 48.dp
            .size(48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(
                if (backdrop != null && isBlurSupported) {
                    Modifier.drawBackdrop(
                        backdrop = backdrop,
                        shape = { RoundedCornerShape(20.dp) },
                        effects = {
                            vibrancy()
                            blur(12f.dp.toPx())
                            lens(20f.dp.toPx(), 20f.dp.toPx())
                        },
                        onDrawSurface = { drawRect(containerColor) }
                    )
                } else {
                    Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(fallbackBackground)
                        .border(
                            width = 1.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(borderColor, borderColor.copy(alpha = 0.1f))
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                }
            )
            .interactiveHighlight(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Small Liquid Glass button for secondary actions.
 */
@Composable
fun LiquidGlassSmallButton(
    onClick: () -> Unit,
    text: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
    backdrop: Backdrop? = null
) {
    LiquidGlassButton(
        onClick = onClick,
        modifier = modifier,
        backdrop = backdrop,
        shape = RoundedCornerShape(8.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
