package com.morgen.rentalmanager.ui.components

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.activity.compose.BackHandler
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalDensity
import com.kyant.backdrop.effects.vibrancy
import kotlinx.coroutines.delay

/**
 * Liquid Glass styled floating top bar with TWO separate capsules:
 * - Left capsule: Icon + Title
 * - Right capsule: Menu button
 * 
 * No background overlay - just two floating glass pills.
 * Based on Stock app's LiquidGlassTopBar design.
 * 
 * @param title The title text to display
 * @param icon Optional icon to display (default: Home icon)
 * @param backdrop The Backdrop instance for glass effects
 * @param blurRadius Blur radius in dp
 * @param onMenuClick Menu button click handler
 * @param menuExpanded Whether the menu is expanded
 * @param onDismissMenu Menu dismiss callback
 * @param menuContent Menu content
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrostedBlurTopBar(
    backdrop: Backdrop? = null,
    height: Dp = 56.dp,
    blurRadius: Float = 12f,
    overlayColor: Color = Color.Unspecified,
    title: String,
    icon: ImageVector = Icons.Default.Home,
    onIconClick: () -> Unit = {},
    showMenu: Boolean = true,
    onMenuClick: () -> Unit = {},
    menuExpanded: Boolean = false,
    onDismissMenu: () -> Unit = {},
    menuContent: @Composable ColumnScope.() -> Unit = {}
) {
    val isLightTheme = !isSystemInDarkTheme()
    val containerColor = if (overlayColor != Color.Unspecified) {
        overlayColor
    } else if (isLightTheme) {
        Color(0xFFFAFAFA).copy(alpha = 0.4f)
    } else {
        Color(0xFF121212).copy(alpha = 0.4f)
    }
    
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val isBlurSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    
    // Menu icon animations
    var iconScale by remember { mutableStateOf(1f) }
    var iconRotation by remember { mutableStateOf(0f) }
    
    val animatedIconScale by animateFloatAsState(
        targetValue = iconScale,
        animationSpec = tween(200, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)),
        label = "menu_icon_scale"
    )
    
    val animatedIconRotation by animateFloatAsState(
        targetValue = iconRotation,
        animationSpec = tween(300, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)),
        label = "menu_icon_rotation"
    )
    
    LaunchedEffect(menuExpanded) {
        if (menuExpanded) {
            iconRotation = 180f
            iconScale = 0.8f
            delay(100)
            iconScale = 1f
        } else {
            iconRotation = 0f
            iconScale = 0.8f
            delay(100)
            iconScale = 1f
        }
    }

    // Container Box for two separate capsules (using Box to prevent layout shifts when menu expands)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = statusBarPadding + 8.dp, start = 16.dp, end = 16.dp)
    ) {
        // 点击外部关闭菜单的遮罩层
        if (menuExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismissMenu
                    )
                    // 使用 zIndex 确保它在最底层（但在其他内容之上，因为整个 TopBar 浮在内容上）
                    // 实际上 TopBar 组件也是 Box，这里我们希望它铺满屏幕
                    // 但是注意 padding。这里是在 padding 内部。需要把 fillMaxSize 放到 padding 外部吗？
                    // BlurBar 自身 Box 设置了 fillMaxWidth 和 padding。
                    // 这里的 child fillMaxSize 只能填满 padding 后的区域。
                    // 但是 padding 只有 top 和 horizontal。bottom 没有。
                    // 所以它可以向下延伸覆盖整个屏幕。
                    // 只有 top statusBarPadding + 8.dp 区域无法拦截？
                    // 实际上这部分区域一般也不是用户"点击外部"的主要区域。
                    // 我们可以尝试将 Scrim 放在这里。
            )
        }
        // LEFT CAPSULE: Title + Icon
        Box(modifier = Modifier.align(Alignment.TopStart)) {
            LeftTitleCapsule(
                title = title,
                icon = icon,
                backdrop = backdrop,
                containerColor = containerColor,
                blurRadius = blurRadius,
                isBlurSupported = isBlurSupported,
                onClick = onIconClick
            )
        }
        
        // RIGHT CAPSULE: Menu Button
        // Use zIndex to ensure menu expands over other content if needed
        if (showMenu) {
            Box(modifier = Modifier.align(Alignment.TopEnd).zIndex(1f)) {
                RightMenuCapsule(
                    backdrop = backdrop,
                    containerColor = containerColor,
                    blurRadius = blurRadius,
                    isBlurSupported = isBlurSupported,
                    menuExpanded = menuExpanded,
                    onMenuClick = onMenuClick,
                    onDismissMenu = onDismissMenu,
                    animatedIconScale = animatedIconScale,
                    animatedIconRotation = animatedIconRotation,
                    menuContent = menuContent
                )
            }
        }
    }
}

/**
 * Left floating capsule with icon and title
 */
