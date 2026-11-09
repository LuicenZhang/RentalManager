package com.morgen.rentalmanager.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.navArgument
import com.morgen.rentalmanager.MainScreen
import com.morgen.rentalmanager.TenantApplication
import com.morgen.rentalmanager.ui.addbillall.AddBillAllScreen
import com.morgen.rentalmanager.ui.addtenant.AddTenantScreen
import com.morgen.rentalmanager.ui.billlist.BillListScreen
import com.morgen.rentalmanager.ui.billdetail.BillDetailScreen
import com.morgen.rentalmanager.ui.edittenant.EditTenantScreen
import com.morgen.rentalmanager.ui.datatransfer.DataTransferScreen
import com.morgen.rentalmanager.ui.datatransfer.DataTransferViewModelFactory
import com.morgen.rentalmanager.ui.about.AboutScreen
import com.morgen.rentalmanager.ui.receipt.ReceiptScreen
import com.morgen.rentalmanager.ui.setting.SettingScreen
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import java.net.URLDecoder
import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel

// 共享的动画缓动曲线
private val SharedEasing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    // 控制叠加式页面显示状态
    var showAddTenant by remember { mutableStateOf(false) }
    var showAddBillAll by remember { mutableStateOf(false) }
    
    // 控制模糊效果
    var blurRadius by remember { mutableStateOf(0f) }

    // 模糊动画
    val animatedBlurRadius by animateFloatAsState(
        targetValue = blurRadius,
        animationSpec = tween(durationMillis = 300, easing = SharedEasing),
        label = "blur_animation"
    )
    
    // 处理系统返回键事件
    BackHandler(enabled = showAddTenant || showAddBillAll) {
        if (showAddTenant) {
            showAddTenant = false
            blurRadius = 0f
        } else if (showAddBillAll) {
            showAddBillAll = false
            blurRadius = 0f
        }
    }
    
    // 监听叠加页面状态，控制模糊效果
    LaunchedEffect(showAddTenant, showAddBillAll) {
        blurRadius = if (showAddTenant || showAddBillAll) 6f else 0f
    }
    
    // 主导航和叠加页面容器
    Box(modifier = Modifier.fillMaxSize()) {
        
        // 主导航内容
        Box(modifier = Modifier
            .fillMaxSize()
            .blur(radius = animatedBlurRadius.dp)
        ) {
            NavHost(
                navController = navController, 
                startDestination = "main",
                // 设置默认的导航动画 - 使用右侧覆盖式动画
                enterTransition = { 
                    // 新页面从右侧进入，同时推动当前页面向左移动
                    slideInHorizontally(
                        animationSpec = tween(
                            durationMillis = 500,
                            easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
                        )
                    ) { fullWidth -> fullWidth } // 从右侧进入
                },
                exitTransition = { 
                    // 当前页面向左退出
                    slideOutHorizontally(
                        animationSpec = tween(
                            durationMillis = 450,
                            easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
                        )
                    ) { fullWidth -> -fullWidth / 3 } // 向左轻微移出
                },
                popEnterTransition = { 
                    // 返回时，上一页从左侧进入
                    slideInHorizontally(
                        animationSpec = tween(
                            durationMillis = 500,
                            easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
                        )
                    ) { fullWidth -> -fullWidth / 3 } // 从左侧进入
                },
                popExitTransition = { 
                    // 当前页面向右退出
                    slideOutHorizontally(
                        animationSpec = tween(
                            durationMillis = 450,
                            easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
                        )
                    ) { fullWidth -> fullWidth } // 向右退出
                }
            ) {
                // 主界面 - 保持显示
                composable("main") {
                    MainScreen(
                        navController = navController,
                        onAddTenantClick = { 
                            showAddTenant = true // 只设置状态，让LaunchedEffect处理模糊
                        },
                        onAddBillClick = { 
                            showAddBillAll = true // 只设置状态，让LaunchedEffect处理模糊
                        }
                    )
                }
                
                // 编辑租户
                composable(
                    route = "edit_tenant/{roomNumber}",
                    arguments = listOf(navArgument("roomNumber") { type = NavType.StringType })
                ) { _ ->
                    EditTenantScreen(navController = navController)
                }
                
                // 账单列表 - 使用与其他页面一致的横向覆盖动画
                composable(
                    route = "bill_list",
                    enterTransition = { 
                        // 从右侧滑入，与其他页面保持一致
                        slideInHorizontally(
                            animationSpec = tween(
                                durationMillis = 500,
                                easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
                            )
                        ) { fullWidth -> fullWidth }
                    },
                    exitTransition = { 
                        // 向左轻微移出
                        slideOutHorizontally(
                            animationSpec = tween(
                                durationMillis = 450,
                                easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
                            )
                        ) { fullWidth -> -fullWidth / 3 }
                    },
                    popEnterTransition = { 
                        // 返回时从左侧进入
                        slideInHorizontally(
                            animationSpec = tween(
                                durationMillis = 500,
                                easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
                            )
                        ) { fullWidth -> -fullWidth / 3 }
                    },
                    popExitTransition = { 
                        // 返回时向右退出
                        slideOutHorizontally(
                            animationSpec = tween(
                                durationMillis = 450,
                                easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
                            )
                        ) { fullWidth -> fullWidth }
                    }
                ) { 
                    val context = LocalContext.current
                    val application = context.applicationContext as TenantApplication
                    BillListScreen(
                        navController = navController,
                        repository = application.repository
                    ) 
                }
                
                // 账单详情
                composable(
                    route = "bill_detail/{roomNumber}/{month}",
                    arguments = listOf(
                        navArgument("roomNumber") { type = NavType.StringType },
                        navArgument("month") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val roomNumber = backStackEntry.arguments?.getString("roomNumber")?.let { 
                        try {
                            java.net.URLDecoder.decode(it, "UTF-8") 
                        } catch (e: Exception) {
                            android.util.Log.w("AppNavigation", "房间号解码失败", e)
                            it
                        }
                    } ?: ""
                    val month = backStackEntry.arguments?.getString("month")?.let { 
                        try {
                            java.net.URLDecoder.decode(it, "UTF-8") 
                        } catch (e: Exception) {
                            android.util.Log.w("AppNavigation", "月份解码失败", e)
                            it
                        }
                    } ?: ""
                    
                    // 验证参数有效性
                    if (roomNumber.isNotBlank() && month.isNotBlank()) {
                        val context = LocalContext.current
                        val application = context.applicationContext as TenantApplication
                        BillDetailScreen(
                            navController = navController,
                            roomNumber = roomNumber,
                            month = month,
                            repository = application.repository
                        )
                    } else {
                        // 参数无效时显示错误页面或返回
                        LaunchedEffect(Unit) {
                            android.util.Log.w("AppNavigation", "账单详情参数无效: roomNumber=$roomNumber, month=$month")
                            navController.popBackStack()
                        }
                    }
                }
                
                // 备份与恢复界面
                composable("data_transfer") { 
                    val context = LocalContext.current
                    val application = context.applicationContext as TenantApplication
                    DataTransferScreen(
                        navController = navController,
                        viewModel = viewModel(
                            factory = DataTransferViewModelFactory(application.repository)
                        )
                    )
                }
                
                // 收据界面
                composable(
                    route = "receipt/{imageUri}",
                    arguments = listOf(navArgument("imageUri") { type = NavType.StringType })
                ) { backStackEntry ->
                    val imageUri = backStackEntry.arguments?.getString("imageUri") ?: ""
                    ReceiptScreen(navController = navController, imageUri = URLDecoder.decode(imageUri, "UTF-8"))
                }
                
                // 设置界面
                composable("settings") { 
                    SettingScreen(navController) 
                }
                
                // 关于应用界面
                composable("about") {
                    AboutScreen(navController)
                }
            }
        }
        
        // 共享的从底部覆盖动画（租户和账单页面都使用）
        val overlayEnter = slideInVertically(
            animationSpec = tween(durationMillis = 500, easing = SharedEasing)
        ) { it }
        
        val overlayExit = slideOutVertically(
            animationSpec = tween(durationMillis = 450, easing = SharedEasing)
        ) { it }
        
        // 添加租户页面
        AnimatedVisibility(
            visible = showAddTenant,
            enter = overlayEnter,
            exit = overlayExit,
            modifier = Modifier.fillMaxSize().zIndex(10f)
        ) {
            AddTenantScreen(
                navController = navController,
                onDismiss = { showAddTenant = false }
            )
        }
        
        // 添加账单页面
        AnimatedVisibility(
            visible = showAddBillAll,
            enter = overlayEnter,
            exit = overlayExit,
            modifier = Modifier.fillMaxSize().zIndex(10f)
        ) {
            AddBillAllScreen(
                navController = navController,
                onDismiss = { showAddBillAll = false }
            )
        }
    }
}

