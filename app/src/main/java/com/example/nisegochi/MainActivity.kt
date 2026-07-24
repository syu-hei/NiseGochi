package com.example.nisegochi

import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.nisegochi.data.persistence.PetDatabase
import com.example.nisegochi.data.PetRepository
import com.example.nisegochi.data.work.PetWorker
import com.example.nisegochi.ui.audio.AudioManager
import com.example.nisegochi.ui.components.AdaptivePetLayout
import com.example.nisegochi.ui.notification.NotificationHelper
import com.example.nisegochi.ui.theme.NiseGochiTheme
import com.example.nisegochi.ui.viewmodel.PetViewModel
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private val repository by lazy {
        val db = PetDatabase.getDatabase(applicationContext)
        val notificationHelper = NotificationHelper(applicationContext)
        PetRepository(db.petDao(), notificationHelper)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Permission handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        AudioManager.loadSounds(this)
        schedulePetWorker()
        requestNotificationPermission()
        
        setContent {
            val viewModel: PetViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return PetViewModel(repository) as T
                    }
                }
            )
            
            NiseGochiTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AdaptivePetLayout(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AudioManager.release()
    }

    private fun schedulePetWorker() {
        val workRequest = PeriodicWorkRequestBuilder<PetWorker>(15, TimeUnit.MINUTES)
            .build()
        
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "PetWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
