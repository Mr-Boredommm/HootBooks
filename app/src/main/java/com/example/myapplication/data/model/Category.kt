package com.example.myapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import android.os.Parcelable

/**
 * 分类实体类
 * 用于管理收入和支出的分类信息
 */
@Entity(tableName = "categories")
@Parcelize
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,                    // 分类名称
    val icon: String,                    // 图标名称/资源标识
    val type: TransactionType,           // 分类类型（收入/支出）
    val color: String = "#FF6B6B",       // 分类颜色（十六进制）
    val isDefault: Boolean = false,      // 是否为默认分类
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable

/**
 * 预定义的默认分类
 */
object DefaultCategories {
    val expenseCategories = listOf(
        Category(name = "餐饮", icon = "restaurant", type = TransactionType.EXPENSE, color = "#FF6B6B", isDefault = true),
        Category(name = "交通", icon = "directions_car", type = TransactionType.EXPENSE, color = "#4ECDC4", isDefault = true),
        Category(name = "购物", icon = "shopping_cart", type = TransactionType.EXPENSE, color = "#45B7D1", isDefault = true),
        Category(name = "娱乐", icon = "movie", type = TransactionType.EXPENSE, color = "#96CEB4", isDefault = true),
        Category(name = "生活", icon = "home", type = TransactionType.EXPENSE, color = "#FECA57", isDefault = true),
        Category(name = "医疗", icon = "local_hospital", type = TransactionType.EXPENSE, color = "#FF9FF3", isDefault = true),
        Category(name = "教育", icon = "school", type = TransactionType.EXPENSE, color = "#54A0FF", isDefault = true),
        Category(name = "其他", icon = "more_horiz", type = TransactionType.EXPENSE, color = "#95A5A6", isDefault = true)
    )

    val incomeCategories = listOf(
        Category(name = "工资", icon = "work", type = TransactionType.INCOME, color = "#2ECC71", isDefault = true),
        Category(name = "奖金", icon = "star", type = TransactionType.INCOME, color = "#F39C12", isDefault = true),
        Category(name = "投资", icon = "trending_up", type = TransactionType.INCOME, color = "#3498DB", isDefault = true),
        Category(name = "兼职", icon = "business_center", type = TransactionType.INCOME, color = "#9B59B6", isDefault = true),
        Category(name = "礼金", icon = "card_giftcard", type = TransactionType.INCOME, color = "#E74C3C", isDefault = true),
        Category(name = "其他", icon = "more_horiz", type = TransactionType.INCOME, color = "#1ABC9C", isDefault = true)
    )
}
