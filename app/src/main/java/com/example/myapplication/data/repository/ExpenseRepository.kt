package com.example.myapplication.data.repository

import com.example.myapplication.data.dao.CategoryDao
import com.example.myapplication.data.dao.TransactionDao
import com.example.myapplication.data.model.Category
import com.example.myapplication.data.model.CategoryStatistics
import com.example.myapplication.data.model.Transaction
import com.example.myapplication.data.model.TransactionSummary
import com.example.myapplication.data.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 记账数据仓库
 * 实现Repository模式，统一管理数据访问逻辑
 */
@Singleton
class ExpenseRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao
) {

    // ==================== 交易相关操作 ====================

    /**
     * 获取所有交易记录
     */
    fun getAllTransactions(): Flow<List<Transaction>> = transactionDao.getAllTransactions()

    /**
     * 根据日期范围获取交易记录
     */
    fun getTransactionsByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Transaction>> {
        val startTimestamp = startDate.atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000
        val endTimestamp = endDate.atTime(23, 59, 59).toEpochSecond(ZoneOffset.UTC) * 1000
        return transactionDao.getTransactionsByDateRange(startTimestamp, endTimestamp)
    }

    /**
     * 根据日期范围获取交易记录 (同步版本)
     */
    suspend fun getTransactionsByDateRangeSync(startDate: LocalDate, endDate: LocalDate): List<Transaction> {
        val startTimestamp = startDate.atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000
        val endTimestamp = endDate.atTime(23, 59, 59).toEpochSecond(ZoneOffset.UTC) * 1000
        return transactionDao.getTransactionsByDateRangeSync(startTimestamp, endTimestamp)
    }

    /**
     * 获取最近的交易记录
     */
    fun getRecentTransactions(limit: Int = 10): Flow<List<Transaction>> =
        transactionDao.getRecentTransactions(limit)

    /**
     * 根据ID获取交易记录
     */
    suspend fun getTransactionById(id: Long): Transaction? =
        transactionDao.getTransactionById(id)

    /**
     * 添加交易记录
     */
    suspend fun addTransaction(transaction: Transaction): Long =
        transactionDao.insertTransaction(transaction)

    /**
     * 更新交易记录
     */
    suspend fun updateTransaction(transaction: Transaction) =
        transactionDao.updateTransaction(transaction)

    /**
     * 删除交易记录
     */
    suspend fun deleteTransaction(transaction: Transaction) =
        transactionDao.deleteTransaction(transaction)

    /**
     * 搜索交易记录
     */
    fun searchTransactions(query: String): Flow<List<Transaction>> =
        transactionDao.searchTransactions(query)

    /**
     * 获取第一笔交易记录（按时间最早排序）
     */
    suspend fun getFirstTransaction(): Transaction? {
        return transactionDao.getFirstTransaction()
    }

    // ==================== 分类相关操作 ====================

    /**
     * 获取所有分类
     */
    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()

    /**
     * 根据类型获取分类
     */
    fun getCategoriesByType(type: TransactionType): Flow<List<Category>> =
        categoryDao.getCategoriesByType(type)

    /**
     * 根据ID获取分类
     */
    suspend fun getCategoryById(id: Long): Category? =
        categoryDao.getCategoryById(id)

    /**
     * 添加分类
     */
    suspend fun addCategory(category: Category): Long =
        categoryDao.insertCategory(category)

    /**
     * 更新分类
     */
    suspend fun updateCategory(category: Category) =
        categoryDao.updateCategory(category)

    /**
     * 删除分类
     */
    suspend fun deleteCategory(category: Category) =
        categoryDao.deleteCategory(category)

    // ==================== 统计分析相关 ====================

    /**
     * 获取月度统计摘要
     */
    suspend fun getMonthlySummary(year: Int, month: Int): TransactionSummary {
        val startDate = LocalDate.of(year, month, 1)
        val endDate = startDate.withDayOfMonth(startDate.lengthOfMonth())

        val startTimestamp = startDate.atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000
        val endTimestamp = endDate.atTime(23, 59, 59).toEpochSecond(ZoneOffset.UTC) * 1000

        val summaryResults = transactionDao.getMonthlySummary(startTimestamp, endTimestamp)

        var totalIncome = 0.0
        var totalExpense = 0.0
        var transactionCount = 0

        summaryResults.forEach { result ->
            when (result.type) {
                TransactionType.INCOME -> totalIncome = result.total
                TransactionType.EXPENSE -> totalExpense = result.total
            }
            transactionCount += result.count
        }

        return TransactionSummary(totalIncome, totalExpense, transactionCount)
    }

    /**
     * 获取分类统计数据
     */
    suspend fun getCategoryStatistics(
        type: TransactionType,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<CategoryStatistics> {
        val startTimestamp = startDate.atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000
        val endTimestamp = endDate.atTime(23, 59, 59).toEpochSecond(ZoneOffset.UTC) * 1000
        val results = transactionDao.getCategoryStatistics(type, startTimestamp, endTimestamp)

        // 转换CategoryStatisticsResult到CategoryStatistics
        return results.map { result ->
            val category = categoryDao.getCategoryById(result.categoryId)
                ?: Category(
                    id = result.categoryId,
                    name = "未知分类",
                    icon = "help_outline",
                    type = type,
                    color = "#666666"
                )

            CategoryStatistics(
                category = category,
                totalAmount = result.totalAmount,
                transactionCount = result.transactionCount
            )
        }
    }

    /**
     * 根据月份获取交易记录
     */
    fun getTransactionsByMonth(year: Int, month: Int): Flow<List<Transaction>> {
        val startDate = LocalDate.of(year, month, 1)
        val endDate = startDate.withDayOfMonth(startDate.lengthOfMonth())
        return getTransactionsByDateRange(startDate, endDate)
    }

    /**
     * 获取当前月度统计的Flow
     */
    fun getCurrentMonthlySummaryFlow(): Flow<TransactionSummary> {
        val now = LocalDate.now()
        return getTransactionsByMonth(now.year, now.monthValue)
            .map { transactions ->
                val income = transactions.filter { it.type == TransactionType.INCOME }
                    .sumOf { it.amount }
                val expense = transactions.filter { it.type == TransactionType.EXPENSE }
                    .sumOf { it.amount }
                TransactionSummary(income, expense, transactions.size)
            }
    }

    /**
     * 获取年度统计数据
     */
    suspend fun getYearlySummary(year: Int): TransactionSummary {
        val startDate = LocalDate.of(year, 1, 1)
        val endDate = LocalDate.of(year, 12, 31)

        val startTimestamp = startDate.atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000
        val endTimestamp = endDate.atTime(23, 59, 59).toEpochSecond(ZoneOffset.UTC) * 1000

        val summaryResults = transactionDao.getMonthlySummary(startTimestamp, endTimestamp)

        var totalIncome = 0.0
        var totalExpense = 0.0
        var transactionCount = 0

        summaryResults.forEach { result ->
            when (result.type) {
                TransactionType.INCOME -> totalIncome = result.total
                TransactionType.EXPENSE -> totalExpense = result.total
            }
            transactionCount += result.count
        }

        return TransactionSummary(totalIncome, totalExpense, transactionCount)
    }

    // ==================== 数据验证和业务逻辑 ====================

    /**
     * 验证交易数据
     */
    fun validateTransaction(transaction: Transaction): Boolean {
        return transaction.amount > 0 && transaction.categoryId > 0
    }

    /**
     * 获取分类使用统计（用于删除前检查）
     */
    suspend fun getCategoryUsageCount(categoryId: Long): Int =
        transactionDao.getTransactionCountByCategory(categoryId)
}
