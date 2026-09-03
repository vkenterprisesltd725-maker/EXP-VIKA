package com.example.ui.addtransaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.repository.ExpVikaRepository
import com.example.domain.model.TransactionDirection
import com.example.domain.model.TransactionType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class AddTransactionViewModel(private val repository: ExpVikaRepository) : ViewModel() {
    val accounts: StateFlow<List<AccountEntity>> = repository.allAccounts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addTransaction(
        accountId: String,
        amount: Double,
        merchant: String,
        description: String,
        category: String,
        type: TransactionType,
        direction: TransactionDirection
    ) {
        viewModelScope.launch {
            repository.insertTransaction(
                TransactionEntity(
                    id = UUID.randomUUID().toString(),
                    accountId = accountId,
                    dateMillis = System.currentTimeMillis(),
                    amount = amount,
                    merchant = merchant,
                    description = description,
                    category = category,
                    type = type.name,
                    direction = direction.name
                )
            )
        }
    }

    companion object {
        fun provideFactory(repository: ExpVikaRepository): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AddTransactionViewModel(repository) as T
            }
        }
    }
}
