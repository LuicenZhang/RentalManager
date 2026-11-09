package com.morgen.rentalmanager.ui.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.unit.dp
import com.morgen.rentalmanager.ui.theme.ModernColors
import com.morgen.rentalmanager.ui.theme.ModernElevation
import com.morgen.rentalmanager.ui.theme.ModernGlass

/**
 * 毛玻璃效果顶部栏
 * 在支持的设备上显示模糊背景，否则显示半透明背景
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    // 创建毛玻璃背景容器
    Box(
        modifier = modifier
            .fillMaxWidth()
            // Removed fixed height to support WindowInsets from TopAppBar

    ) {
        // 背景层 - 实现模糊效果
        // 注意：Android Compose原生BackdropFilter支持有限，这里使用半透明背景模拟
        // 如果需要真正的实时模糊，通常需要RenderEffect (Android 12+) 或第三方库
        
        val backgroundColor = if (ModernColors.Surface.luminance() > 0.5f) {
            ModernGlass.GlassLight
        } else {
            ModernGlass.GlassDark
        }
        
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(backgroundColor)
        )
        
        // 底部边框线
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .align(Alignment.BottomCenter)
                .background(ModernColors.OutlineVariant.copy(alpha = 0.5f))
        )
        
        // 内容层
        TopAppBar(
            title = title,
            navigationIcon = navigationIcon,
            actions = actions,
            colors = colors.copy(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent
            ),
            scrollBehavior = scrollBehavior,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * 扩展函数：获取颜色的亮度值
 */
private fun Color.luminance(): Float {
    // 简单的亮度计算
    return (red * 0.299f + green * 0.587f + blue * 0.114f)
}