@Composable
private fun LeftTitleCapsule(
    title: String,
    icon: ImageVector,
    backdrop: Backdrop?,
    containerColor: Color,
    blurRadius: Float,
    isBlurSupported: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 400f),
        label = "titleCapsuleScale"
    )
    
    Row(
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
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
                            blur(blurRadius.dp.toPx())
                            lens(
                                refractionHeight = 20f.dp.toPx(),
                                refractionAmount = 20f.dp.toPx(),
                                chromaticAberration = true
                            )
                        },
                        onDrawSurface = { drawRect(containerColor) }
                    )
                } else {
                    Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(containerColor.copy(alpha = 0.5f))
                }
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Crossfade(
            targetState = icon,
            animationSpec = tween(200),
            label = "iconTransition"
        ) { targetIcon ->
            Icon(
                imageVector = targetIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Right floating capsule with menu button - expands into menu when clicked
 */
@Composable
private fun RightMenuCapsule(
    backdrop: Backdrop?,
    containerColor: Color,
    blurRadius: Float,
    isBlurSupported: Boolean,
    menuExpanded: Boolean,
    onMenuClick: () -> Unit,
    onDismissMenu: () -> Unit,
    animatedIconScale: Float,
    animatedIconRotation: Float,
    menuContent: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && !menuExpanded) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 400f),
        label = "menuCapsuleScale"
    )
    
    // 单个胶囊直接展开成菜单
    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(
                if (backdrop != null && isBlurSupported) {
                    val density = LocalDensity.current
                    Modifier.drawBackdrop(
                        backdrop = backdrop,
                        shape = { RoundedCornerShape(20.dp) },
                        effects = {
                            vibrancy()
                            blur(blurRadius.dp.toPx())
                            lens(
                                refractionHeight = 20f.dp.toPx(),
                                refractionAmount = 20f.dp.toPx(),
                                chromaticAberration = true
                            )
                        },
                        onDrawSurface = { drawRect(containerColor) }
                    )
                } else {
                    Modifier
                        .clip(RoundedCornerShape(20.dp)) // Fallback shape
                        .background(containerColor.copy(alpha = 0.5f))
                }
            )
            .animateContentSize(
                animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f)
            )
    ) {
        // 内容区域：根据状态切换
        // 使用 Box 确保内容对齐
        Box(
            contentAlignment = Alignment.TopEnd
        ) {
            if (menuExpanded) {
                // 展开状态：显示菜单内容
                Column(
                    modifier = Modifier
                        .widthIn(min = 120.dp, max = 160.dp)
                        .padding(vertical = 8.dp)
                ) {
                    menuContent()
                }
            } else {
                // 收起状态：显示图标
                Box(
                    modifier = Modifier
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = onMenuClick
                        )
                        .size(48.dp)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "菜单",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .size(24.dp)
                            .scale(animatedIconScale)
                            .rotate(animatedIconRotation)
                    )
                }
            }
        }
    }
    
    // 处理返回键关闭菜单
    BackHandler(enabled = menuExpanded) {
        onDismissMenu()
    }
}
