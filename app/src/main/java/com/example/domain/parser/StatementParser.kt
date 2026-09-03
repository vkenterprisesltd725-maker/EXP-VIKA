package com.example.domain.parser

import android.content.Context
import android.net.Uri
import com.example.data.local.entity.TransactionEntity
import com.example.domain.model.TransactionDirection
import com.example.domain.model.TransactionType
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

interface StatementParser {
    suspend fun parse(context: Context, uri: Uri, accountId: String): List<TransactionEntity>
}

class CSVStatementParser : StatementParser {
    override suspend fun parse(context: Context, uri: Uri, accountId: String): List<TransactionEntity> {
        val transactions = mutableListOf<TransactionEntity>()
        val importBatchId = UUID.randomUUID().toString()
        
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val reader = BufferedReader(InputStreamReader(inputStream))
            var headerFound = false
            var dateIdx = -1
            var descIdx = -1
            var drIdx = -1
            var crIdx = -1
            var amountIdx = -1
            var refIdx = -1
            
            // supported date formats
            val dateFormats = listOf(
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
                SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()),
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
                SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()),
                SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            )

            reader.forEachLine { line ->
                if (line.isBlank()) return@forEachLine
                
                // Extremely simple CSV split for demonstration
                val columns = line.split(",").map { it.trim(' ', '"') }
                
                if (!headerFound) {
                    // Try to find header
                    columns.forEachIndexed { index, col ->
                        val lowerCol = col.lowercase(Locale.getDefault())
                        if (lowerCol.contains("date") && !lowerCol.contains("value")) dateIdx = index
                        if (lowerCol.contains("description") || lowerCol.contains("narration") || lowerCol.contains("particulars") || lowerCol.contains("details") || lowerCol.contains("transaction")) descIdx = index
                        if (lowerCol.contains("debit") || lowerCol.contains("withdrawal") || lowerCol.equals("dr")) drIdx = index
                        if (lowerCol.contains("credit") || lowerCol.contains("deposit") || lowerCol.equals("cr")) crIdx = index
                        if (lowerCol.contains("amount")) amountIdx = index
                        if (lowerCol.contains("ref") || lowerCol.contains("chq") || lowerCol.contains("cheque")) refIdx = index
                    }
                    
                    if (dateIdx != -1 && descIdx != -1 && (amountIdx != -1 || drIdx != -1 || crIdx != -1)) {
                        headerFound = true
                    }
                } else {
                    // Parse row
                    try {
                        val dateStr = if (dateIdx in columns.indices) columns[dateIdx] else ""
                        var dateMillis = System.currentTimeMillis()
                        
                        if (dateStr.isNotBlank()) {
                            for (format in dateFormats) {
                                try {
                                    val parsed = format.parse(dateStr)
                                    if (parsed != null) {
                                        dateMillis = parsed.time
                                        break
                                    }
                                } catch (e: Exception) {
                                    // ignore
                                }
                            }
                        }
                        
                        val description = if (descIdx in columns.indices) columns[descIdx] else ""
                        val ref = if (refIdx != -1 && refIdx in columns.indices) columns[refIdx] else null
                        
                        var amount = 0.0
                        var direction = TransactionDirection.DEBIT
                        
                        if (drIdx != -1 && drIdx in columns.indices && columns[drIdx].isNotBlank()) {
                            amount = columns[drIdx].replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.0
                            direction = TransactionDirection.DEBIT
                        } else if (crIdx != -1 && crIdx in columns.indices && columns[crIdx].isNotBlank()) {
                            amount = columns[crIdx].replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.0
                            direction = TransactionDirection.CREDIT
                        } else if (amountIdx != -1 && amountIdx in columns.indices) {
                            val amtStr = columns[amountIdx].replace(Regex("[^0-9.-]"), "")
                            val parsedAmt = amtStr.toDoubleOrNull() ?: 0.0
                            if (parsedAmt < 0) {
                                amount = -parsedAmt
                                direction = TransactionDirection.DEBIT
                            } else {
                                amount = parsedAmt
                                direction = TransactionDirection.CREDIT
                            }
                        }
                        
                        if (amount > 0) {
                            val merchant = extractMerchant(description)
                            val (category, type) = com.example.domain.usecase.TransactionCategorizer.categorize(description, direction.name)
                            
                            transactions.add(
                                TransactionEntity(
                                    id = UUID.randomUUID().toString(),
                                    accountId = accountId,
                                    dateMillis = dateMillis,
                                    amount = amount,
                                    merchant = merchant,
                                    description = description,
                                    category = category,
                                    type = type,
                                    direction = direction.name,
                                    referenceNumber = ref?.takeIf { it.isNotBlank() },
                                    importBatchId = importBatchId
                                )
                            )
                        }
                    } catch (e: Exception) {
                        // skip row on error
                    }
                }
            }
        }
        return transactions
    }
    
    private fun extractMerchant(description: String): String {
        val cleaned = description.replace(Regex("[^a-zA-Z0-9 ]"), " ").trim().replace(Regex(" +"), " ")
        if (cleaned.isBlank()) return "Unknown"
        val parts = cleaned.split(" ")
        if (parts.size <= 2) return cleaned
        return parts.take(2).joinToString(" ")
    }
}
