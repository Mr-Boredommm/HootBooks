package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.model.Transaction
import com.example.myapplication.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

/**
 * 交易数据访问对象
 * 提供交易记录相关的数据库操作
 */
@Dao
interface TransactionDao {

    /**
     * 获取所有交易记录
     */
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    /**
     * 根据日期范围获取交易记录
     */
    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>>

    /**
     * 根据类型获取交易记录
     */
    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>>

    /**
     * 根据分类获取交易记录
     */
    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId ORDER BY date DESC")
    fun getTransactionsByCategory(categoryId: Long): Flow<List<Transaction>>

    /**
     * 根据ID获取交易记录
     */
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): Transaction?

    /**
     * 插入交易记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    /**
     * 更新交易记录
     */
    @Update
    suspend fun updateTransaction(transaction: Transaction)

    /**
     * 删除交易记录
     */
    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    /**
     * 获取月度统计数据
     */
    @Query("""
        SELECT 
            type,
            SUM(amount) as total,
            COUNT(*) as count
        FROM transactions 
        WHERE date BETWEEN :startDate AND :endDate 
        GROUP BY type
    """)
    suspend fun getMonthlySummary(startDate: Long, endDate: Long): List<MonthlySummaryResult>

    /**
     * 获取分类统计数据
     */
    @Query("""
        SELECT 
            t.categoryId,
            SUM(t.amount) as totalAmount,
            COUNT(*) as transactionCount
        FROM transactions t
        WHERE t.type = :type AND t.date BETWEEN :startDate AND :endDate
        GROUP BY t.categoryId
        ORDER BY totalAmount DESC
    """)
    suspend fun getCategoryStatistics(
        type: TransactionType,
        startDate: Long,
        endDate: Long
    ): List<CategoryStatisticsResult>

    /**
     * 获取日统计数据（用于图表）
     */
    @Query("""
        SELECT 
            DATE(date/1000, 'unixepoch') as date,
            type,
            SUM(amount) as amount
        FROM transactions 
        WHERE date BETWEEN :startDate AND :endDate
        GROUP BY DATE(date/1000, 'unixepoch'), type
        ORDER BY date
    """)
    suspend fun getDailyStatistics(startDate: Long, endDate: Long): List<DailyStatisticsResult>

    /**
     * 搜索交易记录
     */
    @Query("""
        SELECT * FROM transactions 
        WHERE note LIKE '%' || :query || '%' 
        ORDER BY date DESC
    """)
    fun searchTransactions(query: String): Flow<List<Transaction>>

    /**
     * 获取最近的交易记录
     */
    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int = 10): Flow<List<Transaction>>
}

/**
 * 月度统计结果数据类
 */
data class MonthlySummaryResult(
    val type: TransactionType,
    val total: Double,
    val count: Int
)

/**
 * 分类统计结果数据类
 */
data class CategoryStatisticsResult(
    val categoryId: Long,
    val totalAmount: Double,
    val transactionCount: Int
)

/**
 * 日统计结果数据类
 */
data class DailyStatisticsResult(
    val date: String,
    val type: TransactionType,
    val amount: Double
)
