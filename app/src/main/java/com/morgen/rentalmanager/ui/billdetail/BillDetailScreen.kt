package com.morgen.rentalmanager.ui.billdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.morgen.rentalmanager.TenantApplication
import com.morgen.rentalmanager.myapplication.TenantRepository
import com.morgen.rentalmanager.ui.components.FrostedBlurTopBar
import com.morgen.rentalmanager.ui.components.LiquidGlassCard
import com.morgen.rentalmanager.ui.theme.*

/**
 * 账单详情页面
 * 显示特定租户特定月份的详细账单信息
 * 
 * 需求: 5.1, 5.2, 5.3, 5.4 - 账单详情导航和显示
 * 
 * @param navController 导航控制器
 * @param roomNumber 房间号
 * @param month 月份
 * @param repository 租户数据仓库
 * @param modifier 修饰符
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillDetailScreen(
    navController: NavController,
    roomNumber: String,
    month: String,
    repository: TenantRepository? = null,
    modifier: Modifier = Modifier
) {
    // ... ViewModel setup ...
    val viewModel: BillDetailViewModel = if (repository != null) {
        viewModel(factory = BillDetailViewModelFactory(repository))
    } else {
        val context = LocalContext.current
        val application = context.applicationContext as TenantApplication
        viewModel(factory = BillDetailViewModelFactory(application.repository))
    }
    
    // 初始化数据加载
    LaunchedEffect(roomNumber, month) {
        viewModel.loadBillDetail(roomNumber, month)
    }
    
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val topContentPadding = statusBarHeight + 80.dp // 80dp to clear the floating pill (56dp + padding)

    // 收集状态
    val uiState by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    // Backdrop for glass effects
    val topBarBackdrop = rememberLayerBackdrop()
    val cardBackdrop = rememberLayerBackdrop()
    
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Content layer wrapped in layerBackdrop so TopBar can blur it
        Box(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(topBarBackdrop)
        ) {
            // Background layer captured by cardBackdrop
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .layerBackdrop(cardBackdrop)
                    .background(ModernColors.Background)
            )
            
            // Scrollable content
            when (uiState) {
                is BillDetailUiState.Loading -> {
                    LoadingContent(
                        topPadding = topContentPadding,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                is BillDetailUiState.Success -> {
                    val successState = uiState as BillDetailUiState.Success
                    SuccessContent(
                        billDetail = successState.billDetail,
                        cardBackdrop = cardBackdrop,
                        topPadding = topContentPadding,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                is BillDetailUiState.Error -> {
                    val errorState = uiState as BillDetailUiState.Error
                    ErrorContent(
                        error = errorState,
                        onRetry = { 
                            viewModel.loadBillDetail(roomNumber, month)
                        },
                        onNavigateBack = { navController.popBackStack() },
                        cardBackdrop = cardBackdrop,
                        topPadding = topContentPadding,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                is BillDetailUiState.NotFound -> {
                    NotFoundContent(
                        roomNumber = roomNumber,
                        month = month,
                        onNavigateBack = { navController.popBackStack() },
                        cardBackdrop = cardBackdrop,
                        topPadding = topContentPadding,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
        
        // Top gradient overlay for smooth status bar transition
        com.morgen.rentalmanager.ui.components.TopGradientOverlay(
            modifier = Modifier.align(Alignment.TopCenter)
        )
        
        // 玻璃顶栏 - Floating above content
        FrostedBlurTopBar(
            title = "账单详情",
            backdrop = topBarBackdrop,
            overlayColor = Color.Transparent,
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            onIconClick = {
                try {
                    navController.popBackStack()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            showMenu = false
        )
    }
}

/**
 * 加载状态内容
 */
@Composable
private fun LoadingContent(
    topPadding: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(top = topPadding),
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
                text = "正在加载账单详情...",
                style = MaterialTheme.typography.bodyMedium,
                color = ModernColors.OnSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 成功状态内容
 */
@Composable
private fun SuccessContent(
    billDetail: BillDetailData,
    cardBackdrop: Backdrop,
    topPadding: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(top = topPadding, start = ModernSpacing.Medium, end = ModernSpacing.Medium, bottom = ModernSpacing.Medium),
        verticalArrangement = Arrangement.spacedBy(ModernSpacing.Medium)
    ) {
        // 账单时间头部
        Text(
            text = "${billDetail.month}月",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = ModernColors.OnSurface
        )
        
        // 租户基本信息卡片
        TenantInfoCard(billDetail = billDetail, backdrop = cardBackdrop)
        
        // 账单汇总卡片
        BillSummaryDetailCard(billDetail = billDetail, backdrop = cardBackdrop)
        
        // 费用明细卡片
        if (billDetail.hasDetails()) {
            BillDetailsCard(billDetail = billDetail, backdrop = cardBackdrop)
        }
        
        // Bottom padding for navigation bar
        Spacer(modifier = Modifier.height(80.dp))
    }
}

/**
 * 错误状态内容
 */
@Composable
private fun ErrorContent(
    error: BillDetailUiState.Error,
    onRetry: () -> Unit,
    onNavigateBack: () -> Unit,
    cardBackdrop: Backdrop,
    topPadding: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(top = topPadding),
        contentAlignment = Alignment.Center
    ) {
        LiquidGlassCard(
            backdrop = cardBackdrop,
            tintColor = ModernColors.Error,
            modifier = Modifier
                .fillMaxWidth()
                .padding(ModernSpacing.Large)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(ModernSpacing.Large),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(ModernSpacing.Medium)
            ) {
                Text(
                    text = "加载失败",
                    style = MaterialTheme.typography.titleMedium,
                    color = ModernColors.Error,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = error.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ModernColors.OnSurface,
                    textAlign = TextAlign.Center
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(ModernSpacing.Medium)
                ) {
                    OutlinedButton(
                        onClick = onNavigateBack,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ModernColors.OnSurface
                        )
                    ) {
                        Text("返回列表")
                    }
                    
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ModernColors.Error,
                            contentColor = ModernColors.OnError
                        )
                    ) {
                        Text("重试")
                    }
                }
            }
        }
    }
}

/**
 * 未找到状态内容
 */
@Composable
private fun NotFoundContent(
    roomNumber: String,
    month: String,
    onNavigateBack: () -> Unit,
    cardBackdrop: Backdrop,
    topPadding: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(top = topPadding),
        contentAlignment = Alignment.Center
    ) {
        LiquidGlassCard(
            backdrop = cardBackdrop,
            modifier = Modifier
                .fillMaxWidth()
                .padding(ModernSpacing.Large)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(ModernSpacing.Large),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(ModernSpacing.Medium)
            ) {
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = ModernColors.OnSurfaceVariant.copy(alpha = 0.6f)
                )
                
                Text(
                    text = "账单不存在",
                    style = MaterialTheme.typography.titleMedium,
                    color = ModernColors.OnSurface,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "未找到房间 $roomNumber 在 ${month}月 的账单信息",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ModernColors.OnSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                Button(
                    onClick = onNavigateBack,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ModernColors.Primary,
                        contentColor = ModernColors.OnPrimary
                    )
                ) {
                    Text("返回账单列表")
                }
            }
        }
    }
}