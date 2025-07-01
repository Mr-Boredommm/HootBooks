package com.example.myapplication.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * 交易记录实体类
 * 记录每一笔收入或支出的详细信息
 */
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@Parcelize
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,                          // 金额
    val categoryId: Long,                        // 分类ID
    val type: TransactionType,                   // 交易类型
    val note: String = "",                       // 备注信息
    val date: Long = System.currentTimeMillis(), // 交易日期（时间戳）
    val createdAt: Long = System.currentTimeMillis() // 创建时间
) : Parcelable {

    /**
     * 获取格式化的金额字符串
     */
    fun getFormattedAmount(): String {
        val symbol = if (type == TransactionType.INCOME) "+" else "-"
        return "$symbol¥${String.format("%.2f", amount)}"
    }

    /**
     * 获取交易日期的LocalDateTime对象
     */
    fun getDateTime(): LocalDateTime {
        return LocalDateTime.ofEpochSecond(date / 1000, 0, ZoneOffset.UTC)
    }
}

/**
 * 交易统计数据类
 * 用于展示统计信息
 */
data class TransactionSummary(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val transactionCount: Int = 0
) {
    val balance: Double get() = totalIncome - totalExpense

    fun getFormattedIncome(): String = "+¥${String.format("%.2f", totalIncome)}"
    fun getFormattedExpense(): String = "-¥${String.format("%.2f", totalExpense)}"
    fun getFormattedBalance(): String {
        val symbol = if (balance >= 0) "+" else ""
        return "$symbol¥${String.format("%.2f", balance)}"
    }
}

/**
 * 分类统计数据类
 * 用于分类统计图表展示
 */
data class CategoryStatistics(
    val category: Category,
    val totalAmount: Double,
    val transactionCount: Int,
    val percentage: Float = 0f
) {
    fun getFormattedAmount(): String = "¥${String.format("%.2f", totalAmount)}"
}
