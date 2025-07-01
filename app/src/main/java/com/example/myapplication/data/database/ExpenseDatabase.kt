package com.example.myapplication.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplication.data.dao.CategoryDao
import com.example.myapplication.data.dao.TransactionDao
import com.example.myapplication.data.model.Category
import com.example.myapplication.data.model.DefaultCategories
import com.example.myapplication.data.model.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Provider

/**
 * 记账应用数据库
 * 使用Room ORM框架管理本地数据存储
 */
@Database(
    entities = [Transaction::class, Category::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class ExpenseDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao

    /**
     * 数据库创建时的回调
     * 用于初始化默认数据
     */
    class DatabaseCallback(
        private val database: Provider<ExpenseDatabase>,
        private val applicationScope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            // 在数据库创建时插入默认分类
            applicationScope.launch(Dispatchers.IO) {
                populateDatabase()
            }
        }

        /**
         * 填充默认数据
         */
        private suspend fun populateDatabase() {
            val categoryDao = database.get().categoryDao()

            // 检查是否已存在默认分类
            if (categoryDao.getDefaultCategoriesCount() == 0) {
                // 插入默认支出分类
                categoryDao.insertCategories(DefaultCategories.expenseCategories)
                // 插入默认收入分类
                categoryDao.insertCategories(DefaultCategories.incomeCategories)
            }
        }
    }
}
