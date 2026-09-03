package com.example.ui.importstatement

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(viewModel: ImportViewModel, onNavigateBack: () -> Unit) {
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    var selectedAccountId by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    
    LaunchedEffect(accounts) {
        if (selectedAccountId == null && accounts.isNotEmpty()) {
            selectedAccountId = accounts.first().id
        }
    }
    
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null && selectedAccountId != null) {
            viewModel.parseStatement(context, uri, selectedAccountId!!)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import Statement") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (val currentState = state) {
                is ImportState.Idle, is ImportState.Error -> {
                    if (currentState is ImportState.Error) {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                            Text(currentState.message, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(16.dp))
                        }
                    }
                    
                    Text("Select Account", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    
                    accounts.forEach { account ->
                        Card(
                            onClick = { selectedAccountId = account.id },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedAccountId == account.id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(account.name, modifier = Modifier.padding(16.dp), fontWeight = if (selectedAccountId == account.id) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { launcher.launch("*/*") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedAccountId != null
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select CSV Statement")
                    }
                }
                
                ImportState.Parsing -> {
                    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                
                is ImportState.Preview -> {
                    Text("Preview", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Total parsed: ${currentState.parsedCount}")
                            Text("Duplicates skipped: ${currentState.duplicatesFound}", color = MaterialTheme.colorScheme.error)
                            Text("New to import: ${currentState.newTransactions.size}", fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { viewModel.confirmImport(currentState.newTransactions) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = currentState.newTransactions.isNotEmpty()
                    ) {
                        Text("Confirm Import")
                    }
                    
                    OutlinedButton(
                        onClick = { viewModel.reset() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel")
                    }
                }
                
                ImportState.Importing -> {
                    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                
                ImportState.Success -> {
                    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Import Successful!", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onNavigateBack) {
                                Text("Done")
                            }
                        }
                    }
                }
            }
        }
    }
}
