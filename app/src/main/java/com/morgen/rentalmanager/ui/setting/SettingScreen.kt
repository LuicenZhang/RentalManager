package com.morgen.rentalmanager.ui.setting

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.morgen.rentalmanager.TenantApplication
import com.morgen.rentalmanager.ui.theme.ModernColors
import com.morgen.rentalmanager.ui.theme.ModernElevation
import com.morgen.rentalmanager.ui.theme.ModernSpacing
import com.morgen.rentalmanager.ui.theme.ModernCorners
import com.morgen.rentalmanager.ui.theme.ModernIconSize
import com.morgen.rentalmanager.ui.theme.ModernTouchTarget
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.morgen.rentalmanager.ui.components.FrostedBlurTopBar
import com.morgen.rentalmanager.ui.components.LiquidGlassCard
import com.morgen.rentalmanager.ui.components.LiquidGlassButton

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingScreen(navController: NavController){
    val app = LocalContext.current.applicationContext as TenantApplication
    val vm: SettingViewModel = viewModel(factory = SettingVMFactory(app.repository))
    val water by vm.water.collectAsState()
    val elec by vm.electricity.collectAsState()
    val privacyKeywords by vm.privacyKeywords.collectAsState()

    var waterTxt by remember(water){ mutableStateOf(water.toString()) }
    var elecTxt by remember(elec){ mutableStateOf(elec.toString()) }
    var newKeyword by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }
    
    // Backdrop for glass effects
    val topBarBackdrop = rememberLayerBackdrop()
    val cardBackdrop = rememberLayerBackdrop()
    
    // Calculate padding for content
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val topContentPadding = statusBarHeight + 80.dp // Space for floating capsule top bar

    Box(
        modifier = Modifier.fillMaxSize()
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
            
            // 创建滚动状态以检测滚动位置
            val scrollState = rememberLazyListState()
            
            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = ModernSpacing.Medium),
                contentPadding = PaddingValues(
                    top = topContentPadding,
                    bottom = 80.dp // Bottom padding for navigation bar
                ),
                verticalArrangement = Arrangement.spacedBy(ModernSpacing.Large)
            ) {
                // 价格配置区域
                item {
                    LiquidGlassCard(
                        backdrop = cardBackdrop,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(ModernSpacing.Medium)) {
                            // 标题区域
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = ModernSpacing.Medium),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            color = ModernColors.Primary.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(ModernCorners.Medium)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AttachMoney,
                                        contentDescription = null,
                                        tint = ModernColors.Primary,
                                        modifier = Modifier.size(ModernIconSize.Large)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(ModernSpacing.Medium))
                                
                                Column {
                                    Text(
                                        text = "价格配置",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ModernColors.OnSurface
                                    )
                                    Text(
                                        text = "设置水电费单价",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ModernColors.OnSurfaceVariant
                                    )
                                }
                            }

                            HorizontalDivider(
                                color = ModernColors.OutlineVariant.copy(alpha = 0.5f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = ModernSpacing.Small)
                            )
                            
                            // 水价设置
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = ModernSpacing.Medium)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(
                                            color = ModernColors.Success.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(ModernCorners.Small)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Water,
                                        contentDescription = null,
                                        tint = ModernColors.Success,
                                        modifier = Modifier.size(ModernIconSize.Medium)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(ModernSpacing.Medium))
                                
                                Text(
                                    text = "水费单价",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ModernColors.OnSurface
                                )
                            }
                            
                            OutlinedTextField(
                                value = waterTxt,
                                onValueChange = { waterTxt = it; vm.onWaterChange(it) },
                                label = { Text("水价 (元/m³)", color = ModernColors.Success) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(ModernCorners.Medium),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ModernColors.Success,
                                    unfocusedBorderColor = ModernColors.Outline,
                                    focusedLabelColor = ModernColors.Success
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(ModernSpacing.Medium))
                            
                            // 电价设置
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = ModernSpacing.Medium)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(
                                            color = ModernColors.Warning.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(ModernCorners.Small)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = ModernColors.Warning,
                                        modifier = Modifier.size(ModernIconSize.Medium)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(ModernSpacing.Medium))
                                
                                Text(
                                    text = "电费单价",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ModernColors.OnSurface
                                )
                            }
                            
                            OutlinedTextField(
                                value = elecTxt,
                                onValueChange = { elecTxt = it; vm.onElecChange(it) },
                                label = { Text("电价 (元/kWh)", color = ModernColors.Warning) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(ModernCorners.Medium),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ModernColors.Warning,
                                    unfocusedBorderColor = ModernColors.Outline,
                                    focusedLabelColor = ModernColors.Warning
                                )
                            )
                        }
                    }
                }
                
                // 隐私保护设置区域
                item {
                    LiquidGlassCard(
                        backdrop = cardBackdrop,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(ModernSpacing.Medium)) {
                            // 隐私保护标题
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = ModernSpacing.Medium)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(
                                            color = ModernColors.Primary.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(ModernCorners.Small)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = ModernColors.Primary,
                                        modifier = Modifier.size(ModernIconSize.Medium)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(ModernSpacing.Medium))
                                
                                Column {
                                    Text(
                                        text = "隐私保护",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ModernColors.OnSurface
                                    )
                                    Text(
                                        text = "设置需要隐藏的关键字",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ModernColors.OnSurfaceVariant
                                    )
                                }
                            }
                            
                            HorizontalDivider(
                                color = ModernColors.OutlineVariant.copy(alpha = 0.5f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = ModernSpacing.Small)
                            )
                            
                            // 关键字输入区域
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = newKeyword,
                                    onValueChange = { newKeyword = it },
                                    label = { Text("添加关键字", color = ModernColors.Primary) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(ModernCorners.Medium),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ModernColors.Primary,
                                        unfocusedBorderColor = ModernColors.Outline,
                                        focusedLabelColor = ModernColors.Primary
                                    ),
                                    singleLine = true
                                )
                                
                                Spacer(modifier = Modifier.width(ModernSpacing.Small))
                                
                                IconButton(
                                    onClick = {
                                        if (newKeyword.isNotBlank()) {
                                            vm.addPrivacyKeyword(newKeyword.trim())
                                            newKeyword = ""
                                        }
                                    },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            color = ModernColors.Primary,
                                            shape = RoundedCornerShape(ModernCorners.Medium)
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "添加关键字",
                                        tint = Color.White
                                    )
                                }
                            }
                            
                            // 关键字列表
                            if (privacyKeywords.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(ModernSpacing.Medium))
                                
                                Text(
                                    text = "已设置的关键字 (${privacyKeywords.size}/10)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = ModernColors.OnSurface,
                                    modifier = Modifier.padding(bottom = ModernSpacing.Small)
                                )
                                
                                // 使用Flow布局实现自适应换行效果
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(ModernSpacing.XSmall),
                                    verticalArrangement = Arrangement.spacedBy(ModernSpacing.XSmall),
                                    maxItemsInEachRow = 4
                                ) {
                                    privacyKeywords.forEach { keyword ->
                                        Surface(
                                            shape = RoundedCornerShape(ModernCorners.Small),
                                            color = ModernColors.Primary.copy(alpha = 0.08f),
                                            border = BorderStroke(1.dp, ModernColors.Outline.copy(alpha = 0.3f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(
                                                    start = ModernSpacing.Small,
                                                    end = 4.dp,
                                                    top = 4.dp,
                                                    bottom = 4.dp
                                                ),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = keyword,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = ModernColors.OnSurface
                                                )
                                                
                                                IconButton(
                                                    onClick = { showDeleteDialog = keyword },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "删除关键字",
                                                        tint = ModernColors.Error,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.height(ModernSpacing.Medium))
                                
                                Surface(
                                    shape = RoundedCornerShape(ModernCorners.Medium),
                                    color = ModernColors.SurfaceVariant.copy(alpha = 0.3f)
                                ) {
                                    Text(
                                        text = "暂无设置关键字，添加关键字后将在收据中隐藏相应内容",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ModernColors.OnSurfaceVariant,
                                        modifier = Modifier.padding(ModernSpacing.Medium)
                                    )
                                }
                            }
                        }
                    }
                }
                
                // 操作按钮区域
                item {
                    LiquidGlassButton(
                        onClick = { vm.save{ navController.popBackStack() } },
                        backdrop = cardBackdrop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(ModernTouchTarget.Comfortable)
                            .padding(horizontal = ModernSpacing.Large)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(ModernIconSize.Medium),
                            tint = ModernColors.Primary
                        )
                        Spacer(modifier = Modifier.width(ModernSpacing.Small))
                        Text(
                            "保存设置",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = ModernColors.Primary
                        )
                    }
                }
            }
        }
        
        // Top gradient overlay for smooth status bar transition
        com.morgen.rentalmanager.ui.components.TopGradientOverlay(
            modifier = Modifier.align(Alignment.TopCenter)
        )
        
        // 玻璃顶栏 - Floating above content
        FrostedBlurTopBar(
            title = "设置",
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
    
    // 删除确认对话框
    showDeleteDialog?.let { keyword ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = {
                Text(
                    text = "确认删除",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    text = "确定要删除关键字 \"$keyword\" 吗？",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.removePrivacyKeyword(keyword)
                        showDeleteDialog = null
                    }
                ) {
                    Text(
                        text = "删除",
                        color = ModernColors.Error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = null }
                ) {
                    Text(
                        text = "取消",
                        color = ModernColors.OnSurfaceVariant
                    )
                }
            },
            containerColor = ModernColors.Surface,
            shape = RoundedCornerShape(ModernCorners.Large)
        )
    }
}