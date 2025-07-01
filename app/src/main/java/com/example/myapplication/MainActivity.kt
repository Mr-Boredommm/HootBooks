package com.example.myapplication

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.navigation.ExpenseNavGraph
import com.example.myapplication.ui.theme.MyApplicationTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * 记账应用主Activity
 * 使用Jetpack Compose和Hilt依赖注入
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 设置固定的显示密度，确保UI一致性
        setFixedDensity()

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ExpenseApp()
            }
        }
    }

    /**
     * 设置固定的显示密度
     * 确保在所有设备上都有一致的UI显示效果
     */
    private fun setFixedDensity() {
        val displayMetrics = resources.displayMetrics

        // 设置目标密度为2.0，这相当于中等偏小的显示尺寸
        // 这个值可以根据实际效果进行调整
        val targetDensity = 2.7f
        val targetDensityDpi = (160 * targetDensity).toInt()
        val targetScaledDensity = targetDensity

        displayMetrics.density = targetDensity
        displayMetrics.densityDpi = targetDensityDpi
        displayMetrics.scaledDensity = targetScaledDensity
    }

    /**
     * 处理配置变化
     * 在这里可以根据需要重新设置显示密度
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // 这里可以根据新的配置重新设置密度
        setFixedDensity()
    }
}

/**
 * 记账应用主组件
 */
@Composable
fun ExpenseApp() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        val navController = rememberNavController()
        ExpenseNavGraph(navController = navController)
    }
}