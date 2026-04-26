package com.subhadip.oomessage

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.subhadip.oomessage.network.p2p.WiFiDirectManager
import com.subhadip.oomessage.network.p2p.WiFiDirectReceiver
import com.subhadip.oomessage.ui.chat.ChatScreen
import com.subhadip.oomessage.ui.chat.ChatViewModel
import com.subhadip.oomessage.ui.discovery.DiscoveryScreen
import com.subhadip.oomessage.ui.discovery.DiscoveryViewModel
import com.subhadip.oomessage.ui.theme.OoMessageTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val intentFilter = IntentFilter().apply {
        addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
    }

    private lateinit var wiFiDirectManager: WiFiDirectManager
    private lateinit var receiver: WiFiDirectReceiver
    private var wifiP2pManager: WifiP2pManager? = null
    private var wifiP2pChannel: WifiP2pManager.Channel? = null
    private var isReceiverRegistered = false

    private val discoveryViewModel: DiscoveryViewModel by viewModels()
    private val chatViewModel: ChatViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                val repository = (application as OoMessageApp).chatRepository
                @Suppress("UNCHECKED_CAST")
                return ChatViewModel(repository) as T
            }
        }
    }

    private val showSetupPopup = mutableStateOf(false)
    private var navigateToChat: ((String) -> Unit)? = null

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.entries.all { it.value }) {
                if (!checkAllHardwareOn()) showSetupPopup.value = true
                else startAppLogic()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkInitialRequirements()

        val serviceIntent = Intent(this, com.subhadip.oomessage.network.p2p.WifiDirectService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(serviceIntent)
        else startService(serviceIntent)

        setContent {
            OoMessageTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {

                    val showPopup by showSetupPopup
                    if (showPopup) SetupRequirementsDialog()

                    val navController = rememberNavController()

                    navigateToChat = { peerName ->
                        runOnUiThread {
                            if (navController.currentDestination?.route != "chat/{peerName}") {
                                navController.navigate("chat/$peerName")
                            }
                        }
                    }

                    NavHost(navController = navController, startDestination = "discovery") {
                        composable("discovery") {
                            DiscoveryScreen(
                                viewModel = discoveryViewModel,
                                onStartScan = {
                                    if (checkAllHardwareOn()) {
                                        discoveryViewModel.setScanning(true)
                                        wiFiDirectManager.startDiscovery({}, {})
                                    } else {
                                        showSetupPopup.value = true
                                    }
                                },
                                onDeviceSelected = { device ->
                                    val safeRouteName = device.deviceName.replace(" ", "_")

                                    // 1. FIRST CHECK: Does our ViewModel know we are already connected?
                                    // This bypasses Android's delayed hardware status completely!
                                    if (chatViewModel.isConnected.value) {
                                        navigateToChat?.invoke(safeRouteName)
                                    }
                                    // 2. SECOND CHECK: If ViewModel reset, but hardware is connected
                                    else if (device.status == WifiP2pDevice.CONNECTED) {
                                        wifiP2pManager?.requestConnectionInfo(wifiP2pChannel) { info ->
                                            if (info != null) {
                                                handleConnection(info, device.deviceName)
                                            }
                                        }
                                    }
                                    // 3. FALLBACK: We are entirely disconnected, start fresh handshake
                                    else {
                                        if (::wiFiDirectManager.isInitialized) {
                                            wiFiDirectManager.connectToDevice(
                                                deviceAddress = device.deviceAddress,
                                                onSuccess = { Toast.makeText(this@MainActivity, "Connecting...", Toast.LENGTH_SHORT).show() },
                                                onFailure = { Toast.makeText(this@MainActivity, "Connection failed", Toast.LENGTH_SHORT).show() }
                                            )
                                        }
                                    }
                                }
                            )
                        }

                        composable("chat/{peerName}") { backStackEntry ->
                            val peerName = backStackEntry.arguments?.getString("peerName") ?: "Unknown"
                            ChatScreen(
                                peerName = peerName,
                                viewModel = chatViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }

    // --- DIALOG UI ---
    @Composable
    fun SetupRequirementsDialog() {
        val wm = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isWifiOn = wm.isWifiEnabled
        val isGpsOn = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)

        AlertDialog(
            onDismissRequest = { },
            title = { Text("Setup Required", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    RequirementRow(Icons.Default.Wifi, "Wi-Fi", isWifiOn) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) startActivity(Intent(Settings.Panel.ACTION_WIFI))
                        else startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                    }
                    RequirementRow(Icons.Default.LocationOn, "Location", isGpsOn) {
                        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { if (checkAllHardwareOn()) showSetupPopup.value = false }) {
                    Text("Finish")
                }
            }
        )
    }

    @Composable
    fun RequirementRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, isActive: Boolean, onAction: () -> Unit) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = if (isActive) Color(0xFF4CAF50) else Color.Gray)
                Spacer(Modifier.width(10.dp))
                Text(title)
            }
            Button(onClick = onAction) { Text(if (isActive) "ON" else "Turn ON") }
        }
    }

    // --- CONNECTION HANDLER ---
    private fun handleConnection(info: WifiP2pInfo, peerDeviceName: String) {
        val ownerAddress = info.groupOwnerAddress?.hostAddress
        if (ownerAddress != null && info.groupFormed) {
            discoveryViewModel.setScanning(false)
            val safeRouteName = peerDeviceName.replace(" ", "_")

            // 1. Always redirect the screen first
            navigateToChat?.invoke(safeRouteName)

            // 2. Only create the socket if it is not already connected
            if (!chatViewModel.isConnected.value) {
                lifecycleScope.launch {
                    if (info.isGroupOwner) {
                        com.subhadip.oomessage.network.socket.ChatServer().startServer { socket ->
                            chatViewModel.setupConnection(socket, safeRouteName)
                        }
                    } else {
                        com.subhadip.oomessage.network.socket.ChatClient().connectToServer(ownerAddress) { socket ->
                            chatViewModel.setupConnection(socket, safeRouteName)
                        }
                    }
                }
            }
        }
    }

    private fun startAppLogic() {
        if (!::wiFiDirectManager.isInitialized) {
            wiFiDirectManager = WiFiDirectManager(this)
            wifiP2pManager = getSystemService(WIFI_P2P_SERVICE) as WifiP2pManager
            wifiP2pChannel = wifiP2pManager?.initialize(this, mainLooper, null)
            receiver = WiFiDirectReceiver(wifiP2pManager!!, wifiP2pChannel!!,
                { discoveryViewModel.updatePeers(it) },
                { info, name -> handleConnection(info, name) }
            )
        }
        if (!isReceiverRegistered) {
            registerReceiver(receiver, intentFilter)
            isReceiverRegistered = true
        }
    }

    private fun checkInitialRequirements() {
        val perms = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) perms.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        val allGranted = perms.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }
        if (!allGranted) requestPermissionLauncher.launch(perms.toTypedArray())
        else if (!checkAllHardwareOn()) showSetupPopup.value = true
        else startAppLogic()
    }

    private fun checkAllHardwareOn(): Boolean {
        val wm = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return wm.isWifiEnabled && lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    override fun onResume() {
        super.onResume()
        if (checkAllHardwareOn() && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            showSetupPopup.value = false
            startAppLogic()
        }
    }

    override fun onPause() {
        super.onPause()
        if (isReceiverRegistered) {
            unregisterReceiver(receiver)
            isReceiverRegistered = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::wiFiDirectManager.isInitialized) wiFiDirectManager.disconnect()
        stopService(Intent(this, com.subhadip.oomessage.network.p2p.WifiDirectService::class.java))
    }

}