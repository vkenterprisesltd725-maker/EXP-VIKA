package com.example.domain.model

enum class TransactionType {
    EXPENSE,
    INCOME,
    TRANSFER,
    REFUND,
    CREDIT_CARD_PAYMENT,
    CASH_WITHDRAWAL,
    FEE,
    INTEREST,
    OTHER
}

enum class TransactionDirection {
    DEBIT,
    CREDIT
}

enum class AccountType {
    SAVINGS,
    CURRENT,
    SALARY,
    CREDIT_CARD,
    OTHER
}
