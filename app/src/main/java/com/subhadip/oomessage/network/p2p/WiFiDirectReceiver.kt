package com.subhadip.oomessage.network.p2p

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager

class WiFiDirectReceiver(
    private val manager: WifiP2pManager,
    private val channel: WifiP2pManager.Channel,
    private val onPeersUpdated: (List<WifiP2pDevice>) -> Unit,
    private val onConnectionInfoAvailable: (WifiP2pInfo, String) -> Unit
) : BroadcastReceiver() {

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            // ১. যখন আশেপাশে নতুন কোনো ডিভাইস পাওয়া যায় বা লিস্ট আপডেট হয়
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                manager.requestPeers(channel) { peers ->
                    onPeersUpdated(peers?.deviceList?.toList() ?: emptyList())
                }
            }

            // ২. যখন কানেকশন স্ট্যাটাস পরিবর্তন হয় (Connect/Disconnect)
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                manager.requestConnectionInfo(channel) { info ->
                    // groupFormed মানে হলো কানেকশন সফলভাবে তৈরি হয়েছে
                    if (info != null && info.groupFormed) {
                        manager.requestGroupInfo(channel) { group ->
                            if (group != null) {
                                // অপর পাশের ডিভাইসের নাম বের করা (Server বা Client অনুযায়ী)
                                val peerName = if (info.isGroupOwner) {
                                    group.clientList?.firstOrNull()?.deviceName ?: "Client Device"
                                } else {
                                    group.owner?.deviceName ?: "Server Device"
                                }

                                // MainActivity-তে সিগন্যাল পাঠানো যাতে চ্যাট স্ক্রিন ওপেন হয়
                                onConnectionInfoAvailable(info, peerName)
                            }
                        }
                    }
                }
            }

            // ৩. যখন ফোনের ওয়াইফাই অন বা অফ করা হয়
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                if (state == WifiP2pManager.WIFI_P2P_STATE_DISABLED) {
                    // ওয়াইফাই অফ হয়ে গেলে এখানে চাইলে টোস্ট বা ডিসকানেক্ট লজিক দিতে পারেন
                }
            }

            // ৪. নিজের ডিভাইসের নাম বা স্ট্যাটাস পরিবর্তন হলে (ঐচ্ছিক কিন্তু দরকারি)
            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                // দরকার হলে এখানে নিজের ডিভাইসের আপডেট নিতে পারেন
            }
        }
    }
}