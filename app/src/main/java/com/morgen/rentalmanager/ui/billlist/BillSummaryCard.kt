package com.morgen.rentalmanager.ui.billlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.morgen.rentalmanager.ui.components.LiquidGlassCard
import com.kyant.backdrop.Backdrop
import androidx.compose.foundation.shape.RoundedCornerShape
import com.morgen.rentalmanager.ui.theme.*

/**
 * 账单汇总卡片组件
 * 使用渐变背景和现代化布局显示统计数据
 * 
 * @param summary 账单汇总数据
 * @param modifier 修饰符
 */
@Composable
fun BillSummaryCard(
    summary: BillSummary,
    backdrop: Backdrop,
    modifier: Modifier = Modifier
) {
    // 根据深色模式选择渐变
    val isLight = ModernColors.Surface.luminance() > 0.5f
    val gradientBrush = if (isLight) {
        ModernGradients.CardBlue
    } else {
        ModernGradients.CardDark
    }
    
    LiquidGlassCard(
        modifier = modifier.fillMaxWidth(),
        backdrop = backdrop,
        shape = RoundedCornerShape(ModernCorners.Medium)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = gradientBrush)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(ModernSpacing.Large),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "本月总收入",
                    style = MaterialTheme.typography.titleMedium,
                    color = ModernColors.OnSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(ModernSpacing.Small))
                
                Text(
                    text = "¥${summary.getFormattedTotalAmount()}",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1).sp
                    ),
                    color = ModernColors.Primary
                )
                
                Spacer(modifier = Modifier.height(ModernSpacing.Medium))
                
                // 统计详情行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SummaryItem(
                        label = "租金",
                        value = "¥${summary.getFormattedTotalRent()}",
                        color = ModernColors.OnSurface
                    )
                    
                    SummaryDivider()
                    
                    SummaryItem(
                        label = "水电费",
                        value = "¥${String.format("%.2f", summary.totalWater + summary.totalElectricity)}",
                        color = ModernColors.OnSurface
                    )
                    
                    SummaryDivider()
                    
                    SummaryItem(
                        label = "账单数",
                        value = "${summary.totalBills}",
                        color = ModernColors.OnSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = ModernColors.OnSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SummaryDivider() {
    Box(
        modifier = Modifier
            .height(24.dp)
            .width(1.dp)
            .background(ModernColors.OutlineVariant)
    )
}

/**
 * 扩展函数：获取颜色的亮度值
 */
private fun Color.luminance(): Float {
    return (red * 0.299f + green * 0.587f + blue * 0.114f)
}