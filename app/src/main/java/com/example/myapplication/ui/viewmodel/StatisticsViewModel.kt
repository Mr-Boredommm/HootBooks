package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Transaction
import com.example.myapplication.data.model.TransactionType
import com.example.myapplication.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

/**
 * 统计页面ViewModel
 * 管理统计数据的加载和处理
 */
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    private val _monthlyStats = MutableStateFlow<List<MonthlyStats>>(emptyList())
    val monthlyStats: StateFlow<List<MonthlyStats>> = _monthlyStats.asStateFlow()

    private val _categoryStats = MutableStateFlow<List<CategoryStats>>(emptyList())
    val categoryStats: StateFlow<List<CategoryStats>> = _categoryStats.asStateFlow()

    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()

    init {
        loadStatistics()
    }

    /**
     * 加载统计数据
     */
    private fun loadStatistics() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // 加载月度统计
                loadMonthlyStatistics()

                // 加载分类统计
                loadCategoryStatistics()

                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    /**
     * 加载月度统计数据
     */
    private suspend fun loadMonthlyStatistics() {
        val currentMonth = YearMonth.now()
        val months = (0..5).map { currentMonth.minusMonths(it.toLong()) }

        val monthlyStatsList = mutableListOf<MonthlyStats>()

        months.forEach { month ->
            repository.getTransactionsByMonth(month.year, month.monthValue)
                .catch { /* 忽略错误，继续处理其他月份 */ }
                .collect { transactions ->
                    val income = transactions.filter { it.type == TransactionType.INCOME }
                        .sumOf { it.amount }
                    val expense = transactions.filter { it.type == TransactionType.EXPENSE }
                        .sumOf { it.amount }

                    monthlyStatsList.add(
                        MonthlyStats(
                            month = month,
                            income = income,
                            expense = expense,
                            balance = income - expense,
                            transactionCount = transactions.size
                        )
                    )
                }
        }

        _monthlyStats.value = monthlyStatsList.sortedByDescending { it.month }
    }

    /**
     * 加载分类统计数据
     */
    private suspend fun loadCategoryStatistics() {
        val month = _selectedMonth.value
        repository.getTransactionsByMonth(month.year, month.monthValue)
            .catch { e ->
                _uiState.value = _uiState.value.copy(error = e.message)
            }
            .collect { transactions ->
                val categoryMap = mutableMapOf<Long, CategoryStats>()

                transactions.forEach { transaction ->
                    val existing = categoryMap[transaction.categoryId]
                    if (existing != null) {
                        categoryMap[transaction.categoryId] = existing.copy(
                            amount = existing.amount + transaction.amount,
                            transactionCount = existing.transactionCount + 1
                        )
                    } else {
                        // 获取分类信息
                        val category = repository.getCategoryById(transaction.categoryId)
                        val categoryName = category?.name ?: "未知分类"

                        categoryMap[transaction.categoryId] = CategoryStats(
                            categoryId = transaction.categoryId,
                            categoryName = categoryName,
                            amount = transaction.amount,
                            transactionCount = 1,
                            type = transaction.type
                        )
                    }
                }

                _categoryStats.value = categoryMap.values.toList()
                    .sortedByDescending { it.amount }
            }
    }

    /**
     * 选择月份
     */
    fun selectMonth(month: YearMonth) {
        _selectedMonth.value = month
        viewModelScope.launch {
            loadCategoryStatistics()
        }
    }

    /**
     * 刷新数据
     */
    fun refresh() {
        loadStatistics()
    }

    /**
     * 清除错误状态
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * 统计页面UI状态
 */
data class StatisticsUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * 月度统计数据
 */
data class MonthlyStats(
    val month: YearMonth,
    val income: Double,
    val expense: Double,
    val balance: Double,
    val transactionCount: Int
) {
    fun getFormattedIncome(): String = "¥${String.format("%.2f", income)}"
    fun getFormattedExpense(): String = "¥${String.format("%.2f", expense)}"
    fun getFormattedBalance(): String {
        val symbol = if (balance >= 0) "+" else ""
        return "$symbol¥${String.format("%.2f", balance)}"
    }
    fun getMonthName(): String = "${month.year}年${month.monthValue}月"
}

/**
 * 分类统计数据
 */
data class CategoryStats(
    val categoryId: Long,
    val categoryName: String,
    val amount: Double,
    val transactionCount: Int,
    val type: TransactionType
) {
    fun getFormattedAmount(): String = "¥${String.format("%.2f", amount)}"
    fun getPercentage(total: Double): Float =
        if (total > 0) (amount / total * 100).toFloat() else 0f
}
