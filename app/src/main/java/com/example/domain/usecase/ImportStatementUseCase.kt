package com.example.domain.usecase

import com.example.data.local.entity.TransactionEntity
import com.example.data.repository.ExpVikaRepository
import kotlinx.coroutines.flow.first

class ImportStatementUseCase(private val repository: ExpVikaRepository) {
    
    suspend fun filterDuplicates(newTransactions: List<TransactionEntity>): Pair<Int, List<TransactionEntity>> {
        val existingTransactions = repository.allTransactions.first()
        val nonDuplicates = mutableListOf<TransactionEntity>()
        var duplicateCount = 0
        
        for (newTx in newTransactions) {
            val isDuplicate = existingTransactions.any { existingTx ->
                val sameAccount = existingTx.accountId == newTx.accountId
                val sameAmount = existingTx.amount == newTx.amount
                val sameDirection = existingTx.direction == newTx.direction
                
                if (sameAccount && sameAmount && sameDirection) {
                    val hasRefMatch = existingTx.referenceNumber != null && existingTx.referenceNumber == newTx.referenceNumber
                    if (hasRefMatch) {
                        true
                    } else {
                        val timeDiff = Math.abs(existingTx.dateMillis - newTx.dateMillis)
                        val withinOneDay = timeDiff <= 86400000L
                        
                        if (withinOneDay) {
                            val descMatch = existingTx.merchant.contains(newTx.merchant, ignoreCase = true) ||
                                            newTx.merchant.contains(existingTx.merchant, ignoreCase = true)
                            descMatch
                        } else {
                            false
                        }
                    }
                } else {
                    false
                }
            }
            
            if (isDuplicate) {
                duplicateCount++
            } else {
                nonDuplicates.add(newTx)
            }
        }
        
        return Pair(duplicateCount, nonDuplicates)
    }
}
