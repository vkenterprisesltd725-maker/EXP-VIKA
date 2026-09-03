package com.example.ui.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.BudgetEntity
import com.example.data.repository.ExpVikaRepository
import com.example.domain.model.TransactionDirection
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class BudgetProgress(
    val budget: BudgetEntity,
    val spentAmount: Double,
    val percentage: Float
)

class BudgetsViewModel(private val repository: ExpVikaRepository) : ViewModel() {

    val budgetProgressList: StateFlow<List<BudgetProgress>> = combine(
        repository.allBudgets,
        repository.allTransactions
    ) { budgets, transactions ->
        budgets.map { budget ->
            val spent = transactions.filter {
                it.category.equals(budget.category, ignoreCase = true) &&
                it.direction == TransactionDirection.DEBIT.name &&
                !it.isSelfTransfer
            }.sumOf { it.amount }
            
            BudgetProgress(
                budget = budget,
                spentAmount = spent,
                percentage = if (budget.monthlyLimit > 0) (spent / budget.monthlyLimit).toFloat().coerceAtMost(1f) else 0f
            )
        }.sortedByDescending { it.percentage }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addBudget(category: String, limit: Double) {
        viewModelScope.launch {
            repository.insertBudget(BudgetEntity(id = UUID.randomUUID().toString(), category = category, monthlyLimit = limit))
        }
    }
    
    fun deleteBudget(id: String) {
        viewModelScope.launch {
            repository.deleteBudget(id)
        }
    }

    companion object {
        fun provideFactory(repository: ExpVikaRepository): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return BudgetsViewModel(repository) as T
            }
        }
    }
}
