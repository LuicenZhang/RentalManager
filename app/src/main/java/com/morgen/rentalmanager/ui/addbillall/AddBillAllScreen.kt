package com.morgen.rentalmanager.ui.addbillall

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.morgen.rentalmanager.TenantApplication
import com.morgen.rentalmanager.ui.theme.ModernColors
import com.morgen.rentalmanager.ui.theme.ModernSpacing
import com.morgen.rentalmanager.ui.theme.ModernIconSize
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.morgen.rentalmanager.ui.components.FrostedBlurTopBar
import com.morgen.rentalmanager.ui.components.LiquidGlassCard
import com.morgen.rentalmanager.ui.components.LiquidGlassButton
import com.morgen.rentalmanager.ui.components.LiquidGlassSegmentedTabs
import com.morgen.rentalmanager.ui.components.TopGradientOverlay
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBillAllScreen(
    navController: NavController,
    onDismiss: () -> Unit = {}
) {
    val context = LocalContext.current
    val application = context.applicationContext as TenantApplication
    val viewModel: AddBillAllViewModel = viewModel(
        factory = AddBillAllViewModelFactory(application.repository)
    )

    val uiState by viewModel.uiState.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    var showMonthPicker by remember { mutableStateOf(false) }
    var currentMeterType by remember { mutableStateOf("water") }

    // Parse selectedMonth (yyyy-MM) to year & month
    val parts = selectedMonth.split("-")
    val initYear = parts[0].toInt()
    val initMonth = parts[1].toInt()
    
    // Backdrop for glass effects
    val topBarBackdrop = rememberLayerBackdrop()
    val cardBackdrop = rememberLayerBackdrop()
    
    // Density and dimensions
    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val topBarHeight = 64.dp
    val controlsHeight = 120.dp // Month selector + tabs
    
    // Collapsing state (matching BillListScreen pattern - TopBar collapses)
    val topBarHeightPx = with(density) { topBarHeight.toPx() }
    var collapseOffsetPx by remember { mutableFloatStateOf(0f) }
    
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newOffset = (collapseOffsetPx - delta).coerceIn(0f, topBarHeightPx)
                collapseOffsetPx = newOffset
                return Offset.Zero
            }
        }
    }
    
    val collapseFraction = if (topBarHeightPx > 0) collapseOffsetPx / topBarHeightPx else 0f
    
    // Dynamic positions (TopBar collapses, controls move up)
    val topBarOffsetY = -topBarHeight * collapseFraction
    val topBarAlpha = 1f - collapseFraction
    
    // Controls position: follows TopBar collapse
    val controlsTopOffset = (statusBarHeight + topBarHeight - (topBarHeight * collapseFraction))
        .coerceAtLeast(statusBarHeight + 8.dp)
    
    // Content padding
    val contentPaddingTop = statusBarHeight + topBarHeight + controlsHeight + ModernSpacing.Medium

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
    ) {
        // Content layer wrapped in layerBackdrop
        Box(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(topBarBackdrop)
        ) {
            // Background layer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .layerBackdrop(cardBackdrop)
                    .background(ModernColors.Background)
            )
            
            // Main scrollable content
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = contentPaddingTop,
                    bottom = 100.dp,
                    start = ModernSpacing.Medium,
                    end = ModernSpacing.Medium
                ),
                verticalArrangement = Arrangement.spacedBy(ModernSpacing.Medium)
            ) {
                if (uiState.isEmpty()) {
                    item {
                        EmptyStateCard(cardBackdrop)
                    }
                } else {
                    items(
                        items = uiState,
                        key = { state -> state.roomNumber }
                    ) { state ->
                        GlassTenantBillInputCard(
                            state = state,
                            meterType = currentMeterType,
                            viewModel = viewModel,
                            backdrop = cardBackdrop
                        )
                    }
                }
            }
        }
        
        // Top gradient overlay for smooth status bar transition
        TopGradientOverlay(
            modifier = Modifier.align(Alignment.TopCenter)
        )
        
        // Collapsible TopBar
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset { IntOffset(0, topBarOffsetY.roundToPx()) }
                .graphicsLayer { alpha = topBarAlpha }
                .zIndex(2f)
        ) {
            FrostedBlurTopBar(
                title = "批量添加账单",
                backdrop = topBarBackdrop,
                overlayColor = Color.Transparent,
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                onIconClick = {
                    onDismiss()
                    try {
                        if (navController.previousBackStackEntry != null) {
                            navController.popBackStack()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                showMenu = false
            )
        }
        
        // Fixed controls section (moves up as TopBar collapses)
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset { IntOffset(0, controlsTopOffset.roundToPx()) }
                .fillMaxWidth()
                .padding(horizontal = ModernSpacing.Medium)
                .zIndex(1f),
            verticalArrangement = Arrangement.spacedBy(ModernSpacing.Small)
        ) {
            // Month selector button with glass effect
            LiquidGlassButton(
                onClick = { showMonthPicker = true },
                backdrop = topBarBackdrop,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "📅 账单月份: $selectedMonth",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = ModernColors.Primary
                )
            }
            
            // Glass segmented tabs
            val tabItems = listOf("水费", "电费", "附加费")
            val tabKeys = listOf("water", "electricity", "extra")
            val selectedTabIndex = tabKeys.indexOf(currentMeterType).coerceAtLeast(0)
            
            LiquidGlassSegmentedTabs(
                items = tabItems,
                selectedIndex = selectedTabIndex,
                onTabSelected = { index ->
                    currentMeterType = tabKeys[index]
                },
                backdrop = topBarBackdrop
            )
        }
        
        // Floating save button at bottom - 在 topBarBackdrop 层外，使用 topBarBackdrop 模糊
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(ModernSpacing.Medium)
                .zIndex(1f)
        ) {
            LiquidGlassButton(
                onClick = {
                    viewModel.saveBills()
                    Toast.makeText(context, "账单已保存", Toast.LENGTH_SHORT).show()
                    onDismiss()
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    }
                },
                backdrop = topBarBackdrop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.size(ModernIconSize.Medium),
                    tint = ModernColors.Primary
                )
                Spacer(modifier = Modifier.width(ModernSpacing.Small))
                Text(
                    "全部保存",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = ModernColors.Primary
                )
            }
        }
        
        // Month picker overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(5f)
        ) {
            com.morgen.rentalmanager.ui.components.ModernMonthYearPicker(
                show = showMonthPicker,
                initialYear = initYear,
                initialMonth = initMonth,
                onDismiss = { showMonthPicker = false },
                onConfirm = { year, month ->
                    viewModel.onMonthSelected(year, month)
                    showMonthPicker = false
                },
                backdrop = topBarBackdrop
            )
        }
    }
}

