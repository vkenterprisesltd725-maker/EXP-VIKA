package com.example.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.repository.ExpVikaRepository
import com.example.ui.accounts.AccountsScreen
import com.example.ui.accounts.AccountsViewModel
import com.example.ui.addtransaction.AddTransactionScreen
import com.example.ui.addtransaction.AddTransactionViewModel
import com.example.ui.home.HomeScreen
import com.example.ui.home.HomeViewModel
import com.example.ui.importstatement.ImportScreen
import com.example.ui.importstatement.ImportViewModel
import com.example.ui.transactions.TransactionsScreen
import com.example.ui.transactions.TransactionsViewModel

import com.example.ui.analytics.AnalyticsScreen
import com.example.ui.analytics.AnalyticsViewModel

import androidx.compose.material.icons.filled.PieChart
import com.example.ui.budgets.BudgetsScreen
import com.example.ui.budgets.BudgetsViewModel

@Composable
fun AppNavigation(repository: ExpVikaRepository) {
    val navController = rememberNavController()
    
    val items = listOf(
        Triple(Home, Icons.Filled.Home, "Home"),
        Triple(Transactions, Icons.Filled.List, "Transactions"),
        Triple(Analytics, Icons.Filled.Analytics, "Analytics"),
        Triple(Budgets, Icons.Filled.PieChart, "Budgets"),
        Triple(Accounts, Icons.Filled.AccountBalance, "Accounts")
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { (route, icon, label) ->
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) },
                        selected = currentDestination?.hierarchy?.any { it.route?.contains(route.javaClass.simpleName) == true } == true,
                        onClick = {
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            // Hide FAB on AddTransaction screen
            if (currentDestination?.route?.contains(AddTransaction.javaClass.simpleName) != true) {
                FloatingActionButton(onClick = { navController.navigate(AddTransaction) }) {
                    Icon(Icons.Filled.Add, "Add Transaction")
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Home,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<Home> {
                val viewModel = androidx.lifecycle.viewmodel.compose.viewModel<HomeViewModel>(
                    factory = HomeViewModel.provideFactory(repository)
                )
                HomeScreen(viewModel = viewModel)
            }
            composable<Transactions> {
                val viewModel = androidx.lifecycle.viewmodel.compose.viewModel<TransactionsViewModel>(
                    factory = TransactionsViewModel.provideFactory(repository)
                )
                TransactionsScreen(
                    viewModel = viewModel,
                    onNavigateToImport = { navController.navigate(ImportStatement) }
                )
            }
            composable<Analytics> {
                val viewModel = androidx.lifecycle.viewmodel.compose.viewModel<AnalyticsViewModel>(
                    factory = AnalyticsViewModel.provideFactory(repository)
                )
                AnalyticsScreen(viewModel = viewModel)
            }
            composable<Budgets> {
                val viewModel = androidx.lifecycle.viewmodel.compose.viewModel<BudgetsViewModel>(
                    factory = BudgetsViewModel.provideFactory(repository)
                )
                BudgetsScreen(viewModel = viewModel)
            }
            composable<Accounts> {
                val viewModel = androidx.lifecycle.viewmodel.compose.viewModel<AccountsViewModel>(
                    factory = AccountsViewModel.provideFactory(repository)
                )
                AccountsScreen(viewModel = viewModel)
            }
            composable<AddTransaction> {
                val viewModel = androidx.lifecycle.viewmodel.compose.viewModel<AddTransactionViewModel>(
                    factory = AddTransactionViewModel.provideFactory(repository)
                )
                AddTransactionScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<ImportStatement> {
                val viewModel = androidx.lifecycle.viewmodel.compose.viewModel<ImportViewModel>(
                    factory = ImportViewModel.provideFactory(repository)
                )
                ImportScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
