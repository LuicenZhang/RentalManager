package com.morgen.rentalmanager.ui.about

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.morgen.rentalmanager.ui.theme.ModernColors
import com.morgen.rentalmanager.ui.theme.ModernCorners
import com.morgen.rentalmanager.ui.theme.ModernSpacing
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.res.painterResource
import java.util.*
import androidx.compose.ui.draw.clip
import com.morgen.rentalmanager.R
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.morgen.rentalmanager.ui.components.FrostedBlurTopBar
import com.morgen.rentalmanager.ui.components.LiquidGlassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navController: NavController) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
    // 硬编码版本号
    val appVersion = "1.1.0 beta"
    val appVersionCode = "1"
    
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
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(top = topContentPadding, start = ModernSpacing.Medium, end = ModernSpacing.Medium, bottom = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(ModernSpacing.Large))
                
                // 应用图标 - 从drawable文件夹加载自定义图标并添加圆角
                Image(
                    painter = painterResource(id = R.drawable.app_icon),
                    contentDescription = "应用图标",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(20.dp))
                )
                
                Spacer(modifier = Modifier.height(ModernSpacing.Large))
                
                // 应用名称
                Text(
                    text = "租房管家",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = ModernColors.OnSurface
                )
                
                Spacer(modifier = Modifier.height(ModernSpacing.Small))
                
                // 应用版本
                Text(
                    text = "Version $appVersion",
                    style = MaterialTheme.typography.bodyLarge,
                    color = ModernColors.OnSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(ModernSpacing.XXLarge))
                
                // 应用信息卡片
                LiquidGlassCard(
                    backdrop = cardBackdrop,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(ModernSpacing.Large),
                        verticalArrangement = Arrangement.spacedBy(ModernSpacing.Medium)
                    ) {
                        Text(
                            text = "应用介绍",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Medium,
                            color = ModernColors.OnSurface
                        )
                        
                        Text(
                            text = "租房管家是一款专为房东设计的租户和账单管理工具，可以轻松记录租户信息、生成水电费账单、打印收据等。",
                            style = MaterialTheme.typography.bodyLarge,
                            color = ModernColors.OnSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(ModernSpacing.Large))
                
                // 开发者信息卡片
                LiquidGlassCard(
                    backdrop = cardBackdrop,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(ModernSpacing.Large),
                        verticalArrangement = Arrangement.spacedBy(ModernSpacing.Medium)
                    ) {
                        Text(
                            text = "开发者信息",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Medium,
                            color = ModernColors.OnSurface
                        )
                        
                        Text(
                            text = "开发者：Claude、ChatGPT、Google Gemini 共同开发",
                            style = MaterialTheme.typography.bodyLarge,
                            color = ModernColors.OnSurfaceVariant
                        )
                        
                        Text(
                            text = "联系邮箱：ikicc0752@gmail.com",
                            style = MaterialTheme.typography.bodyLarge,
                            color = ModernColors.OnSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(ModernSpacing.Large))
                
                // 版权信息
                val currentYear = Calendar.getInstance().get(Calendar.YEAR).toString()
                Text(
                    text = "© $currentYear 租房管家 版权所有",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ModernColors.OnSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(ModernSpacing.XXLarge))
            }
        }
        
        // Top gradient overlay for smooth status bar transition
        com.morgen.rentalmanager.ui.components.TopGradientOverlay(
            modifier = Modifier.align(Alignment.TopCenter)
        )
        
        // 玻璃顶栏 - Floating above content
        FrostedBlurTopBar(
            title = "关于应用",
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