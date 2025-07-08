package com.example.myapplication.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Category
import com.example.myapplication.data.model.Transaction
import com.example.myapplication.data.model.TransactionType
import com.example.myapplication.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.objecthunter.exp4j.ExpressionBuilder
import java.util.Date
import javax.inject.Inject

data class AddTransactionUiState(
    val amount: String = "0",
    val transactionType: TransactionType = TransactionType.EXPENSE,
    val selectedCategory: Category? = null,
    val selectedDate: Date = Date(),
    val note: String = "",
    val isAmountValid: Boolean = true,
    val error: String? = null,
    val transactionId: Long = 0, // 添加交易ID字段，用于区分新增和编辑模式
    val isLoading: Boolean = false // 添加加载状态
)

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState = _uiState.asStateFlow()

    val categories: StateFlow<List<Category>> = expenseRepository.getAllCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setAmount(amount: String) {
        _uiState.update { it.copy(amount = amount, isAmountValid = true) }
    }

    fun setTransactionType(type: TransactionType) {
        _uiState.update { it.copy(transactionType = type, selectedCategory = null) }
    }

    fun setSelectedCategory(category: Category) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun setDate(date: Date) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun setNote(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun getCategoriesForCurrentType(): List<Category> {
        // This is a placeholder. You should replace it with your actual logic.
        return emptyList()
    }

    fun onOperatorClick(operator: String) {
        _uiState.update {
            val currentAmount = it.amount
            if (currentAmount.isNotEmpty() && "+-*/".contains(currentAmount.last())) {
                it.copy(amount = currentAmount.dropLast(1) + operator)
            } else {
                it.copy(amount = currentAmount + operator)
            }
        }
    }

    fun calculateResult() {
        val expression = _uiState.value.amount
        try {
            val result = evaluateExpression(expression)
            _uiState.update {
                it.copy(
                    amount = result.toString(),
                    isAmountValid = result > 0
                )
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Invalid expression") }
        }
    }

    /**
     * 加载交易数据用于编辑
     */
    fun loadTransaction(transactionId: Long) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                // 获取交易数据
                expenseRepository.getTransactionById(transactionId)?.let { transaction ->
                    // 获取分类信息
                    val category = expenseRepository.getCategoryById(transaction.categoryId)

                    // 更新UI状态
                    _uiState.update {
                        it.copy(
                            transactionId = transaction.id,
                            amount = transaction.amount.toString(),
                            transactionType = transaction.type,
                            selectedCategory = category,
                            selectedDate = Date(transaction.date),
                            note = transaction.note,
                            isAmountValid = true,
                            isLoading = false
                        )
                    }
                } ?: run {
                    // 如果找不到交易记录
                    _uiState.update {
                        it.copy(
                            error = "找不到该交易记录",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "加载交易数据失败: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * 保存交易数据（新增或更新）
     * @param isEditMode 是否为编辑模式
     * @param onSuccess 成功回调
     */
    fun saveTransaction(isEditMode: Boolean, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val currentState = _uiState.value

            // 验证输入
            if (currentState.selectedCategory == null) {
                _uiState.update { it.copy(error = "请选择分类") }
                return@launch
            }

            val amountValue = currentState.amount.toDoubleOrNull()
            if (amountValue == null || amountValue <= 0) {
                _uiState.update { it.copy(error = "请输入有效金额") }
                return@launch
            }

            try {
                val transaction = Transaction(
                    id = if (isEditMode) currentState.transactionId else 0, // 编辑模式使用现有ID，新增模式使用0
                    amount = amountValue,
                    type = currentState.transactionType,
                    categoryId = currentState.selectedCategory.id,
                    date = currentState.selectedDate.time,
                    note = currentState.note
                )

                if (isEditMode) {
                    // 更新现有交易
                    expenseRepository.updateTransaction(transaction)
                } else {
                    // 添加新交易
                    expenseRepository.addTransaction(transaction)
                }

                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "保存失败: ${e.message}") }
            }
        }
    }

    private fun evaluateExpression(expression: String): Double {
        return ExpressionBuilder(expression).build().evaluate()
    }

    /**
     * 清除错误信息
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
