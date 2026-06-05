package com.harold.my_stats.phone

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.content.ContextCompat
import com.harold.my_stats.service.CollectorService
import kotlinx.coroutines.delay

@Composable
internal fun PhoneValidationScreen(viewModel: PhoneDashboardViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val state = uiState.dashboard

    BackHandler(enabled = uiState.screen != "home") { viewModel.onBack() }

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
            delay(10_000)
        }
    }

    PhoneTheme(uiState.isDarkTheme) {
        Surface(modifier = Modifier.fillMaxSize(), color = androidx.compose.material3.MaterialTheme.colorScheme.background) {
            when (uiState.screen) {
                "settings" -> SettingsScreen(
                    isDarkTheme = uiState.isDarkTheme,
                    endpointUrl = uiState.endpointUrl,
                    uploadIntervalMs = uiState.uploadIntervalMs,
                    onToggleTheme = viewModel::toggleTheme,
                    onEndpointChange = viewModel::updateEndpoint,
                    onUploadIntervalChange = viewModel::updateUploadInterval,
                    onPrivacy = { viewModel.openScreen("privacy") },
                    onAbout = { viewModel.openScreen("about") },
                    onBack = { viewModel.openScreen("home") }
                )
                "privacy" -> TextScreen(
                    title = "Privacy Policy",
                    text = privacyPolicyText(),
                    onBack = { viewModel.openScreen("settings") }
                )
                "about" -> TextScreen(
                    title = "About",
                    text = aboutText(),
                    onBack = { viewModel.openScreen("settings") }
                )
                else -> PhoneHomeScreen(
                    uiState = uiState,
                    state = state,
                    onToggleService = viewModel::toggleCollector,
                    onOpenSettings = { viewModel.openScreen("settings") },
                    onMainTabSelected = viewModel::selectMainTab,
                    onDetailTabSelected = viewModel::selectDetailTab
                )
            }
        }
    }
}


@Composable
internal fun PhoneHomeScreen(
    uiState: PhoneUiState,
    state: DashboardState,
    onToggleService: () -> Unit,
    onOpenSettings: () -> Unit,
    onMainTabSelected: (Int) -> Unit,
    onDetailTabSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        AppHeader(
            model = uiState.phoneName,
            onOpenSettings = onOpenSettings,
            modifier = Modifier.padding(start = 18.dp, top = 18.dp, end = 18.dp, bottom = 8.dp)
        )

        ServiceHeroCard(
            isRunning = uiState.isRunning,
            collectedCount = state.collectedCount,
            sentEndpointCount = state.sentEndpointCount,
            failedEndpointCount = state.failedEndpointCount,
            collectedDataSizeBytes = state.collectedDataSizeBytes,
            uploadIntervalMs = uiState.uploadIntervalMs,
            refreshedAt = if (uiState.isRunning) state.refreshedAt else "Not running",
            onToggleService = onToggleService,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
        )

        if (!uiState.isRunning) {
            StartPromptCard(modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp))
        } else {
            DarkTabs(
                titles = listOf("System", "Top Details", "Raw Data"),
                selectedIndex = uiState.mainTab,
                onSelected = onMainTabSelected
            )
            when (uiState.mainTab) {
                0 -> SystemTab(state)
                1 -> TopDetailsTab(state.snapshot, uiState.detailTab, onDetailTabSelected)
                else -> RawDataTab(state)
            }
        }
    }
}


@Preview(name = "Phone Home Screen Runtime Path", showBackground = true, widthDp = 390, heightDp = 900)
@Composable
fun PhoneHomeScreenRuntimePathPreview() {
    val state = previewDashboardState()
    PhonePreviewSurface {
        PhoneHomeScreen(
            uiState = PhoneUiState(
                dashboard = state,
                isRunning = true,
                phoneName = "Pixel 9 Pro"
            ),
            state = state,
            onToggleService = {},
            onOpenSettings = {},
            onMainTabSelected = {},
            onDetailTabSelected = {}
        )
    }
}
