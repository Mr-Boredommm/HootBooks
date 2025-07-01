package com.example.myapplication.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.myapplication.ui.screen.HomeScreen
import com.example.myapplication.ui.screen.AddTransactionScreen
import com.example.myapplication.ui.screen.StatisticsScreen

/**
 * 应用导航图
 * 定义应用的导航结构和页面路由
 */
@Composable
fun ExpenseNavGraph(
    navController: NavHostController,
    startDestination: String = Routes.HOME
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // 主页
        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToAddTransaction = {
                    navController.navigate(Routes.ADD_TRANSACTION)
                },
                onNavigateToStatistics = {
                    navController.navigate(Routes.STATISTICS)
                },
                onNavigateToTransactionDetail = { transactionId ->
                    navController.navigate(Routes.transactionDetail(transactionId))
                }
            )
        }

        // 添加交易页面
        composable(Routes.ADD_TRANSACTION) {
            AddTransactionScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // 统计页面
        composable(Routes.STATISTICS) {
            StatisticsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // 交易详情页面（预留）
        composable(
            route = Routes.TRANSACTION_DETAIL,
            arguments = listOf(
                navArgument("transactionId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getLong("transactionId") ?: 0L
            // TODO: 实现交易详情页面
            // TransactionDetailScreen(
            //     transactionId = transactionId,
            //     onNavigateBack = { navController.popBackStack() }
            // )
        }
    }
}
