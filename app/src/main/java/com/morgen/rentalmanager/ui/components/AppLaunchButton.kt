package com.morgen.rentalmanager.ui.components

import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.morgen.rentalmanager.ui.theme.ModernAnimations
import com.morgen.rentalmanager.ui.theme.ModernColors
import com.morgen.rentalmanager.ui.theme.ModernCorners
import kotlinx.coroutines.delay

// 优化的动画曲线
private val EaseOutQuart = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

/**
 * 手机应用风格的启动按钮 - 玻璃效果版本
 * 点击时会执行放大→淡出→导航的动画序列
 */
@Composable
fun AppLaunchButton(
    onClick: () -> Unit,
    navController: NavController,
    route: String,
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    backdrop: Backdrop? = null, // 添加 backdrop 参数
    containerColor: Color = ModernColors.Primary,
    contentColor: Color = Color.White,
    onLaunchStateChange: ((Boolean) -> Unit)? = null,
    onRouteNavigate: (() -> Boolean)? = null
) {
    var isLaunching by remember { mutableStateOf(false) }
    var buttonVisible by remember { mutableStateOf(true) }
    var isPressed by remember { mutableStateOf(false) }
    
    val isLightTheme = !isSystemInDarkTheme()
    val isBlurSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    
    // 玻璃效果背景色
    val glassContainerColor = if (isLightTheme) {
        Color(0xFFFAFAFA).copy(alpha = 0.6f)
    } else {
        Color(0xFF1E1E1E).copy(alpha = 0.6f)
    }
    
    val fallbackBackground = if (isLightTheme) {
        Color(0xFFFAFAFA).copy(alpha = 0.85f)
    } else {
        Color(0xFF2A2A2A).copy(alpha = 0.85f)
    }
    
    val glassBorder = if (isLightTheme) {
        Color.White.copy(alpha = 0.5f)
    } else {
        Color.White.copy(alpha = 0.2f)
    }
    
    // 点击反馈动画
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f),
        label = "button_scale"
    )
    
    // 按钮透明度动画
    val buttonAlpha by animateFloatAsState(
        targetValue = when {
            isLaunching -> 0f
            buttonVisible -> 1f
            else -> 0f
        },
        animationSpec = tween(
            durationMillis = 200,
            easing = EaseOutQuart
        ),
        label = "button_alpha"
    )
    
    // 启动动画完成后的处理
    LaunchedEffect(isLaunching) {
        if (isLaunching) {
            onLaunchStateChange?.invoke(true)
            val shouldUseDefaultNavigation = onRouteNavigate?.invoke() ?: true
            delay(200)
            if (shouldUseDefaultNavigation) {
                navController.navigate(route)
            }
            delay(50)
            onLaunchStateChange?.invoke(false)
            delay(100)
            isLaunching = false
            buttonVisible = true
        }
    }
    
    // 点击效果重置
    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(150)
            isPressed = false
        }
    }
    
    // 组件销毁时确保状态重置
    DisposableEffect(Unit) {
        onDispose {
            onLaunchStateChange?.invoke(false)
        }
    }
    
    val interactionSource = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(ModernCorners.Large)
    
    // 使用玻璃效果的按钮样式
    Box(
        modifier = modifier
            .height(48.dp)
            .scale(scale)
            .alpha(buttonAlpha)
            .then(
                if (backdrop != null && isBlurSupported) {
                    Modifier.drawBackdrop(
                        backdrop = backdrop,
                        shape = { shape },
                        effects = {
                            vibrancy()
                            blur(12f.dp.toPx())
                            lens(20f.dp.toPx(), 20f.dp.toPx())
                        },
                        onDrawSurface = { drawRect(glassContainerColor) }
                    )
                } else {
                    Modifier
                        .clip(shape)
                        .background(fallbackBackground)
                        .border(
                            width = 1.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(glassBorder, glassBorder.copy(alpha = 0.1f))
                            ),
                            shape = shape
                        )
                }
            )
            .interactiveHighlight(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = !isLaunching
            ) {
                isPressed = true
                onClick()
                isLaunching = true
            }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
