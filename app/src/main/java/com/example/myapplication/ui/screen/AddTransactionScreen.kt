package com.example.myapplication.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.data.model.Category
import com.example.myapplication.data.model.TransactionType
import com.example.myapplication.ui.component.CalculatorKeyboard
import com.example.myapplication.ui.component.CategoryChip
import com.example.myapplication.ui.theme.ExpenseRed
import com.example.myapplication.ui.theme.IncomeGreen
import com.example.myapplication.ui.viewmodel.AddTransactionUiState
import com.example.myapplication.ui.viewmodel.AddTransactionViewModel
import java.util.*

/**
 * 添加或编辑交易页面
 * 提供交易类型选择、分类选择、金额输入和备注功能
 * @param transactionId 如果是编辑模式，则提供交易ID；如果是添加模式，则为null
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    transactionId: Long? = null,
    onNavigateBack: () -> Unit,
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    // 加载交易数据
    LaunchedEffect(transactionId) {
        if (transactionId != null && transactionId > 0) {
            viewModel.loadTransaction(transactionId)
        }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()

    val isEditMode = transactionId != null && transactionId > 0
    val screenTitle = if (isEditMode) "编辑交易" else "添加交易"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            viewModel.saveTransaction(isEditMode) { onNavigateBack() }
                        },
                        enabled = uiState.isAmountValid && uiState.selectedCategory != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text("保存")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 交易类型选择
            TransactionTypeSelector(
                selectedType = uiState.transactionType,
                onTypeSelected = viewModel::setTransactionType,
                modifier = Modifier.padding(16.dp)
            )

            // 金额显示
            AmountDisplay(
                amount = uiState.amount,
                isValid = uiState.isAmountValid,
                transactionType = uiState.transactionType,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // 分类选择
            CategorySelection(
                categories = categories.filter { it.type == uiState.transactionType },
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = viewModel::setSelectedCategory,
                modifier = Modifier.padding(16.dp)
            )

            // 日期和备注
            DateAndNoteSection(
                selectedDate = uiState.selectedDate,
                note = uiState.note,
                onDateSelected = viewModel::setDate,
                onNoteChanged = viewModel::setNote,
                modifier = Modifier.padding(16.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // 计算器键盘
            CalculatorKeyboard(
                onNumberClick = { number ->
                    val newAmount = if (uiState.amount == "0") number else uiState.amount + number
                    viewModel.setAmount(newAmount)
                },
                onOperatorClick = viewModel::onOperatorClick,
                onDeleteClick = {
                    val newAmount = if (uiState.amount.length <= 1) "0" else uiState.amount.dropLast(1)
                    viewModel.setAmount(newAmount)
                },
                onClearClick = { viewModel.setAmount("0") },
                onEqualsClick = viewModel::calculateResult,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // 错误处理
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    uiState.error?.let { error ->
        LaunchedEffect(error) {
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            // 显示错误信息后清除错误
            viewModel.clearError()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Scaffold内容不变...

        // 在屏幕底部显示Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}

/**
 * 交易类型选择器
 */
@Composable
private fun TransactionTypeSelector(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TransactionTypeButton(
            text = "支出",
            isSelected = selectedType == TransactionType.EXPENSE,
            color = ExpenseRed,
            onClick = { onTypeSelected(TransactionType.EXPENSE) },
            modifier = Modifier.weight(1f)
        )

        TransactionTypeButton(
            text = "收入",
            isSelected = selectedType == TransactionType.INCOME,
            color = IncomeGreen,
            onClick = { onTypeSelected(TransactionType.INCOME) },
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * 交易类型按钮
 */
@Composable
private fun TransactionTypeButton(
    text: String,
    isSelected: Boolean,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) color else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isSelected) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 金额显示
 */
@Composable
private fun AmountDisplay(
    amount: String,
    isValid: Boolean,
    transactionType: TransactionType,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "金额",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${if (transactionType == TransactionType.EXPENSE) "支出" else "收入"}¥$amount",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = if (isValid) {
                    when (transactionType) {
                        TransactionType.INCOME -> IncomeGreen
                        TransactionType.EXPENSE -> ExpenseRed
                    }
                } else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 分类选择
 */
@Composable

private fun CategorySelection(
    categories: List<Category>,
    selectedCategory: Category?,
    onCategorySelected: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "选择分类",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                CategoryChip(
                    category = category,
                    isSelected = selectedCategory?.id == category.id,
                    onClick = { onCategorySelected(category) }
                )
            }
        }
    }
}

