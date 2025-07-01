package com.example.myapplication.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.data.model.Transaction
import com.example.myapplication.data.model.TransactionType
import com.example.myapplication.ui.component.TransactionItem
import com.example.myapplication.ui.component.MonthlySummaryCard
import com.example.myapplication.ui.theme.IncomeGreen
import com.example.myapplication.ui.theme.ExpenseRed
import com.example.myapplication.ui.viewmodel.HomeViewModel

/**
 * 主页面
 * 显示月度统计和最近交易记录
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToTransactionDetail: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val recentTransactions by viewModel.recentTransactions.collectAsStateWithLifecycle()
    val monthlySummary by viewModel.monthlySummary.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "记账本",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    TextButton(
                        onClick = onNavigateToStatistics
                    ) {
                        Text("统计")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddTransaction,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加交易"
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 月度统计卡片
            item {
                MonthlySummaryCard(
                    summary = monthlySummary,
                    onClick = onNavigateToStatistics
                )
            }

            // 最近交易标题
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "最近交易",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    TextButton(
                        onClick = { /* TODO: Navigate to all transactions */ }
                    ) {
                        Text("查看全部")
                    }
                }
            }

            // 交易列表
            if (recentTransactions.isEmpty() && !uiState.isLoading) {
                item {
                    EmptyTransactionState(
                        onAddTransaction = onNavigateToAddTransaction
                    )
                }
            } else {
                items(recentTransactions) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        onClick = { onNavigateToTransactionDetail(transaction.id) },
                        onDelete = { viewModel.deleteTransaction(transaction) }
                    )
                }
            }
        }
    }

    // 错误处理
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // TODO: Show snackbar or error dialog
        }
    }

    // 加载状态
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

/**
 * 空状态组件
 */
@Composable
private fun EmptyTransactionState(
    onAddTransaction: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "还没有交易记录",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "点击下方按钮开始记账",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onAddTransaction
            ) {
                Text("开始记账")
            }
        }
    }
}
