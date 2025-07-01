package com.example.myapplication.data.database

import androidx.room.TypeConverter
import com.example.myapplication.data.model.TransactionType

/**
 * Room数据库类型转换器
 * 用于将自定义类型转换为Room支持的基本类型
 */
class Converters {

    /**
     * 将TransactionType枚举转换为字符串
     */
    @TypeConverter
    fun fromTransactionType(type: TransactionType): String {
        return type.name
    }

    /**
     * 将字符串转换为TransactionType枚举
     */
    @TypeConverter
    fun toTransactionType(type: String): TransactionType {
        return TransactionType.valueOf(type)
    }
}
