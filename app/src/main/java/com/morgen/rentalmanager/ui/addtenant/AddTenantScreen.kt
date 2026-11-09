package com.morgen.rentalmanager.ui.addtenant

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.morgen.rentalmanager.TenantApplication
import com.morgen.rentalmanager.myapplication.Tenant
import com.morgen.rentalmanager.ui.theme.*
import com.morgen.rentalmanager.ui.components.FrostedBlurTopBar
import com.morgen.rentalmanager.ui.components.LiquidGlassCard
import com.morgen.rentalmanager.ui.components.LiquidGlassButton
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTenantScreen(
    navController: NavController,
    onDismiss: () -> Unit = {}
) {
    val context = LocalContext.current
    val application = context.applicationContext as TenantApplication
    val viewModel: AddTenantViewModel = viewModel(
        factory = AddTenantViewModelFactory(application.repository)
    )

    var roomNumber by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var rent by remember { mutableStateOf("") }

    // Calculate top padding based on status bar height + TopAppBar height
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val topPadding = statusBarHeight + 64.dp + ModernSpacing.Large

    // Create Backdrop for glass effects
    val topBarBackdrop = rememberLayerBackdrop()
    val cardBackdrop = rememberLayerBackdrop()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 主要内容区域 - 使用 layerBackdrop(topBarBackdrop) 让 TopBar 可以捕获全部内容
        Box(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(topBarBackdrop)
        ) {
            // Background layer - 嵌套在 topBarBackdrop 内，用于卡片的玻璃效果采样
            // 这样 TopBar 模糊时可以完整捕获卡片的玻璃效果
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .layerBackdrop(cardBackdrop)
                    .background(ModernColors.Background)
            )

            // Content - 在 cardBackdrop 之后，但仍在 topBarBackdrop 内
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = topPadding,
                    bottom = ModernSpacing.Large,
                    start = ModernSpacing.Large,
                    end = ModernSpacing.Large
                )
            ) {
                // Headline Section
                item {
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backdrop = cardBackdrop,
                        shape = RoundedCornerShape(ModernCorners.Medium)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(ModernSpacing.Large),
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
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = ModernColors.Primary,
                                    modifier = Modifier.size(ModernIconSize.Large)
                                )
                            }

                            Spacer(modifier = Modifier.width(ModernSpacing.Medium))

                            Column {
                                Text(
                                    text = "新增租户",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = ModernColors.OnSurface
                                )
                                Text(
                                    text = "填写租户基本信息",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ModernColors.OnSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(ModernSpacing.Large))
                }

                // Form Section
                item {
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backdrop = cardBackdrop,
                        shape = RoundedCornerShape(ModernCorners.Medium)
                    ) {
                        Column(modifier = Modifier.padding(ModernSpacing.Large)) {
                            Text(
                                text = "基本信息",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = ModernColors.OnSurface,
                                modifier = Modifier.padding(bottom = ModernSpacing.Medium)
                            )

                            OutlinedTextField(
                                value = roomNumber,
                                onValueChange = { roomNumber = it },
                                label = { Text("房间号") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(ModernCorners.Medium),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ModernColors.Primary,
                                    unfocusedBorderColor = ModernColors.Outline,
                                    focusedLabelColor = ModernColors.Primary
                                )
                            )

                            Spacer(modifier = Modifier.height(ModernSpacing.Medium))

                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("租户姓名") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(ModernCorners.Medium),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ModernColors.Primary,
                                    unfocusedBorderColor = ModernColors.Outline,
                                    focusedLabelColor = ModernColors.Primary
                                )
                            )

                            Spacer(modifier = Modifier.height(ModernSpacing.Medium))

                            OutlinedTextField(
                                value = rent,
                                onValueChange = { rent = it },
                                label = { Text("月租金 (元)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(ModernCorners.Medium),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ModernColors.Primary,
                                    unfocusedBorderColor = ModernColors.Outline,
                                    focusedLabelColor = ModernColors.Primary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(ModernSpacing.Large))
                }

                // Save Button Section
                item {
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backdrop = cardBackdrop,
                        shape = RoundedCornerShape(ModernCorners.Medium)
                    ) {
                        Column(modifier = Modifier.padding(ModernSpacing.Large)) {
                            LiquidGlassButton(
                                onClick = {
                                    val rentValue = rent.toDoubleOrNull()
                                    if (roomNumber.isNotBlank() && name.isNotBlank() && rentValue != null) {
                                        val newTenant = Tenant(
                                            roomNumber = roomNumber,
                                            name = name,
                                            rent = rentValue
                                        )
                                        viewModel.addTenant(newTenant)

                                        // Dismiss callback
                                        onDismiss()
                                        kotlinx.coroutines.MainScope().launch {
                                            delay(50)
                                            onDismiss()
                                        }

                                        // Navigate back
                                        if (navController.previousBackStackEntry != null) {
                                            navController.popBackStack()
                                        }
                                    } else {
                                        Toast.makeText(context, "请填写所有字段", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                backdrop = cardBackdrop,
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
                                    "保存租户",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ModernColors.Primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(ModernSpacing.Large))
                }

                // Info Section
                item {
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backdrop = cardBackdrop,
                        shape = RoundedCornerShape(ModernCorners.Medium)
                    ) {
                        Column(modifier = Modifier.padding(ModernSpacing.Medium)) {
                            Text(
                                text = "💡 填写说明",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = ModernColors.OnSurface
                            )
                            Spacer(modifier = Modifier.height(ModernSpacing.Small))
                            Text(
                                text = "• 房间号用于唯一标识租户\n• 租户姓名用于生成收据\n• 月租金将自动计入账单",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ModernColors.OnSurfaceVariant,
                                lineHeight = ModernTypography.LineHeightMedium
                            )
                        }
                    }
                }
            }
        }

        // Top gradient overlay for smooth status bar transition
        com.morgen.rentalmanager.ui.components.TopGradientOverlay(
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // Floating glass TopBar
        FrostedBlurTopBar(
            title = "添加租户",
            backdrop = topBarBackdrop,
            overlayColor = Color.Transparent,
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            onIconClick = {
                onDismiss()
                kotlinx.coroutines.MainScope().launch {
                    delay(50)
                    onDismiss()
                }
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            },
            showMenu = false
        )
    }
}