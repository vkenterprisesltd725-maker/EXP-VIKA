package com.example.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ExpVikaRepository
import com.example.domain.model.TransactionDirection
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class CategorySummary(
    val category: String,
    val amount: Double,
    val percentage: Float
)

class AnalyticsViewModel(repository: ExpVikaRepository) : ViewModel() {

    val expensesByCategory: StateFlow<List<CategorySummary>> = repository.allTransactions
        .map { transactions ->
            // Filter only real expenses (not transfers, not income)
            val expenses = transactions.filter {
                it.direction == TransactionDirection.DEBIT.name && !it.isSelfTransfer
            }
            
            val totalExpense = expenses.sumOf { it.amount }
            if (totalExpense <= 0) return@map emptyList()
            
            val grouped = expenses.groupBy { it.category }
            grouped.map { (category, txs) ->
                val sum = txs.sumOf { it.amount }
                CategorySummary(
                    category = category,
                    amount = sum,
                    percentage = (sum / totalExpense).toFloat()
                )
            }.sortedByDescending { it.amount }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
    val totalExpense: StateFlow<Double> = repository.allTransactions
        .map { transactions ->
            transactions.filter {
                it.direction == TransactionDirection.DEBIT.name && !it.isSelfTransfer
            }.sumOf { it.amount }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    companion object {
        fun provideFactory(repository: ExpVikaRepository): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AnalyticsViewModel(repository) as T
            }
        }
    }
}
