package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Category
import com.example.myapplication.data.model.Transaction
import com.example.myapplication.data.model.TransactionSummary
import com.example.myapplication.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * 主页面ViewModel
 * 管理主页面的UI状态和业务逻辑
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _recentTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    val recentTransactions: StateFlow<List<Transaction>> = _recentTransactions.asStateFlow()

    private val _monthlySummary = MutableStateFlow(TransactionSummary())
    val monthlySummary: StateFlow<TransactionSummary> = _monthlySummary.asStateFlow()

    private val _categories = MutableStateFlow<Map<Long, Category>>(emptyMap())
    val categories: StateFlow<Map<Long, Category>> = _categories.asStateFlow()

    init {
        loadData()
        loadCategories()
    }

    /**
     * 加载主页数据
     */
    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // 并行加载数据
                launch {
                    repository.getRecentTransactions(10)
                        .catch { e ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = e.message
                            )
                        }
                        .collect { transactions ->
                            _recentTransactions.value = transactions
                        }
                }

                launch {
                    repository.getCurrentMonthlySummaryFlow()
                        .catch { e ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = e.message
                            )
                        }
                        .collect { summary ->
                            _monthlySummary.value = summary
                        }
                }

                // 设置加载完成状态
                _uiState.value = _uiState.value.copy(isLoading = false)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    /**
     * 加载分类数据
     */
    private fun loadCategories() {
        viewModelScope.launch {
            try {
                repository.getAllCategories()
                    .catch { e ->
                        _uiState.value = _uiState.value.copy(
                            error = e.message
                        )
                    }
                    .collect { categoryList ->
                        // 将分类列表转换为ID映射的Map，方便快速查找
                        _categories.value = categoryList.associateBy { it.id }
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    /**
     * 获取分类名称
     */
    fun getCategoryName(categoryId: Long): String {
        return _categories.value[categoryId]?.name ?: "未知分类"
    }

    /**
     * 删除交易记录
     */
    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                repository.deleteTransaction(transaction)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    /**
     * 刷新数据
     */
    fun refresh() {
        loadData()
        loadCategories()
    }

    /**
     * 清除错误状态
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * 主页面UI状态
 */
data class HomeUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)
