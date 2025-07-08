package com.example.myapplication.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.Transaction
import com.example.myapplication.data.model.TransactionType
import com.example.myapplication.ui.theme.ExpenseRed
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 每日交易卡片组件
 * 显示一天内的所有交易记录和当天的总支出
 */
@Composable
fun DailyTransactionCard(
    date: LocalDate,
    transactions: List<Transaction>,
    getCategoryName: (Long) -> String,
    onTransactionClick: (Long) -> Unit,
    onDeleteTransaction: (Transaction) -> Unit,
    modifier: Modifier = Modifier
) {
    val dailyExpense = transactions
        .filter { it.type == TransactionType.EXPENSE }
        .sumOf { it.amount }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 日期和当日总支出
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatDailyDate(date),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "支出: ¥${String.format("%.2f", dailyExpense)}",
                    style = MaterialTheme.typography.titleSmall,
                    color = ExpenseRed,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            // 交易列表
            transactions.forEach { transaction ->
                DailyTransactionItem(
                    transaction = transaction,
                    categoryName = getCategoryName(transaction.categoryId),
                    onClick = { onTransactionClick(transaction.id) },
                    onDelete = { onDeleteTransaction(transaction) }
                )
                if (transaction != transactions.last()) {
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

/**
 * 每日交易项组件
 * 简化版的交易项，用于显示在每日卡片中
 */
@Composable
private fun DailyTransactionItem(
    transaction: Transaction,
    categoryName: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // 分类和备注
            Text(
                text = categoryName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            if (transaction.note.isNotBlank()) {
                Text(
                    text = transaction.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 时间
            Text(
                text = formatTransactionTime(transaction.date),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 金额
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = transaction.getFormattedAmount(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = when (transaction.type) {
                    TransactionType.INCOME -> com.example.myapplication.ui.theme.IncomeGreen
                    TransactionType.EXPENSE -> ExpenseRed
                }
            )

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 格式化日期显示
 */
private fun formatDailyDate(date: LocalDate): String {
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)

    return when {
        date.isEqual(today) -> "今天 (${date.format(DateTimeFormatter.ofPattern("MM月dd日"))})"
        date.isEqual(yesterday) -> "昨天 (${date.format(DateTimeFormatter.ofPattern("MM月dd日"))})"
        date.year == today.year -> date.format(DateTimeFormatter.ofPattern("MM月dd日 EEEE"))
        else -> date.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 EEEE"))
    }
}

/**
 * 格式化交易时间
 */
private fun formatTransactionTime(timestamp: Long): String {
    val dateTime = Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()

    return dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
}

/**
 * 获取交易的日期（仅日期部分）
 */
fun Transaction.getLocalDate(): LocalDate {
    return Instant.ofEpochMilli(this.date)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}
