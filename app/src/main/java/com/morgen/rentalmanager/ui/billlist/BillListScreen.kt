package com.morgen.rentalmanager.ui.billlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.morgen.rentalmanager.myapplication.TenantRepository
import com.morgen.rentalmanager.ui.theme.*
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.morgen.rentalmanager.ui.components.FrostedBlurTopBar
import com.morgen.rentalmanager.ui.components.LiquidGlassCard
import com.morgen.rentalmanager.ui.components.ModernMonthYearPicker
import com.kyant.backdrop.Backdrop
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.statusBars
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt


/**
 * 账单列表主界面组件
 * 协调各个子组件显示，集成ViewModel和状态管理，添加加载状态和错误处理UI
 * 
 * 需求: 7.1, 7.2, 7.3, 7.4 - 数据加载和错误处理
 * 
 * @param navController 导航控制器
 * @param repository 租户数据仓库
 * @param modifier 修饰符
 */
@Composable
fun BillListScreen(
    navController: NavController,
    repository: TenantRepository? = null,
    modifier: Modifier = Modifier
) {
    // 获取ViewModel实例
    val viewModel: BillListViewModel = if (repository != null) {
        viewModel(factory = BillListViewModelFactory(repository))
    } else {
        // 如果没有提供repository，使用默认的ViewModel（用于预览和测试）
        viewModel()
    }
    
    // 收集状态
    val uiState by viewModel.uiState.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    
    // Create Backdrops
    val cardBackdrop = rememberLayerBackdrop()

    // 主界面内容
    Box(
        modifier = modifier.fillMaxSize()
    ) {

        
        // Content layer
        // Content layer
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            BillListScreenContent(
                uiState = uiState,
                selectedMonth = selectedMonth,
                isRefreshing = isRefreshing,
                onMonthSelected = viewModel::selectMonth,
                onRefresh = viewModel::refreshData,
                onRetry = viewModel::retryLoadData,
                onBillClick = { roomNumber, month ->
                    // 导航到账单详情页面
                    try {
                        if (roomNumber.isBlank() || month.isBlank()) return@BillListScreenContent
                        val encodedRoomNumber = java.net.URLEncoder.encode(roomNumber, "UTF-8")
                        val encodedMonth = java.net.URLEncoder.encode(month, "UTF-8")
                        navController.navigate("bill_detail/$encodedRoomNumber/$encodedMonth")
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                cardBackdrop = cardBackdrop,
                navController = navController
            )
        }
    }
}

@Composable
private fun BillListScreenContent(
    uiState: BillListUiState,
    selectedMonth: String,
    isRefreshing: Boolean,
    onMonthSelected: (String) -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onBillClick: (String, String) -> Unit,
    cardBackdrop: LayerBackdrop,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val topBarBackdrop = rememberLayerBackdrop()
    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    
    // Collapsing state
    val topBarHeight = 64.dp
    val topBarHeightPx = with(density) { topBarHeight.toPx() }
    var collapseOffsetPx by remember { mutableFloatStateOf(0f) }
    
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                // Determine new offset:
                // If delta < 0 (scrolling down), we increase collapseOffset (hide bar)
                // If delta > 0 (scrolling up), we decrease collapseOffset (show bar)
                val newOffset = (collapseOffsetPx - delta).coerceIn(0f, topBarHeightPx)
                
                // We consume the delta only if we are moving the bar?
                // For Quick Return, valid pattern is to consume none and let list scroll too, OR consume.
                // If we consume, list stops.
                // Let's NOT consume, so list scrolls simultaneously with bar hiding.
                collapseOffsetPx = newOffset
                return Offset.Zero 
            }
        }
    }
    
    val collapseFraction = if (topBarHeightPx > 0) collapseOffsetPx / topBarHeightPx else 0f
    
    // Dynamic positions
    val topBarOffsetY = -topBarHeight * collapseFraction
    val topBarAlpha = 1f - collapseFraction
    
    // MonthSelector moves up as TopBar collapses
    // Initial top: statusBar + topBarHeight
    // Final top: statusBar
    // Just place it below the (moving) top bar, but clamped to statusBar
    val monthSelectorTopOffset = (statusBarHeight + topBarHeight - (topBarHeight * collapseFraction)).coerceAtLeast(statusBarHeight + 8.dp)

    // Content padding: Space for Headers (TopBar + MonthSelector)
    // Initial occupied height: statusBar + topBarHeight + MonthSelectorHeight(~80dp including padding)
    val contentPaddingTop = statusBarHeight + topBarHeight + 80.dp + ModernSpacing.Medium

    // State for Month Picker
    var showMonthPicker by remember { mutableStateOf(false) }

    // Helper for parsing current month (simple version)
    val (currentYear, currentMonth) = remember(selectedMonth) {
        try {
            if (selectedMonth.contains("-")) {
                val parts = selectedMonth.split("-")
                (parts[0].toInt() to parts[1].toInt())
            } else {
                val cal = java.util.Calendar.getInstance()
                (cal.get(java.util.Calendar.YEAR) to cal.get(java.util.Calendar.MONTH) + 1)
            }
        } catch (e: Exception) {
            val cal = java.util.Calendar.getInstance()
            (cal.get(java.util.Calendar.YEAR) to cal.get(java.util.Calendar.MONTH) + 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
    ) {
        // 主要内容区域
        Box(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(topBarBackdrop)
        ) {
            // Background layer moved here to be captured by topBarBackdrop
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .layerBackdrop(cardBackdrop)
                    .background(ModernColors.Background)
            )

            when (uiState) {
                is BillListUiState.Loading -> {
                    LoadingContent(modifier = Modifier.fillMaxSize())
                }
                is BillListUiState.Success -> {
                    SuccessContent(
                        uiState = uiState,
                        onBillClick = onBillClick,
                        cardBackdrop = cardBackdrop,
                        selectedMonth = selectedMonth,
                        onMonthSelected = onMonthSelected,
                        topPadding = contentPaddingTop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is BillListUiState.Error -> {
                    ErrorContent(
                        error = uiState,
                        onRetry = onRetry,
                        modifier = Modifier.fillMaxSize().padding(top = contentPaddingTop)
                    )
                }
                is BillListUiState.Empty -> {
                    EmptyContent(
                        emptyState = uiState,
                        modifier = Modifier.fillMaxSize().padding(top = contentPaddingTop)
                    )
                }
            }
        }
        
        // Top gradient overlay for smooth status bar transition
        com.morgen.rentalmanager.ui.components.TopGradientOverlay(
            modifier = Modifier.align(Alignment.TopCenter)
        )
        
        // 顶部栏 (Collapsible)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset { IntOffset(0, topBarOffsetY.roundToPx()) }
                .graphicsLayer { alpha = topBarAlpha }
                .zIndex(2f)
        ) {
            FrostedBlurTopBar(
                title = "账单列表",
                backdrop = topBarBackdrop,
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                onIconClick = { navController.popBackStack() },
                showMenu = false, // Disable menu
                menuContent = {},
                onMenuClick = {}
            )
        }
        
        // Month Selector Button (Fixed/Sticky)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset { IntOffset(0, monthSelectorTopOffset.roundToPx()) }
                .padding(horizontal = ModernSpacing.Medium)
                .fillMaxWidth()
                .zIndex(1f) // Below TopBar (visually, when overlapping)
        ) {
             MonthSelector(
                 selectedMonth = selectedMonth,
                 onClick = { showMonthPicker = true },
                 backdrop = topBarBackdrop
             )
        }
        
        // Full Screen Month Picker Overlay (High Z-Index to cover TopBar and MonthSelector)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(3f)
        ) {
            ModernMonthYearPicker(
                show = showMonthPicker,
                initialYear = currentYear,
                initialMonth = currentMonth,
                onDismiss = { showMonthPicker = false },
                onConfirm = { year, month ->
                    val monthString = "$year-${String.format("%02d", month)}"
                    onMonthSelected(monthString)
                    showMonthPicker = false
                },
                backdrop = topBarBackdrop
            )
        }
    }
}

