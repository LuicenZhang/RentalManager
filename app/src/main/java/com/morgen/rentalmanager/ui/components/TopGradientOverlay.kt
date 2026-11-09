package com.morgen.rentalmanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.morgen.rentalmanager.ui.theme.ModernColors

/**
 * 顶部渐变遮罩组件
 * 用于滚动页面顶部，实现状态栏过渡效果
 */
@Composable
fun TopGradientOverlay(
    modifier: Modifier = Modifier,
    height: Dp = 72.dp,
    backgroundColor: Color = ModernColors.Background
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor,
                        backgroundColor.copy(alpha = 0f)
                    )
                )
            )
    )
}
