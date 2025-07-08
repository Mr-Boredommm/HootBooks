package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.TransactionType
import com.example.myapplication.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

/**
 * 统计数据UI状态
 */
data class StatisticsUiState(
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * 月度统计数据
 */
data class MonthlyStats(
    val yearMonth: YearMonth,
    val income: Double,
    val expense: Double
) {
    fun getMonthName(): String {
        return yearMonth.format(DateTimeFormatter.ofPattern("yyyy年MM月", Locale.CHINESE))
    }

    fun getFormattedIncome(): String = "¥%.2f".format(income)
    fun getFormattedExpense(): String = "¥%.2f".format(expense)
    fun getFormattedBalance(): String = "¥%.2f".format(income - expense)
}

/**
 * 分类统计数据
 */
data class CategoryStats(
    val categoryId: Long,
    val categoryName: String,
    val type: TransactionType,
    val amount: Double,
    val percentage: Double,
    val iconName: String
) {
    fun getFormattedAmount(): String = "¥%.2f".format(amount)
    fun getFormattedPercentage(): String = "%.1f%%".format(percentage)
}

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
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
        loadMonthlyStats()
        loadCategoryStats()
    }

    /**
     * 加载月度统计数据
     */
    private fun loadMonthlyStats() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                // 获取第一笔交易记录的时间
                val firstTransactionDate = try {
                    val firstTransaction = expenseRepository.getFirstTransaction()
                    if (firstTransaction != null) {
                        // 从时间戳获取 YearMonth
                        val instant = java.time.Instant.ofEpochMilli(firstTransaction.date)
                        val localDate = instant.atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                        YearMonth.from(localDate)
                    } else {
                        // 如果没有交易记录，则默认显示最近6个月
                        YearMonth.now().minusMonths(5)
                    }
                } catch (e: Exception) {
                    // 如果出错，则默认显示最近6个月
                    println("获取第一笔交易记录失败: ${e.message}")
                    YearMonth.now().minusMonths(5)
                }

                // 获取当前月份
                val currentMonth = YearMonth.now()

                // 计算需要显示的月份数量（使用更基础的方法计算）
                val yearDiff = currentMonth.year - firstTransactionDate.year
                val monthDiff = currentMonth.monthValue - firstTransactionDate.monthValue
                val totalMonths = yearDiff * 12 + monthDiff
                val monthCount = if (totalMonths < 0) -totalMonths + 1 else totalMonths + 1

                // 生成所有需要显示的月份
                val months = (0 until monthCount).map { currentMonth.minusMonths(it.toLong()) }

                println("显示从 ${firstTransactionDate.year}年${firstTransactionDate.monthValue}月 到 ${currentMonth.year}年${currentMonth.monthValue}月 的统计数据")

                val result = mutableListOf<MonthlyStats>()

                for (month in months) {
                    val startDate = month.atDay(1)
                    val endDate = month.atEndOfMonth()

                    try {
                        // 获取该月交易数据
                        val transactions = expenseRepository.getTransactionsByDateRangeSync(startDate, endDate)
                        val income = transactions
                            .filter { it.type == TransactionType.INCOME }
                            .sumOf { it.amount }

                        val expense = transactions
                            .filter { it.type == TransactionType.EXPENSE }
                            .sumOf { it.amount }

                        result.add(
                            MonthlyStats(
                                yearMonth = month,
                                income = income,
                                expense = expense
                            )
                        )
                    } catch (e: Exception) {
                        // 单个月份加载失败，添加一个空的统计数据
                        println("加载 ${month.year}年${month.monthValue}月 数据失败: ${e.message}")
                        result.add(
                            MonthlyStats(
                                yearMonth = month,
                                income = 0.0,
                                expense = 0.0
                            )
                        )
                    }
                }

                // 按月份排序（从近到远）并且打印日志
                val sortedResult = result.sortedByDescending { it.yearMonth }
                sortedResult.forEach {
                    println("月度统计: ${it.yearMonth.year}年${it.yearMonth.monthValue}月, 收入: ${it.income}, 支出: ${it.expense}")
                }
                _monthlyStats.value = sortedResult
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "加载月度统计数据失败: ${e.message}") }
            }
        }
    }

    /**
     * 加载分类统计数据
     */
    private fun loadCategoryStats() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                val month = _selectedMonth.value
                val startDate = month.atDay(1)
                val endDate = month.atEndOfMonth()

                // 获取该月交易数据
                expenseRepository.getTransactionsByDateRange(startDate, endDate)
                    .collect { transactions ->
                        // 获取所有分类
                        expenseRepository.getAllCategories().collect { categories ->
                            val categoryMap = categories.associateBy { it.id }

                            // 按类型和分类统计金额
                            val statsByCategory = transactions.groupBy {
                                it.type to it.categoryId
                            }.map { (key, txs) ->
                                val (type, categoryId) = key
                                val category = categoryMap[categoryId]

                                val amount = txs.sumOf { it.amount }

                                // 计算在同类型中的百分比
                                val totalAmount = transactions
                                    .filter { it.type == type }
                                    .sumOf { it.amount }

                                val percentage = if (totalAmount > 0) {
                                    (amount / totalAmount) * 100
                                } else {
                                    0.0
                                }

                                CategoryStats(
                                    categoryId = categoryId,
                                    categoryName = category?.name ?: "未知分类",
                                    type = type,
                                    amount = amount,
                                    percentage = percentage,
                                    iconName = category?.icon ?: "help_outline"
                                )
                            }.sortedByDescending { it.amount }

                            _categoryStats.value = statsByCategory
                            _uiState.update { it.copy(isLoading = false) }
                        }
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "加载分类统计数据失败: ${e.message}") }
            }
        }
    }

    /**
     * 选择月份
     */
    fun selectMonth(yearMonth: YearMonth) {
        _selectedMonth.value = yearMonth
        loadCategoryStats() // 重新加载所选月份的分类统计
    }

    /**
     * 清除错误信息
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
