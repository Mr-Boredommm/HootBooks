package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Category
import com.example.myapplication.data.model.DefaultCategories
import com.example.myapplication.data.model.Transaction
import com.example.myapplication.data.model.TransactionType
import com.example.myapplication.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

/**
 * 添加交易ViewModel
 * 管理添加/编辑交易的UI状态和业务逻辑
 */
@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    init {
        loadCategories()
        // 确保UI状态初始化时就有默认的交易类型和初始数据
        _uiState.value = _uiState.value.copy(
            transactionType = TransactionType.EXPENSE,
            amount = "0"
        )
    }

    /**
     * 加载分类数据
     */
    private fun loadCategories() {
        viewModelScope.launch {
            try {
                // 先确保有默认分类数据
                ensureDefaultCategories()

                repository.getAllCategories().collect { categoryList ->
                    _categories.value = categoryList
                    // 移除未使用的currentTypeCategories变量
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "加载分类数据失败: ${e.message}"
                )
                // 如果加载失败，尝试创建默认分类
                ensureDefaultCategories()
            }
        }
    }

    /**
     * 确保默认分类存在
     */
    private suspend fun ensureDefaultCategories() {
        try {
            // 获取当前分类列表
            val categories = repository.getAllCategories().first()

            if (categories.isEmpty()) {
                // 如果没有分类，添加默认分类
                DefaultCategories.expenseCategories.forEach { category ->
                    repository.addCategory(category)
                }
                DefaultCategories.incomeCategories.forEach { category ->
                    repository.addCategory(category)
                }
            }
        } catch (e: Exception) {
            // 如果出错，直接添加默认分类
            try {
                DefaultCategories.expenseCategories.forEach { category ->
                    repository.addCategory(category)
                }
                DefaultCategories.incomeCategories.forEach { category ->
                    repository.addCategory(category)
                }
            } catch (addError: Exception) {
                e.printStackTrace()
                addError.printStackTrace()
            }
        }
    }

    /**
     * 设置交易类型
     */
    fun setTransactionType(type: TransactionType) {
        _uiState.value = _uiState.value.copy(
            transactionType = type,
            selectedCategory = null // 重置分类选择
        )
    }

    /**
     * 设置金额
     */
    fun setAmount(amount: String) {
        val doubleAmount = amount.toDoubleOrNull()
        _uiState.value = _uiState.value.copy(
            amount = amount,
            isAmountValid = doubleAmount != null && doubleAmount > 0
        )
    }

    /**
     * 设置选中的分类
     */
    fun setSelectedCategory(category: Category) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    /**
     * 设置备注
     */
    fun setNote(note: String) {
        _uiState.value = _uiState.value.copy(note = note)
    }

    /**
     * 设置日期
     */
    fun setDate(date: Date) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
    }

    /**
     * 检查表单是否有效
     */
    private fun isFormValid(): Boolean {
        val state = _uiState.value
        return state.isAmountValid &&
                state.selectedCategory != null &&
                state.amount.isNotBlank()
    }

    /**
     * 保存交易记录
     */
    fun saveTransaction(onSuccess: () -> Unit) {
        if (!isFormValid()) {
            _uiState.value = _uiState.value.copy(
                error = "请填写完整的交易信息"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val state = _uiState.value
                val transaction = Transaction(
                    amount = state.amount.toDouble(),
                    categoryId = state.selectedCategory!!.id,
                    type = state.transactionType,
                    note = state.note,
                    date = state.selectedDate.time // 使用Date.time获取时间戳
                )

                repository.addTransaction(transaction)

                _uiState.value = _uiState.value.copy(isLoading = false)
                onSuccess()

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "保存失败"
                )
            }
        }
    }

    /**
     * 清除错误状态
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * 重置表单
     */
    fun resetForm() {
        _uiState.value = AddTransactionUiState()
    }

    /**
     * 获取当前类型的分类列表
     */
    fun getCategoriesForCurrentType(): List<Category> {
        return _categories.value.filter { it.type == _uiState.value.transactionType }
    }
}

/**
 * 添加交易UI状态
 */
data class AddTransactionUiState(
    val transactionType: TransactionType = TransactionType.EXPENSE,
    val amount: String = "",
    val isAmountValid: Boolean = false,
    val selectedCategory: Category? = null,
    val note: String = "",
    val selectedDate: Date = Date(),
    val isLoading: Boolean = false,
    val error: String? = null
)
