package com.subhadip.oomessage.network.p2p

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.os.Looper

class WiFiDirectManager(private val context: Context) {

    private val manager: WifiP2pManager? = context.getSystemService(Context.WIFI_P2P_SERVICE) as? WifiP2pManager
    private val channel: WifiP2pManager.Channel? = manager?.initialize(context, Looper.getMainLooper(), null)

    @SuppressLint("MissingPermission")
    fun startDiscovery(onSuccess: () -> Unit, onFailure: (Int) -> Unit) {
        manager?.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() = onSuccess()
            override fun onFailure(reasonCode: Int) = onFailure(reasonCode)
        })
    }

    @SuppressLint("MissingPermission")
    fun requestPeers(onPeersAvailable: (WifiP2pDeviceList) -> Unit) {
        manager?.requestPeers(channel) { peers ->
            if (peers != null) onPeersAvailable(peers)
        }
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(deviceAddress: String, onSuccess: () -> Unit, onFailure: (Int) -> Unit) {
        val config = WifiP2pConfig().apply {
            this.deviceAddress = deviceAddress
            // FIX: Explicitly setup WPS. Without this, many phones will instantly reject the connection!
            this.wps.setup = WpsInfo.PBC
        }

        manager?.connect(channel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() = onSuccess()
            override fun onFailure(reasonCode: Int) = onFailure(reasonCode)
        })
    }

    fun requestConnectionInfo(onConnectionInfoAvailable: (WifiP2pInfo) -> Unit) {
        manager?.requestConnectionInfo(channel) { info ->
            if (info != null) onConnectionInfoAvailable(info)
        }
    }
    // Add this to the bottom of WiFiDirectManager.kt
    @SuppressLint("MissingPermission")
    fun disconnect() {
        manager?.removeGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                println("Disconnected successfully")
            }
            override fun onFailure(reason: Int) {
                println("Disconnect failed: $reason")
            }
        })
    }
}