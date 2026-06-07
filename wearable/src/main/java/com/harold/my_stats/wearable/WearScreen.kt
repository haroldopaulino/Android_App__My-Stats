package com.harold.my_stats.wearable

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.content.ContextCompat
import com.harold.my_stats.service.CollectorService
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import kotlinx.coroutines.delay

@Composable
internal fun WearValidationScreen(viewModel: WearDashboardViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val state = uiState.state

    val context = LocalContext.current
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                viewModel.refresh()
            }
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(CollectorService.ACTION_COLLECTION_DATA_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        onDispose { context.unregisterReceiver(receiver) }
    }

    LaunchedEffect(uiState.isRunning) {
        while (uiState.isRunning) {
            viewModel.refresh()
            viewModel.uploadPendingIfDue()
            delay(10_000)
        }
    }

    MaterialTheme(colors = MaterialTheme.colors.copy(background = Color.Black, surface = Color(0xFF101820), primary = Color(0xFF4DA3FF))) {
        Scaffold {
            ScalingLazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item { WearHeader(if (uiState.isRunning) state.battery else "--", if (uiState.isRunning) state.subtitle else "Stopped", uiState.isRunning) }
                item {
                    Button(
                        onClick = viewModel::toggleCollector,
                        colors = ButtonDefaults.buttonColors(backgroundColor = if (uiState.isRunning) Color(0xFF3A151B) else Color(0xFF4DA3FF)),
                        modifier = Modifier.fillMaxWidth(0.78f)
                    ) {
                        Text(if (uiState.isRunning) "Stop Service" else "Start Service", color = if (uiState.isRunning) Color(0xFFFF6B75) else Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                if (uiState.isRunning) {
                    item { WearCollectionCounters(state.collectedCount, state.collectedDataSizeBytes, state.sentEndpointCount, state.failedEndpointCount) }
                    items(state.rows) { row -> WearMetricCard(row) }
                } else {
                    item { WearStoppedCard() }
                }
                item { Spacer(Modifier.height(20.dp)) }
            }
        }
    }
}
