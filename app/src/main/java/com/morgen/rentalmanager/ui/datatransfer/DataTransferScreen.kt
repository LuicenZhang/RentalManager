package com.morgen.rentalmanager.ui.datatransfer

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.morgen.rentalmanager.ui.components.FrostedBlurTopBar
import com.morgen.rentalmanager.ui.components.LiquidGlassCard
import com.morgen.rentalmanager.ui.components.LiquidGlassButton
import com.morgen.rentalmanager.ui.theme.ModernColors
import com.morgen.rentalmanager.ui.theme.ModernElevation
import com.morgen.rentalmanager.ui.theme.ModernSpacing
import com.morgen.rentalmanager.ui.theme.ModernCorners
import com.morgen.rentalmanager.ui.theme.ModernIconSize
import com.morgen.rentalmanager.ui.theme.ModernTouchTarget
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataTransferScreen(
    navController: NavController,
    viewModel: DataTransferViewModel = viewModel(factory = DataTransferViewModelFactory((LocalContext.current.applicationContext as com.morgen.rentalmanager.TenantApplication).repository))
) {
    val context = LocalContext.current
    var isExporting by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }
    
    // 操作结果对话框状态
    var showResultDialog by remember { mutableStateOf(false) }
    var resultDialogTitle by remember { mutableStateOf("") }
    var resultDialogMessage by remember { mutableStateOf("") }
    var isSuccessResult by remember { mutableStateOf(true) }
    var shouldNavigateBack by remember { mutableStateOf(false) }
    var backupFilePath by remember { mutableStateOf("") }
    
    // Backdrop for glass effects
    val topBarBackdrop = com.kyant.backdrop.backdrops.rememberLayerBackdrop()
    val cardBackdrop = com.kyant.backdrop.backdrops.rememberLayerBackdrop()
    
    // Calculate padding for content
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val topContentPadding = statusBarHeight + 80.dp // Space for floating capsule top bar
    
    // Document picker for import
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.importData(context, uri)
            isImporting = true
            Toast.makeText(context, "正在恢复数据...", Toast.LENGTH_SHORT).show()
        }
    }
    
    // Document picker for export
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
            viewModel.exportData(context, uri)
            isExporting = true
            Toast.makeText(context, "正在备份数据...", Toast.LENGTH_SHORT).show()
        }
    }
    
    // Observer for operation status
    LaunchedEffect(viewModel.operationStatus) {
        when(viewModel.operationStatus) {
            is DataTransferStatus.Success -> {
                val successStatus = viewModel.operationStatus as DataTransferStatus.Success
                val message = successStatus.message
                
                // 显示成功对话框
                resultDialogTitle = "操作成功"
                resultDialogMessage = message
                isSuccessResult = true
                showResultDialog = true
                isExporting = false
                isImporting = false
                shouldNavigateBack = true
                
                // 保存备份文件路径
                backupFilePath = successStatus.filePath ?: ""
            }
            is DataTransferStatus.Error -> {
                val errorStatus = viewModel.operationStatus as DataTransferStatus.Error
                val message = errorStatus.message
                
                // 显示错误对话框
                resultDialogTitle = "操作失败"
                resultDialogMessage = message
                isSuccessResult = false
                showResultDialog = true
                isExporting = false
                isImporting = false
                shouldNavigateBack = false
            }
            else -> {
                // 其他状态不处理
            }
        }
    }

    // 在导航返回时重置状态
    DisposableEffect(navController) {
        onDispose {
                viewModel.resetStatus()
            }
    }

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
            
            // Scrollable content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = topContentPadding, start = ModernSpacing.Medium, end = ModernSpacing.Medium, bottom = ModernSpacing.Medium),
                verticalArrangement = Arrangement.spacedBy(ModernSpacing.Large)
            ) {
                // 主功能卡片 - Manual Backup
                LiquidGlassCard(
                    backdrop = cardBackdrop,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(ModernSpacing.Large),
                        verticalArrangement = Arrangement.spacedBy(ModernSpacing.Large)
                    ) {
                        // 标题
                        Text(
                            text = "手动备份与恢复",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ModernColors.OnSurface
                        )
                        
                        // 备份功能按钮
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(ModernSpacing.Medium)
                        ) {
                            // 备份按钮
                            LiquidGlassButton(
                                onClick = {
                                    val dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
                                    val date = LocalDateTime.now().format(dateFormat)
                                    val fileName = "租房管家备份_$date.json"
                                    exportLauncher.launch(fileName)
                                },
                                backdrop = cardBackdrop,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                enabled = !isExporting && !isImporting
                            ) {
                                Icon(
                                    Icons.Default.FileUpload,
                                    contentDescription = null,
                                    tint = ModernColors.Primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "备份数据",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ModernColors.Primary
                                )
                            }

                            // 恢复按钮
                            com.morgen.rentalmanager.ui.components.LiquidGlassButton(
                                onClick = { importLauncher.launch("application/json") },
                                backdrop = cardBackdrop,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                enabled = !isExporting && !isImporting
                            ) {
                                Icon(
                                    Icons.Default.Download,
                                    contentDescription = null,
                                    tint = ModernColors.Primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "恢复数据",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ModernColors.Primary
                                )
                            }
                        }
                    }
                }
                
                // 自动备份管理卡片
                com.morgen.rentalmanager.ui.components.LiquidGlassCard(
                    backdrop = cardBackdrop,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(ModernSpacing.Large),
                        verticalArrangement = Arrangement.spacedBy(ModernSpacing.Medium)
                    ) {
                        // 标题
                        Row(
                            modifier = Modifier.fillMaxWidth(),
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
                                    imageVector = Icons.Default.Backup,
                                    contentDescription = null,
                                    tint = ModernColors.Primary,
                                    modifier = Modifier.size(ModernIconSize.Large)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(ModernSpacing.Medium))
                            
                            Column {
                                Text(
                                    text = "自动备份管理",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ModernColors.OnSurface
                                )
                                Text(
                                    text = "查看和使用自动备份文件",
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
                        
                        // 自动备份路径
                        val activity = context as? android.app.Activity
                        val backupDir = if (activity != null) {
                            com.morgen.rentalmanager.utils.BackupFileHelper.getBackupDirectory(activity)
                        } else {
                            "文档/租房管家备份"
                        }
                        
                        // 备份路径信息
                        Surface(
                            shape = RoundedCornerShape(ModernCorners.Medium),
                            color = ModernColors.SurfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.padding(vertical = ModernSpacing.Small)
                        ) {
                            Column(
                                modifier = Modifier.padding(ModernSpacing.Medium),
                                verticalArrangement = Arrangement.spacedBy(ModernSpacing.Small)
                            ) {
                                Text(
                                    text = "备份文件位置:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = ModernColors.OnSurface
                                )
                                
                                Text(
                                    text = "$backupDir/租房管家自动备份.json",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ModernColors.OnSurfaceVariant
                                )
                            }
                        }
                        
                        // 信息提示
                        Surface(
                            shape = RoundedCornerShape(ModernCorners.Medium),
                            color = ModernColors.Primary.copy(alpha = 0.1f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(ModernSpacing.Medium),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = ModernColors.Primary,
                                    modifier = Modifier.size(ModernIconSize.Small)
                                )
                                Spacer(modifier = Modifier.width(ModernSpacing.Small))
                                Text(
                                    text = "每次保存账单时，应用会自动创建备份文件",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ModernColors.Primary
                                )
                            }
                        }
                        
                        // 恢复自动备份按钮
                        com.morgen.rentalmanager.ui.components.LiquidGlassButton(
                            onClick = {
                                viewModel.importAutoBackup(context)
                                isImporting = true
                                Toast.makeText(context, "正在从自动备份恢复数据...", Toast.LENGTH_SHORT).show()
                            },
                            backdrop = cardBackdrop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = !isExporting && !isImporting
                        ) {
                            Icon(
                                Icons.Default.Download,
                                contentDescription = null,
                                tint = ModernColors.Primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "从自动备份恢复数据",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = ModernColors.Primary
                            )
                        }
                    }
                }
                
                // 操作提示信息
                if (isExporting || isImporting) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(ModernSpacing.Small)
                    ) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Text(
                            text = if (isExporting) "备份中，请稍候..." else "恢复中，请稍候...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ModernColors.OnSurfaceVariant
                        )
                    }
                }
                
                // Bottom padding for navigation bar
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
        
        // Top gradient overlay for smooth status bar transition
        com.morgen.rentalmanager.ui.components.TopGradientOverlay(
            modifier = Modifier.align(Alignment.TopCenter)
        )
        
        // 玻璃顶栏 - Floating above content
        com.morgen.rentalmanager.ui.components.FrostedBlurTopBar(
            title = "备份与恢复",
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
    
    // 结果对话框
    if (showResultDialog) {
        ResultDialog(
            title = resultDialogTitle,
            message = resultDialogMessage,
            isSuccess = isSuccessResult,
            filePath = backupFilePath,
            isExport = viewModel.isExportOperation,
            onDismiss = {
                viewModel.resetStatus()
                showResultDialog = false
                if (shouldNavigateBack) {
                    navController.popBackStack()
            }
            },
            onShare = {
                if (backupFilePath.isNotEmpty() && viewModel.isExportOperation) {
                    shareBackupFile(context, backupFilePath, viewModel.backupUri, viewModel.backupFileName)
                }
            }
        )
    }
}

