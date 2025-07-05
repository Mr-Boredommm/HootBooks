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
    val error: String? = null
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

    fun saveTransaction(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState.selectedCategory == null) {
                _uiState.update { it.copy(error = "Please select a category") }
                return@launch
            }
            if (currentState.amount.toDoubleOrNull() == null || currentState.amount.toDouble() <= 0) {
                _uiState.update { it.copy(error = "Invalid amount") }
                return@launch
            }

            val transaction = Transaction(
                id = 0, // Room will auto-generate the ID
                amount = currentState.amount.toDouble(),
                type = currentState.transactionType,
                categoryId = currentState.selectedCategory.id,
                date = currentState.selectedDate.time, // Store date as Long
                note = currentState.note
            )
            expenseRepository.addTransaction(transaction)
            onSuccess()
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
