package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val id: String,
    val name: String,
    val bankName: String,
    val type: String, // from AccountType
    val openingBalance: Double,
    val currentBalance: Double,
    val creditLimit: Double? = null,
    val statementDate: Int? = null,
    val dueDate: Int? = null
)