/**
 * 日期和备注选择部分
 */
@Composable
private fun DateAndNoteSection(
    selectedDate: Date,
    note: String,
    onDateSelected: (Date) -> Unit,
    onNoteChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // 日期选择器状态
    var showDateDialog by remember { mutableStateOf(false) }
    
    Column(modifier = modifier) {
        // 日期选择
        OutlinedButton(
            onClick = { showDateDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "选择日期"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(formatDate(selectedDate))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 备注输入
        OutlinedTextField(
            value = note,
            onValueChange = onNoteChanged,
            label = { Text("备注（可选）") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )
    }
    
    // 显示日期选择对话框 - 使用自定义实现代替实验性API
    if (showDateDialog) {
        CustomDatePickerDialog(
            selectedDate = selectedDate,
            onDateSelected = { 
                onDateSelected(it)
                showDateDialog = false
            },
            onDismiss = { showDateDialog = false }
        )
    }
}

/**
 * 自定义日期选择对话框
 */
@Composable
private fun CustomDatePickerDialog(
    selectedDate: Date,
    onDateSelected: (Date) -> Unit,
    onDismiss: () -> Unit
) {
    val calendar = Calendar.getInstance().apply { time = selectedDate }
    var year by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var month by remember { mutableStateOf(calendar.get(Calendar.MONTH)) }
    var day by remember { mutableStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择日期") },
        text = {
            Column {
                // 年份选择
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("年份:", modifier = Modifier.width(60.dp))
                    Slider(
                        value = year.toFloat(),
                        onValueChange = { year = it.toInt() },
                        valueRange = 2020f..2030f,
                        steps = 9,
                        modifier = Modifier.weight(1f)
                    )
                    Text(year.toString(), modifier = Modifier.width(50.dp))
                }
                
                // 月份选择
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("月份:", modifier = Modifier.width(60.dp))
                    Slider(
                        value = month.toFloat(),
                        onValueChange = { month = it.toInt() },
                        valueRange = 0f..11f,
                        steps = 11,
                        modifier = Modifier.weight(1f)
                    )
                    Text("${month + 1}", modifier = Modifier.width(50.dp))
                }
                
                // 日期选择
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("日期:", modifier = Modifier.width(60.dp))
                    val maxDay = getMaxDaysInMonth(year, month)
                    Slider(
                        value = day.toFloat().coerceAtMost(maxDay.toFloat()),
                        onValueChange = { day = it.toInt().coerceAtMost(maxDay) },
                        valueRange = 1f..maxDay.toFloat(),
                        steps = maxDay - 1,
                        modifier = Modifier.weight(1f)
                    )
                    Text(day.toString(), modifier = Modifier.width(50.dp))
                }
                
                // 显示选择的日期
                Text(
                    text = "${year}年${month + 1}月${day}日",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val newCalendar = Calendar.getInstance()
                    newCalendar.set(year, month, day.coerceAtMost(getMaxDaysInMonth(year, month)))
                    onDateSelected(newCalendar.time)
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 获取指定年月的最大天数
 */
private fun getMaxDaysInMonth(year: Int, month: Int): Int {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.YEAR, year)
    calendar.set(Calendar.MONTH, month)
    return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
}

/**
 * 格式化日期显示
 */
private fun formatDate(date: Date): String {
    val calendar = Calendar.getInstance()
    calendar.time = date
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH 是从0开始的
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    return "${year}年${month.toString().padStart(2, '0')}月${day.toString().padStart(2, '0')}日"
}
