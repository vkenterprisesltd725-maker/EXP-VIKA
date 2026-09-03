package com.example.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.TransactionEntity
import com.example.data.repository.ExpVikaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class TransactionsViewModel(repository: ExpVikaRepository) : ViewModel() {
    val transactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun generateCsvContent(): String {
        val txs = transactions.value
        val sb = StringBuilder()
        sb.append("Date,Merchant,Description,Category,Type,Direction,Amount\n")
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        txs.forEach { tx ->
            val date = dateFormat.format(java.util.Date(tx.dateMillis))
            val merchant = tx.merchant.replace(",", " ")
            val desc = tx.description.replace(",", " ")
            val cat = tx.category.replace(",", " ")
            sb.append("$date,$merchant,$desc,$cat,${tx.type},${tx.direction},${tx.amount}\n")
        }
        return sb.toString()
    }

    companion object {
        fun provideFactory(repository: ExpVikaRepository): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TransactionsViewModel(repository) as T
            }
        }
    }
}
