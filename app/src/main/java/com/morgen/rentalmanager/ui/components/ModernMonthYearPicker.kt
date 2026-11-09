package com.morgen.rentalmanager.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.morgen.rentalmanager.ui.theme.*
import java.util.Calendar

/**
 * A modern month-year picker dialog with glass blur effect.
 * Uses GlassCenterSheet for the glass/blur background.
 */
@Composable
fun ModernMonthYearPicker(
    show: Boolean,
    initialYear: Int,
    initialMonth: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit,
    backdrop: Backdrop? = null
) {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val years = (currentYear - 5..currentYear + 5).toList()
    val months = (1..12).toList()

    var selectedYear by remember(show) { mutableStateOf(initialYear) }
    var selectedMonth by remember(show) { mutableStateOf(initialMonth) }
    
    var expandedYear by remember { mutableStateOf(false) }
    var expandedMonth by remember { mutableStateOf(false) }
    
    // Use fallback backdrop if none provided
    val effectiveBackdrop = backdrop ?: rememberLayerBackdrop()

    GlassCenterSheet(
        visible = show,
        onDismiss = onDismiss,
        backdrop = effectiveBackdrop
    ) {
        Column(
            modifier = Modifier.padding(ModernSpacing.Large),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "选择月份",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = ModernColors.OnSurface
            )
            
            Spacer(modifier = Modifier.height(ModernSpacing.Large))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Year Picker - Glass styled
                Box {
                    LiquidGlassButton(
                        onClick = { expandedYear = true },
                        backdrop = effectiveBackdrop,
                        shape = RoundedCornerShape(ModernCorners.Medium)
                    ) {
                        Text(
                            "$selectedYear 年",
                            fontWeight = FontWeight.Medium,
                            color = ModernColors.Primary
                        )
                    }
                    DropdownMenu(
                        expanded = expandedYear,
                        onDismissRequest = { expandedYear = false }
                    ) {
                        years.forEach { year ->
                            DropdownMenuItem(
                                text = { Text(year.toString()) },
                                onClick = {
                                    selectedYear = year
                                    expandedYear = false
                                }
                            )
                        }
                    }
                }

                // Month Picker - Glass styled
                Box {
                    LiquidGlassButton(
                        onClick = { expandedMonth = true },
                        backdrop = effectiveBackdrop,
                        shape = RoundedCornerShape(ModernCorners.Medium)
                    ) {
                        Text(
                            "$selectedMonth 月",
                            fontWeight = FontWeight.Medium,
                            color = ModernColors.Primary
                        )
                    }
                    DropdownMenu(
                        expanded = expandedMonth,
                        onDismissRequest = { expandedMonth = false }
                    ) {
                        months.forEach { month ->
                            DropdownMenuItem(
                                text = { Text(month.toString()) },
                                onClick = {
                                    selectedMonth = month
                                    expandedMonth = false
                                }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(ModernSpacing.XLarge))
            
            // Glass styled action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cancel button - Glass styled
                LiquidGlassButton(
                    onClick = onDismiss,
                    backdrop = effectiveBackdrop,
                    shape = RoundedCornerShape(ModernCorners.Medium)
                ) {
                    Text(
                        "取消",
                        fontWeight = FontWeight.Medium,
                        color = ModernColors.OnSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.width(ModernSpacing.Medium))
                
                // Confirm button - Glass styled with primary color
                LiquidGlassButton(
                    onClick = { onConfirm(selectedYear, selectedMonth) },
                    backdrop = effectiveBackdrop,
                    shape = RoundedCornerShape(ModernCorners.Medium)
                ) {
                    Text(
                        "确认",
                        fontWeight = FontWeight.Medium,
                        color = ModernColors.Primary
                    )
                }
            }
        }
    }
}