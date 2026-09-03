package com.example.domain.usecase

import com.example.domain.model.TransactionDirection
import com.example.domain.model.TransactionType
import java.util.Locale

object TransactionCategorizer {
    
    fun categorize(description: String, direction: String): Pair<String, String> {
        val lowerDesc = description.lowercase(Locale.getDefault())
        
        // Income heuristics
        if (direction == TransactionDirection.CREDIT.name) {
            if (lowerDesc.contains("salary") || lowerDesc.contains("sal ") || lowerDesc.contains("payroll")) {
                return Pair("Salary", TransactionType.INCOME.name)
            }
            if (lowerDesc.contains("interest") || lowerDesc.contains("int pd")) {
                return Pair("Interest", TransactionType.INCOME.name)
            }
            if (lowerDesc.contains("refund") || lowerDesc.contains("reversal")) {
                return Pair("Refund", TransactionType.INCOME.name)
            }
            return Pair("Other Income", TransactionType.INCOME.name)
        }
        
        // Transfer heuristics (Explicit mentions of own accounts/transfers)
        if (lowerDesc.contains("to ac") || lowerDesc.contains("to a/c") || lowerDesc.contains("tfr") || lowerDesc.contains("transfer")) {
            return Pair("Transfer", TransactionType.TRANSFER.name)
        }

        // Expense heuristics
        if (lowerDesc.contains("swiggy") || lowerDesc.contains("zomato") || lowerDesc.contains("mcdonald") || lowerDesc.contains("starbucks") || lowerDesc.contains("kfc")) {
            return Pair("Food & Dining", TransactionType.EXPENSE.name)
        }
        if (lowerDesc.contains("uber") || lowerDesc.contains("ola") || lowerDesc.contains("irctc") || lowerDesc.contains("makemytrip") || lowerDesc.contains("indigo")) {
            return Pair("Transport & Travel", TransactionType.EXPENSE.name)
        }
        if (lowerDesc.contains("amazon") || lowerDesc.contains("flipkart") || lowerDesc.contains("myntra") || lowerDesc.contains("ajio")) {
            return Pair("Shopping", TransactionType.EXPENSE.name)
        }
        if (lowerDesc.contains("netflix") || lowerDesc.contains("prime") || lowerDesc.contains("spotify") || lowerDesc.contains("hotstar")) {
            return Pair("Entertainment", TransactionType.EXPENSE.name)
        }
        if (lowerDesc.contains("jio") || lowerDesc.contains("airtel") || lowerDesc.contains("vi ") || lowerDesc.contains("bescom") || lowerDesc.contains("bill")) {
            return Pair("Bills & Utilities", TransactionType.EXPENSE.name)
        }
        if (lowerDesc.contains("apollo") || lowerDesc.contains("pharmacy") || lowerDesc.contains("hospital") || lowerDesc.contains("clinic")) {
            return Pair("Health", TransactionType.EXPENSE.name)
        }
        if (lowerDesc.contains("atm") || lowerDesc.contains("cash wdl") || lowerDesc.contains("withdrawal")) {
            return Pair("Cash Withdrawal", TransactionType.EXPENSE.name)
        }
        
        return Pair("Uncategorized", TransactionType.EXPENSE.name)
    }
}