/**
 * Empty state card
 */
@Composable
private fun EmptyStateCard(backdrop: LayerBackdrop) {
    LiquidGlassCard(
        backdrop = backdrop,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ModernSpacing.XLarge),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(ModernSpacing.Small)
            ) {
                Text(
                    text = "📋",
                    style = MaterialTheme.typography.displayMedium
                )
                Text(
                    text = "暂无租户信息",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = ModernColors.OnSurface
                )
                Text(
                    text = "请先添加租户后再录入账单",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ModernColors.OnSurfaceVariant
                )
            }
        }
    }
}

/**
 * Glass-styled tenant bill input card
 */
@Composable
private fun GlassTenantBillInputCard(
    state: BillInputState,
    meterType: String,
    viewModel: AddBillAllViewModel,
    backdrop: LayerBackdrop
) {
    LiquidGlassCard(
        backdrop = backdrop,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ModernSpacing.Medium),
            verticalArrangement = Arrangement.spacedBy(ModernSpacing.Medium)
        ) {
            // Tenant header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = state.roomNumber,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ModernColors.OnSurface
                    )
                    Text(
                        text = state.tenantName,
                        style = MaterialTheme.typography.bodySmall,
                        color = ModernColors.OnSurfaceVariant
                    )
                }
                
                // Status indicator
                Surface(
                    color = ModernColors.Primary.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = when(meterType) {
                            "water" -> "💧 水费"
                            "electricity" -> "⚡ 电费"
                            else -> "📝 附加费"
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = ModernColors.Primary
                    )
                }
            }
            
            HorizontalDivider(color = ModernColors.OnSurface.copy(alpha = 0.1f))
            
            // Content based on meter type
            when (meterType) {
                "water", "electricity" -> {
                    // Filter meters by type
                    val meters = state.meters.filter { it.type == meterType }
                    if (meters.isEmpty()) {
                        Text(
                            text = "暂无${if (meterType == "water") "水表" else "电表"}数据",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ModernColors.OnSurfaceVariant
                        )
                    } else {
                        meters.forEach { meter ->
                            MeterInputCard(
                                meter = meter,
                                roomNumber = state.roomNumber,
                                viewModel = viewModel
                            )
                        }
                    }
                    
                    // Add meter button
                    TextButton(
                        onClick = { viewModel.addMeter(state.roomNumber, meterType) }
                    ) {
                        Text("➕ 添加${if (meterType == "water") "水表" else "电表"}")
                    }
                }
                "extra" -> {
                    ExtraFeeSection(
                        extraFees = state.extraFees,
                        roomNumber = state.roomNumber,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

/**
 * Single meter input card
 */
@Composable
private fun MeterInputCard(
    meter: MeterInput,
    roomNumber: String,
    viewModel: AddBillAllViewModel
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ModernSpacing.Small)
    ) {
        // Meter name
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = meter.name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = ModernColors.OnSurface
            )
            if (!meter.isPrimary) {
                TextButton(
                    onClick = { viewModel.removeMeter(roomNumber, meter.id) }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        
        // Readings row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ModernSpacing.Small)
        ) {
            OutlinedTextField(
                value = meter.previousReading,
                onValueChange = { viewModel.onMeterPreviousReadingChange(roomNumber, meter.id, it) },
                label = { Text("上次读数") },
                readOnly = !meter.isPrevEditable,
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium
            )
            OutlinedTextField(
                value = meter.currentReading,
                onValueChange = { viewModel.onMeterReadingChange(roomNumber, meter.id, it) },
                label = { Text("本次读数") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium
            )
        }
        
        // Usage and amount - 可编辑输入框 (带视觉差异)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ModernSpacing.Small)
        ) {
            // 用量输入框 - 使用蓝色调
            OutlinedTextField(
                value = meter.usage,
                onValueChange = { viewModel.onMeterUsageChange(roomNumber, meter.id, it) },
                label = { Text("📊 用量") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2196F3),
                    focusedLabelColor = Color(0xFF2196F3),
                    cursorColor = Color(0xFF2196F3)
                )
            )
            // 金额输入框 - 使用绿色调突出
            OutlinedTextField(
                value = meter.amount,
                onValueChange = { viewModel.onMeterAmountChange(roomNumber, meter.id, it) },
                label = { Text("💰 金额") },
                prefix = { Text("¥", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF4CAF50)
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4CAF50),
                    focusedLabelColor = Color(0xFF4CAF50),
                    cursorColor = Color(0xFF4CAF50)
                )
            )
        }
        
        HorizontalDivider(color = ModernColors.OnSurface.copy(alpha = 0.05f))
    }
}

/**
 * Extra fee section
 */
@Composable
private fun ExtraFeeSection(
    extraFees: List<ExtraFee>,
    roomNumber: String,
    viewModel: AddBillAllViewModel
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(ModernSpacing.Small)
    ) {
        // Existing fees
        extraFees.forEach { fee ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(ModernSpacing.Small)
                ) {
                    OutlinedTextField(
                        value = fee.name,
                        onValueChange = { viewModel.onExtraFeeNameChange(roomNumber, fee.id, it) },
                        label = { Text("费用名称") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = fee.amount,
                        onValueChange = { viewModel.onExtraFeeAmountChange(roomNumber, fee.id, it) },
                        label = { Text("金额") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                IconButton(onClick = { viewModel.removeExtraFee(roomNumber, fee.id) }) {
                    Text("🗑️")
                }
            }
        }
        
        // Add fee button
        TextButton(
            onClick = { viewModel.addExtraFee(roomNumber) }
        ) {
            Text("➕ 添加附加费")
        }
    }
}