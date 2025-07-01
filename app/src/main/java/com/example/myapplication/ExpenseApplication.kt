package com.example.myapplication

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * 记账应用程序类
 * 使用Hilt进行依赖注入管理
 */
@HiltAndroidApp
class ExpenseApplication : Application()
