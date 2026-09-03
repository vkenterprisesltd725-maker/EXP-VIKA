package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    val appContainer = (application as ExpVikaApplication).container
    
    lifecycleScope.launch {
      appContainer.expVikaRepository.seedSampleDataIfNeeded()
    }

    setContent {
      MyApplicationTheme {
        AppNavigation(repository = appContainer.expVikaRepository)
      }
    }
  }
}
