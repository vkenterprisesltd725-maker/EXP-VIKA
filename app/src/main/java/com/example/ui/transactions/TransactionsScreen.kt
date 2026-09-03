package com.example.ui.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.TransactionEntity
import com.example.domain.model.TransactionDirection
import com.example.ui.theme.ExpenseColor
import com.example.ui.theme.IncomeColor
import com.example.ui.theme.TransferColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(viewModel: TransactionsViewModel, onNavigateToImport: () -> Unit) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) {
            try {
                val csvData = viewModel.generateCsvContent()
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(csvData.toByteArray())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transactions") },
                actions = {
                    IconButton(onClick = { exportLauncher.launch("expvika_export.csv") }) {
                        Icon(Icons.Filled.Download, contentDescription = "Export Transactions")
                    }
                    IconButton(onClick = onNavigateToImport) {
                        Icon(Icons.Filled.UploadFile, contentDescription = "Import Statement")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(transactions) { transaction ->
                TransactionListItem(transaction = transaction)
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun TransactionListItem(transaction: TransactionEntity) {
    val isExpense = transaction.direction == TransactionDirection.DEBIT.name && !transaction.isSelfTransfer
    val isIncome = transaction.direction == TransactionDirection.CREDIT.name && !transaction.isSelfTransfer
    val isTransfer = transaction.isSelfTransfer
    
    val icon = when {
        isTransfer -> Icons.Default.SyncAlt
        isIncome -> Icons.Default.ArrowDownward
        else -> Icons.Default.ArrowUpward
    }
    
    val iconColor = when {
        isTransfer -> TransferColor
        isIncome -> IncomeColor
        else -> ExpenseColor
    }
    
    val amountPrefix = when {
        isTransfer -> ""
        isIncome -> "+"
        else -> "-"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.merchant, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val dateStr = dateFormat.format(Date(transaction.dateMillis))
                Text("$dateStr • ${transaction.category}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "$amountPrefix₹${"%,.2f".format(transaction.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isTransfer) MaterialTheme.colorScheme.onSurface else iconColor
                )
                if (isTransfer) {
                    Text("Transfer", style = MaterialTheme.typography.labelSmall, color = TransferColor)
                }
            }
        }
    }
}
