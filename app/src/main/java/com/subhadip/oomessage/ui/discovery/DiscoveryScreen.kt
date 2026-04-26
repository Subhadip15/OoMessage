package com.subhadip.oomessage.ui.discovery

import android.net.wifi.p2p.WifiP2pDevice
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.subhadip.oomessage.ui.components.DeviceCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryScreen(
    viewModel: DiscoveryViewModel,
    onStartScan: () -> Unit,
    onDeviceSelected: (WifiP2pDevice) -> Unit
) {
    val peers by viewModel.peers.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()

    // 1. Initialize PullToRefresh state
    val pullRefreshState = rememberPullToRefreshState()

    // 2. Trigger reset/scan when user pulls down
    if (pullRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            onStartScan()
        }
    }

    // 3. Sync the visual spinner with the scanning state to hide/show it
    LaunchedEffect(isScanning) {
        if (isScanning) {
            pullRefreshState.startRefresh()
        } else {
            pullRefreshState.endRefresh()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("OoMessage") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        // 4. Wrap the ENTIRE screen content in the Box for the swipe gesture
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Ensures it sits exactly under the TopBar
                .nestedScroll(pullRefreshState.nestedScrollConnection)
        ) {
            // 5. ONE LazyColumn for everything so the whole page scrolls and refreshes together
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // --- Top Info Banner ---
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Info",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "To discover and connect with others, please ensure both Wi-Fi and Location are turned on.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                }

                // --- Scan Button ---
                item {
                    Button(
                        onClick = onStartScan,
                        enabled = !isScanning,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        Text(if (isScanning) "Searching for Devices..." else "Scan for Devices")
                    }
                }

                // --- Devices List or Empty State ---
                if (peers.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp), // Height to center the text nicely
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No devices found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    items(peers) { device ->
                        DeviceCard(device = device, onClick = { onDeviceSelected(device) })
                        Spacer(modifier = Modifier.height(8.dp)) // Spacing between cards
                    }
                }
            }

            // 6. The Refresh Indicator (Aligned TopCenter of the screen, just under TopBar)
            PullToRefreshContainer(
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}