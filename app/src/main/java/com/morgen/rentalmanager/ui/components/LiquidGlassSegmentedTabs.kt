package com.morgen.rentalmanager.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.highlight.HighlightStyle
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import kotlin.math.abs

/**
 * Liquid Glass Segmented Tabs with glassmorphism effects.
 * 
 * A beautiful segmented tab component with:
 * - Frosted glass container effect
 * - Sliding glass indicator with lens refraction
 * - Interactive highlight on press
 * - Smooth spring animations
 */
@Composable
fun LiquidGlassSegmentedTabs(
    items: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    backdrop: Backdrop,
    topOffset: Dp = 0.dp,
    modifier: Modifier = Modifier
) {
    val isLightTheme = !isSystemInDarkTheme()
    val containerColor = if (isLightTheme) {
        Color(0xFFFAFAFA).copy(alpha = 0.4f)
    } else {
        Color(0xFF121212).copy(alpha = 0.4f)
    }
    
    val tabsCount = items.size
    val tabsBackdrop = rememberLayerBackdrop()
    val density = LocalDensity.current
    val containerShape = RoundedCornerShape(16.dp)
    val indicatorShape = RoundedCornerShape(12.dp)
    
    // Animated indicator position
    val indicatorOffset by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
        label = "indicatorOffset"
    )
    
    Box(
        modifier = modifier
            .padding(top = topOffset)
            .fillMaxWidth()
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth()
        ) {
            val containerWidthPx = constraints.maxWidth.toFloat()
            val indicatorWidthPx = with(density) { (containerWidthPx - 8f.dp.toPx()) / tabsCount }
            
            // Layer 1: Main container with glass effect
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .drawBackdrop(
                        backdrop = backdrop,
                        shape = { containerShape },
                        effects = {
                            vibrancy()
                            blur(8f.dp.toPx())
                            lens(16f.dp.toPx(), 16f.dp.toPx())
                        },
                        highlight = {
                            Highlight(
                                style = HighlightStyle.Default(angle = 0f),
                                alpha = 0.15f
                            )
                        },
                        onDrawSurface = { drawRect(containerColor) }
                    )
                    .clip(containerShape)
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Clickable tab items
                items.forEachIndexed { index, label ->
                    val isSelected = index == selectedIndex
                    val selectionProgress = (1f - abs(indicatorOffset - index)).coerceIn(0f, 1f)
                    val interactionSource = remember { MutableInteractionSource() }
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .interactiveHighlight(interactionSource)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = {
                                    if (!isSelected) {
                                        onTabSelected(index)
                                    }
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            color = androidx.compose.ui.graphics.lerp(
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                MaterialTheme.colorScheme.primary,
                                selectionProgress
                            ),
                            fontWeight = if (selectionProgress > 0.5f) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
            
            // Layer 2: Hidden row for indicator backdrop capture
            Row(
                modifier = Modifier
                    .clearAndSetSemantics {}
                    .alpha(0f)
                    .layerBackdrop(tabsBackdrop)
                    .fillMaxWidth()
                    .height(48.dp)
                    .drawBackdrop(
                        backdrop = backdrop,
                        shape = { containerShape },
                        effects = {
                            vibrancy()
                            blur(8f.dp.toPx())
                        },
                        highlight = {
                            Highlight(
                                style = HighlightStyle.Default(angle = 0f),
                                alpha = 0.3f
                            )
                        },
                        onDrawSurface = { drawRect(containerColor) }
                    )
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { label ->
                    Box(
                        modifier = Modifier.weight(1f).height(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            // Layer 3: Glass sliding indicator (visual only)
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .graphicsLayer {
                        translationX = indicatorOffset * indicatorWidthPx
                    }
                    .fillMaxWidth(1f / tabsCount)
                    .height(40.dp)
                    .drawBackdrop(
                        backdrop = rememberCombinedBackdrop(backdrop, tabsBackdrop),
                        shape = { indicatorShape },
                        effects = {
                            lens(
                                refractionHeight = 6f.dp.toPx(),
                                refractionAmount = 10f.dp.toPx(),
                                chromaticAberration = true
                            )
                        },
                        highlight = {
                            Highlight(
                                style = HighlightStyle.Default(angle = 0f),
                                alpha = 0.4f
                            )
                        },
                        shadow = {
                            Shadow(alpha = 0.3f)
                        },
                        innerShadow = {
                            InnerShadow(
                                radius = 4f.dp,
                                alpha = 0.3f
                            )
                        },
                        onDrawSurface = {
                            drawRect(
                                if (isLightTheme) Color.Black.copy(0.08f)
                                else Color.White.copy(0.1f)
                            )
                        }
                    )
            )
        }
    }
}
