package com.example.ui.importstatement

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.repository.ExpVikaRepository
import com.example.domain.parser.CSVStatementParser
import com.example.domain.usecase.ImportStatementUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class ImportState {
    object Idle : ImportState()
    object Parsing : ImportState()
    data class Preview(
        val parsedCount: Int,
        val duplicatesFound: Int,
        val newTransactions: List<TransactionEntity>
    ) : ImportState()
    object Importing : ImportState()
    object Success : ImportState()
    data class Error(val message: String) : ImportState()
}

class ImportViewModel(
    private val repository: ExpVikaRepository,
    private val useCase: ImportStatementUseCase = ImportStatementUseCase(repository)
) : ViewModel() {

    val accounts: StateFlow<List<AccountEntity>> = repository.allAccounts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val state = MutableStateFlow<ImportState>(ImportState.Idle)
    
    private val parser = CSVStatementParser()
    private val intelligence = com.example.domain.usecase.TransactionIntelligenceUseCase(repository)

    fun parseStatement(context: Context, uri: Uri, accountId: String) {
        viewModelScope.launch {
            state.value = ImportState.Parsing
            try {
                val parsed = parser.parse(context, uri, accountId)
                if (parsed.isEmpty()) {
                    state.value = ImportState.Error("We couldn't confidently parse some transactions. Please ensure the file is a valid CSV statement.")
                    return@launch
                }
                
                val (duplicates, unique) = useCase.filterDuplicates(parsed)
                state.value = ImportState.Preview(parsed.size, duplicates, unique)
            } catch (e: Exception) {
                state.value = ImportState.Error(e.message ?: "Unknown error during parsing")
            }
        }
    }

    fun confirmImport(transactions: List<TransactionEntity>) {
        viewModelScope.launch {
            state.value = ImportState.Importing
            try {
                repository.insertTransactions(transactions)
                // Run intelligence sweep to link self-transfers
                intelligence.detectSelfTransfers()
                state.value = ImportState.Success
            } catch (e: Exception) {
                state.value = ImportState.Error(e.message ?: "Failed to save transactions")
            }
        }
    }
    
    fun reset() {
        state.value = ImportState.Idle
    }

    companion object {
        fun provideFactory(repository: ExpVikaRepository): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ImportViewModel(repository) as T
            }
        }
    }
}
