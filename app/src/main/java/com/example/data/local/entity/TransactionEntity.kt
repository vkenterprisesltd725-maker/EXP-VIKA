package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val accountId: String,
    val dateMillis: Long,
    val amount: Double,
    val merchant: String,
    val description: String,
    val category: String,
    val type: String, // TransactionType
    val direction: String, // TransactionDirection
    val isSelfTransfer: Boolean = false,
    val linkedTransferId: String? = null,
    val referenceNumber: String? = null,
    val importBatchId: String? = null
)
