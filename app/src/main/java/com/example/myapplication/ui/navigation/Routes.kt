package com.example.myapplication.ui.navigation

/**
 * 应用导航路由
 * 定义应用中所有页面的导航路径
 */
object Routes {
    const val HOME = "home"
    const val ADD_TRANSACTION = "add_transaction"
    const val STATISTICS = "statistics"
    const val CATEGORIES = "categories"
    const val SETTINGS = "settings"
    const val TRANSACTION_DETAIL = "transaction_detail/{transactionId}"

    fun transactionDetail(transactionId: Long) = "transaction_detail/$transactionId"
}
