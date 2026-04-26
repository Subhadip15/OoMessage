package com.subhadip.oomessage.ui.discovery

import android.net.wifi.p2p.WifiP2pDevice
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DiscoveryViewModel : ViewModel() {
    private val _peers = MutableStateFlow<List<WifiP2pDevice>>(emptyList())
    val peers: StateFlow<List<WifiP2pDevice>> = _peers.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    fun updatePeers(newPeers: List<WifiP2pDevice>) {
        _peers.value = newPeers
    }

    fun setScanning(scanning: Boolean) {
        _isScanning.value = scanning
    }
}