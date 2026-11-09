package com.morgen.rentalmanager.ui.edittenant

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.morgen.rentalmanager.ui.components.FrostedBlurTopBar
import com.morgen.rentalmanager.ui.components.LiquidGlassCard
import com.morgen.rentalmanager.ui.components.LiquidGlassButton
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop

import com.morgen.rentalmanager.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTenantScreen(navController: NavController) {
    val context = LocalContext.current
    val application = context.applicationContext as TenantApplication
    val viewModel: EditTenantViewModel = viewModel(
        factory = EditTenantViewModelFactory(application.repository)
    )

    val tenantState by viewModel.tenant.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var rent by remember { mutableStateOf("") }

    // Update local states when tenant data is loaded
    LaunchedEffect(tenantState) {
        name = tenantState.name
        rent = tenantState.rent.toString()
    }
    
    // Calculate top padding based on status bar height + TopAppBar height
    // FrostedBlurTopBar adds status bar padding internally + 8.dp + height (~56dp)
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val topPadding = statusBarHeight + 64.dp + ModernSpacing.Large
    
    // Create Backdrop for TopBar blur effect (to blur list content)
    val topBarBackdrop = rememberLayerBackdrop()
    
    // Create Backdrop for Card blur effect (to blur background)
    val cardBackdrop = rememberLayerBackdrop()

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Background layer - used for Card glass effect sampling
        Box(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(cardBackdrop)
                .background(ModernColors.Background)
        )
        
        // Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(topBarBackdrop)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = topPadding, // Space for Floating Capsules
                    bottom = ModernSpacing.Large,
                    start = ModernSpacing.Large,
                    end = ModernSpacing.Large
                )
            ) {
            item {
                // Headline Section
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
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = ModernColors.Primary,
                                modifier = Modifier.size(ModernIconSize.Large)
                            )
                        }

                        Spacer(modifier = Modifier.width(ModernSpacing.Medium))

                        Column {
                            Text(
                                text = "编辑租户 - ${tenantState.roomNumber}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = ModernColors.OnSurface
                            )
                            Text(
                                text = "修改租户基本信息",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ModernColors.OnSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(ModernSpacing.Large))
            }

            item {
                // Form Section
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
                            value = tenantState.roomNumber,
                            onValueChange = { /* Room number is primary key, not editable */ },
                            label = { Text("房间号", color = ModernColors.OnSurfaceVariant) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false,
                            shape = RoundedCornerShape(ModernCorners.Medium),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledBorderColor = ModernColors.Outline,
                                disabledLabelColor = ModernColors.OnSurfaceVariant
                            )
                        )

                        Spacer(modifier = Modifier.height(ModernSpacing.Medium))

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("租户姓名", color = ModernColors.Primary) },
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
                            label = { Text("月租金 (元)", color = ModernColors.Primary) },
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

            item {
                // Operations Section
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backdrop = cardBackdrop,
                    shape = RoundedCornerShape(ModernCorners.Medium)
                ) {
                    Column(modifier = Modifier.padding(ModernSpacing.Large)) {
                        Text(
                            text = "操作",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = ModernColors.OnSurface,
                            modifier = Modifier.padding(bottom = ModernSpacing.Medium)
                        )

                        // Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(ModernSpacing.Medium)
                        ) {
                            // Save Button
                            LiquidGlassButton(
                                onClick = {
                                    val rentValue = rent.toDoubleOrNull()
                                    if (name.isNotBlank() && rentValue != null) {
                                        coroutineScope.launch {
                                            val updatedTenant = Tenant(
                                                roomNumber = tenantState.roomNumber,
                                                name = name,
                                                rent = rentValue
                                            )

                                            val rentChanged = tenantState.rent != rentValue

                                            viewModel.updateTenant(updatedTenant)

                                            if (rentChanged) {
                                                viewModel.recalculateAllBills(tenantState.roomNumber)
                                            }

                                            delay(500)

                                            Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show()
                                            navController.popBackStack()
                                        }
                                    } else {
                                        Toast.makeText(context, "请填写所有有效字段", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(ModernTouchTarget.Comfortable)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = null,
                                    modifier = Modifier.size(ModernIconSize.Medium),
                                    tint = ModernColors.Primary
                                )
                                Spacer(modifier = Modifier.width(ModernSpacing.Small))
                                Text(
                                    "保存",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ModernColors.Primary
                                )
                            }

                            // Delete Button
                            LiquidGlassButton(
                                onClick = {
                                    coroutineScope.launch {
                                        viewModel.deleteTenant()
                                        delay(100)
                                        navController.popBackStack()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(ModernTouchTarget.Comfortable)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(ModernIconSize.Medium),
                                    tint = ModernColors.Error
                                )
                                Spacer(modifier = Modifier.width(ModernSpacing.Small))
                                Text(
                                    "删除",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ModernColors.Error
                                )
                            }
                        }
                    }
                }
            }
        }

        }


        // Top gradient overlay for smooth status bar transition
        com.morgen.rentalmanager.ui.components.TopGradientOverlay(
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // Floating Capsule Top Bar
        FrostedBlurTopBar(
            title = "编辑租户",
            backdrop = topBarBackdrop,
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            onIconClick = { navController.popBackStack() },
            showMenu = false
        )

    }
}
 