/**
 * 顶部栏组件
 * 包含标题、月份选择器和刷新按钮
 */
// Removed BillListTopBar as it is replaced by FrostedBlurTopBar and in-content MonthSelector

/**
 * 加载状态内容
 * 需求: 7.1 - 数据加载时显示加载指示器
 */
@Composable
private fun LoadingContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ModernSpacing.Medium)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = ModernColors.Primary,
                strokeWidth = 4.dp
            )
            
            Text(
                text = "正在加载账单数据...",
                style = MaterialTheme.typography.bodyMedium,
                color = ModernColors.OnSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 成功状态内容
 * 显示账单汇总、图表和列表 - 修复嵌套滚动问题
 */
@Composable
private fun SuccessContent(
    uiState: BillListUiState.Success,
    onBillClick: (String, String) -> Unit,
    cardBackdrop: Backdrop,
    selectedMonth: String,
    onMonthSelected: (String) -> Unit,
    topPadding: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    // 使用LazyColumn统一管理所有内容，避免嵌套滚动
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = topPadding,
            bottom = ModernSpacing.Medium,
            start = ModernSpacing.Medium,
            end = ModernSpacing.Medium
        ),
        verticalArrangement = Arrangement.spacedBy(ModernSpacing.Medium)
    ) {
        // Month Selector moved to BillListScreenContent overlay
        // item {
        //     MonthSelector(...)
        // }

        // 账单汇总卡片
        item {
            BillSummaryCard(
                summary = uiState.summary,
                backdrop = cardBackdrop
            )
        }
        
        // 费用分布图表卡片
        if (uiState.hasChartData()) {
            item {
                BillChartCard(
                    chartData = uiState.chartData,
                    backdrop = cardBackdrop
                )
            }
        }
        
        // 账单列表标题
        if (uiState.hasBills()) {
            item {
                Text(
                    text = "账单详情",
                    style = MaterialTheme.typography.titleMedium,
                    color = ModernColors.OnSurface,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = ModernSpacing.Small)
                )
            }
            
            // 账单列表项
            items(
                items = uiState.bills.filter { bill ->
                    try {
                        bill.isValid() && bill.roomNumber.isNotBlank()
                    } catch (e: Exception) {
                        android.util.Log.w("SuccessContent", "账单数据无效", e)
                        false
                    }
                },
                key = { bill -> 
                    try {
                        "${bill.roomNumber}_${bill.tenantName}_${uiState.selectedMonth}"
                    } catch (e: Exception) {
                        bill.hashCode()
                    }
                }
            ) { bill ->
                BillItemCard(
                    billItem = bill,
                    backdrop = cardBackdrop,
                    onClick = { 
                        try {
                            onBillClick(bill.roomNumber, uiState.selectedMonth)
                        } catch (e: Exception) {
                            android.util.Log.e("SuccessContent", "点击处理失败", e)
                        }
                    }
                )
            }
        } else {
            // 如果有汇总数据但没有账单列表，显示提示
            item {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backdrop = cardBackdrop,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(ModernCorners.Medium)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(ModernSpacing.Large),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "暂无详细账单数据",
                            style = MaterialTheme.typography.titleMedium,
                            color = ModernColors.OnSurface,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(ModernSpacing.Small))
                        
                        Text(
                            text = "仅显示汇总统计信息",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ModernColors.OnSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

/**
 * 错误状态内容
 * 需求: 7.2 - 数据加载失败时显示错误提示信息和重试选项
 */
@Composable
private fun ErrorContent(
    error: BillListUiState.Error,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ModernSpacing.Large),
            colors = CardDefaults.cardColors(
                containerColor = ModernColors.ErrorContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(ModernSpacing.Large),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(ModernSpacing.Medium)
            ) {
                Text(
                    text = "数据加载失败",
                    style = MaterialTheme.typography.titleMedium,
                    color = ModernColors.OnErrorContainer,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = error.getUserFriendlyMessage(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ModernColors.OnErrorContainer,
                    textAlign = TextAlign.Center
                )
                
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ModernColors.Error,
                        contentColor = ModernColors.OnError
                    )
                ) {
                    Text("重试")
                }
                
                // 根据错误类型显示额外提示
                when {
                    error.isNetworkError() -> {
                        Text(
                            text = "请检查网络连接后重试",
                            style = MaterialTheme.typography.bodySmall,
                            color = ModernColors.OnErrorContainer.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    error.isDatabaseError() -> {
                        Text(
                            text = "数据库访问异常，请稍后重试",
                            style = MaterialTheme.typography.bodySmall,
                            color = ModernColors.OnErrorContainer.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

/**
 * 空状态内容
 * 需求: 7.4 - 数据为空时显示友好的空状态界面
 */
@Composable
private fun EmptyContent(
    emptyState: BillListUiState.Empty,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ModernSpacing.Large),
            colors = CardDefaults.cardColors(
                containerColor = ModernColors.SurfaceContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(ModernSpacing.Large),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(ModernSpacing.Medium)
            ) {
                Text(
                    text = emptyState.getEmptyMessage(),
                    style = MaterialTheme.typography.titleMedium,
                    color = ModernColors.OnSurface,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "当前月份还没有生成账单\n请先添加租户信息和费用数据",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ModernColors.OnSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
} 
/**

 * 错误账单项占位符
 * 当单个账单项渲染失败时显示
 */
@Composable
private fun ErrorBillItemPlaceholder(
    roomNumber: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = ModernColors.SurfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ModernSpacing.Medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "房间 $roomNumber 数据异常",
                style = MaterialTheme.typography.bodyMedium,
                color = ModernColors.OnSurfaceVariant
            )
            Text(
                text = "请刷新重试",
                style = MaterialTheme.typography.bodySmall,
                color = ModernColors.OnSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}