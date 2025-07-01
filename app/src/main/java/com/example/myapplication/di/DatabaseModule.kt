package com.example.myapplication.di

import android.content.Context
import androidx.room.Room
import com.example.myapplication.data.dao.CategoryDao
import com.example.myapplication.data.dao.TransactionDao
import com.example.myapplication.data.database.ExpenseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Provider
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * 数据库模块
 * 提供数据库相关的依赖注入
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob())

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        @ApplicationScope applicationScope: CoroutineScope,
        databaseProvider: Provider<ExpenseDatabase>
    ): ExpenseDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            ExpenseDatabase::class.java,
            "expense_database"
        )
        .addCallback(ExpenseDatabase.DatabaseCallback(databaseProvider, applicationScope))
        .build()
    }

    @Provides
    fun provideTransactionDao(database: ExpenseDatabase): TransactionDao =
        database.transactionDao()

    @Provides
    fun provideCategoryDao(database: ExpenseDatabase): CategoryDao =
        database.categoryDao()
}

/**
 * 应用作用域注解
 * 用于标识应用程序级别的协程作用域
 */
@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope
