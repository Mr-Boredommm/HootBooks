package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Category
import com.example.myapplication.data.model.Transaction
import com.example.myapplication.data.model.TransactionType
import com.example.myapplication.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneOffset
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
    }

    /**
     * 加载分类数据
     */
    private fun loadCategories() {
        viewModelScope.launch {
            repository.getAllCategories().collect { categoryList ->
                _categories.value = categoryList
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
    fun setDate(date: LocalDate) {
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
                    date = state.selectedDate.atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000
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
    val selectedDate: LocalDate = LocalDate.now(),
    val isLoading: Boolean = false,
    val error: String? = null
)
