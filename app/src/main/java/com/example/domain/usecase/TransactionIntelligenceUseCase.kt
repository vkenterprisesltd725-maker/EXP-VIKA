package com.example.domain.usecase

import com.example.data.repository.ExpVikaRepository
import com.example.domain.model.TransactionDirection
import com.example.domain.model.TransactionType
import kotlinx.coroutines.flow.first
import java.util.UUID

class TransactionIntelligenceUseCase(private val repository: ExpVikaRepository) {
    
    suspend fun detectSelfTransfers(): Int {
        val allTransactions = repository.allTransactions.first()
        val unlinked = allTransactions.filter { !it.isSelfTransfer && it.linkedTransferId == null }
        
        val debits = unlinked.filter { it.direction == TransactionDirection.DEBIT.name }.toMutableList()
        val credits = unlinked.filter { it.direction == TransactionDirection.CREDIT.name }.toMutableList()
        
        val updates = mutableListOf<com.example.data.local.entity.TransactionEntity>()
        var linkedCount = 0
        
        val iterator = debits.iterator()
        while (iterator.hasNext()) {
            val debit = iterator.next()
            
            // Find a matching credit:
            // 1. Same amount exactly
            // 2. Different account
            // 3. Within +/- 2 days (172800000 millis)
            val match = credits.firstOrNull { credit ->
                credit.amount == debit.amount &&
                credit.accountId != debit.accountId &&
                Math.abs(credit.dateMillis - debit.dateMillis) <= 172800000L
            }
            
            if (match != null) {
                credits.remove(match)
                iterator.remove()
                
                val linkId = UUID.randomUUID().toString()
                updates.add(debit.copy(isSelfTransfer = true, linkedTransferId = linkId, type = TransactionType.TRANSFER.name))
                updates.add(match.copy(isSelfTransfer = true, linkedTransferId = linkId, type = TransactionType.TRANSFER.name))
                linkedCount++
            }
        }
        
        if (updates.isNotEmpty()) {
            repository.updateTransactions(updates)
        }
        
        return linkedCount
    }
}