@Composable
fun ResultDialog(
    title: String,
    message: String,
    isSuccess: Boolean,
    filePath: String,
    isExport: Boolean,
    onDismiss: () -> Unit,
    onShare: (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ModernSpacing.Medium)
            ) {
                // 状态图标
                Surface(
                    shape = CircleShape,
                    color = if (isSuccess) 
                        ModernColors.Success.copy(alpha = 0.1f) 
                    else 
                        ModernColors.Error.copy(alpha = 0.1f),
                    modifier = Modifier.size(40.dp)
            ) {
                    Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                            tint = if (isSuccess) ModernColors.Success else ModernColors.Error,
                            modifier = Modifier.size(24.dp)
                )
                    }
                }
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ModernColors.OnSurface
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.padding(top = ModernSpacing.Medium),
                verticalArrangement = Arrangement.spacedBy(ModernSpacing.Medium)
            ) {
                // 分隔线
                HorizontalDivider(
                    color = ModernColors.OutlineVariant,
                    thickness = 1.dp
                )
                
                // 消息内容
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = ModernColors.OnSurfaceVariant
            )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 如果是备份成功且有分享回调，显示分享按钮
                if (isSuccess && onShare != null) {
                    OutlinedButton(
                        onClick = onShare,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ModernColors.Primary,
                        ),
                        border = BorderStroke(1.dp, ModernColors.Primary),
                        modifier = Modifier
                            .height(40.dp)
                            .padding(end = ModernSpacing.Medium)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(ModernSpacing.Small))
                        Text(
                            text = "分享",
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ModernColors.Primary
                ),
                    modifier = Modifier.height(40.dp)
            ) {
                Text(
                        text = "确定",
                    fontWeight = FontWeight.Medium
                )
                }
            }
        },
        containerColor = if (isSystemInDarkTheme()) Color(0xFF1D1D1D) else ModernColors.Surface,
        shape = RoundedCornerShape(ModernCorners.Large),
        modifier = Modifier.fillMaxWidth(0.9f),
        tonalElevation = ModernElevation.Level1
    )
}

// 分享备份文件的函数
private fun shareBackupFile(context: android.content.Context, filePath: String, uri: Uri?, fileName: String?) {
    try {
        // 如果有直接的Uri，优先使用
        if (uri != null) {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, uri)
                type = "application/json"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "分享备份文件"))
            return
        }
        
        // 否则，检查是否是自动备份文件(从Documents目录)
        val file = File(filePath)
        if (file.exists()) {
            // 创建临时文件用于分享
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val tempFile = File(context.cacheDir, fileName ?: "租房管家备份_${timestamp}.json")
            
            // 复制备份内容到临时文件
            file.inputStream().use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            // 获取临时文件Uri
            val fileUri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                tempFile
            )
            
            // 分享文件
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, fileUri)
                type = "application/json"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "分享备份文件"))
        } else {
            // 文件不存在
            Toast.makeText(context, "找不到备份文件: $filePath", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "分享文件失败: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
} 