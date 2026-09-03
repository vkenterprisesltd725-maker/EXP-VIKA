package com.example.data.repository

import com.example.data.local.dao.AccountDao
import com.example.data.local.dao.BudgetDao
import com.example.data.local.dao.TransactionDao
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.TransactionEntity
import com.example.domain.model.AccountType
import com.example.domain.model.TransactionDirection
import com.example.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ExpVikaRepository(
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao
) {
    val allAccounts: Flow<List<AccountEntity>> = accountDao.getAllAccounts()
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allBudgets: Flow<List<BudgetEntity>> = budgetDao.getAllBudgets()

    suspend fun insertAccount(account: AccountEntity) {
        accountDao.insertAccount(account)
    }

    suspend fun insertTransaction(transaction: TransactionEntity) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun insertTransactions(transactions: List<TransactionEntity>) {
        transactionDao.insertTransactions(transactions)
    }

    suspend fun updateTransactions(transactions: List<TransactionEntity>) {
        transactionDao.updateTransactions(transactions)
    }
    
    suspend fun insertBudget(budget: BudgetEntity) {
        budgetDao.insertBudget(budget)
    }
    
    suspend fun deleteBudget(id: String) {
        budgetDao.deleteBudget(id)
    }

    suspend fun seedSampleDataIfNeeded() {
        val currentAccounts = accountDao.getAllAccounts().first()
        if (currentAccounts.isEmpty()) {
            val hdfcSavings = AccountEntity(id = "acc_hdfc_sav", name = "HDFC Savings", bankName = "HDFC", type = AccountType.SAVINGS.name, openingBalance = 85000.0, currentBalance = 85000.0)
            val sbiSavings = AccountEntity(id = "acc_sbi_sav", name = "SBI Savings", bankName = "SBI", type = AccountType.SAVINGS.name, openingBalance = 42000.0, currentBalance = 42000.0)
            val hdfcCredit = AccountEntity(id = "acc_hdfc_cc", name = "HDFC Credit Card", bankName = "HDFC", type = AccountType.CREDIT_CARD.name, openingBalance = -18500.0, currentBalance = -18500.0, creditLimit = 100000.0)
            
            insertAccount(hdfcSavings)
            insertAccount(sbiSavings)
            insertAccount(hdfcCredit)

            val now = System.currentTimeMillis()
            val day = 86400000L

            insertTransaction(TransactionEntity(accountId = "acc_hdfc_sav", dateMillis = now - day*5, amount = 75000.0, merchant = "Employer", description = "Salary", category = "Salary", type = TransactionType.INCOME.name, direction = TransactionDirection.CREDIT.name))
            insertTransaction(TransactionEntity(accountId = "acc_hdfc_cc", dateMillis = now - day*4, amount = 2499.0, merchant = "Amazon", description = "Shopping", category = "Shopping", type = TransactionType.EXPENSE.name, direction = TransactionDirection.DEBIT.name))
            insertTransaction(TransactionEntity(accountId = "acc_hdfc_sav", dateMillis = now - day*3, amount = 650.0, merchant = "Swiggy", description = "Food Delivery", category = "Food & Dining", type = TransactionType.EXPENSE.name, direction = TransactionDirection.DEBIT.name))
            insertTransaction(TransactionEntity(accountId = "acc_sbi_sav", dateMillis = now - day*2, amount = 380.0, merchant = "Uber", description = "Ride", category = "Transport", type = TransactionType.EXPENSE.name, direction = TransactionDirection.DEBIT.name))
            insertTransaction(TransactionEntity(accountId = "acc_hdfc_sav", dateMillis = now - day*1, amount = 20000.0, merchant = "Self Transfer", description = "HDFC to SBI", category = "Transfer", type = TransactionType.TRANSFER.name, direction = TransactionDirection.DEBIT.name, isSelfTransfer = true, linkedTransferId = "txn_transfer_1"))
            insertTransaction(TransactionEntity(accountId = "acc_sbi_sav", dateMillis = now - day*1, amount = 20000.0, merchant = "Self Transfer", description = "HDFC to SBI", category = "Transfer", type = TransactionType.TRANSFER.name, direction = TransactionDirection.CREDIT.name, isSelfTransfer = true, linkedTransferId = "txn_transfer_1"))
            insertTransaction(TransactionEntity(accountId = "acc_sbi_sav", dateMillis = now, amount = 10000.0, merchant = "Credit Card Payment", description = "SBI to HDFC CC", category = "Transfer", type = TransactionType.CREDIT_CARD_PAYMENT.name, direction = TransactionDirection.DEBIT.name, isSelfTransfer = true, linkedTransferId = "txn_transfer_2"))
            insertTransaction(TransactionEntity(accountId = "acc_hdfc_cc", dateMillis = now, amount = 10000.0, merchant = "Credit Card Payment", description = "SBI to HDFC CC", category = "Transfer", type = TransactionType.CREDIT_CARD_PAYMENT.name, direction = TransactionDirection.CREDIT.name, isSelfTransfer = true, linkedTransferId = "txn_transfer_2"))
        }
    }
}
