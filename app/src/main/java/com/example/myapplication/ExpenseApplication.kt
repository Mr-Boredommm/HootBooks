package com.example.myapplication

import android.app.Application
import com.example.myapplication.data.model.DefaultCategories
import com.example.myapplication.data.repository.ExpenseRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 记账应用程序类
 * 使用Hilt进行依赖注入管理
 */
@HiltAndroidApp
class ExpenseApplication : Application() {

    @Inject
    lateinit var repository: ExpenseRepository

    override fun onCreate() {
        super.onCreate()

        // 初始化默认分类数据
        CoroutineScope(Dispatchers.IO).launch {
            initializeDefaultCategories()
        }
    }

    /**
     * 初始化默认分类数据
     */
    private suspend fun initializeDefaultCategories() {
        try {
            // 检查是否已有分类数据
            repository.getAllCategories().collect { categories ->
                if (categories.isEmpty()) {
                    // 添加默认的支出分类
                    DefaultCategories.expenseCategories.forEach { category ->
                        repository.addCategory(category)
                    }

                    // 添加默认的收入分类
                    DefaultCategories.incomeCategories.forEach { category ->
                        repository.addCategory(category)
                    }
                }
            }
        } catch (e: Exception) {
            // 忽略初始化错误，避免应用崩溃
            e.printStackTrace()
        }
    }
}
