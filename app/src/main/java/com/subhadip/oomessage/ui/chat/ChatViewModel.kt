package com.subhadip.oomessage.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subhadip.oomessage.data.model.Message
import com.subhadip.oomessage.network.socket.ConnectionHandler
import com.subhadip.oomessage.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.Socket

class ChatViewModel(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private var connectionHandler: ConnectionHandler? = null
    private var currentPeerMacAddress: String = "UNKNOWN_PEER"

    fun setupConnection(socket: Socket, peerMacAddress: String) {
        // FIX 1: যদি অলরেডি এই পিয়ার-এর সাথে কানেক্টেড থাকি, তবে নতুন করে হ্যান্ডলার বানানোর দরকার নেই।
        if (_isConnected.value && currentPeerMacAddress == peerMacAddress) {
            return
        }

        currentPeerMacAddress = peerMacAddress
        _isConnected.value = true

        // চ্যাট হিস্ট্রি লোড করা
        viewModelScope.launch {
            chatRepository.getMessagesForPeer(peerMacAddress).collect { history ->
                _messages.value = history
            }
        }

        // নতুন কানেকশন হ্যান্ডলার সেটআপ
        connectionHandler = ConnectionHandler(
            socket = socket,
            onMessageReceived = { incomingText ->
                viewModelScope.launch {
                    chatRepository.saveIncomingMessage(incomingText, currentPeerMacAddress)
                }
            },
            onDisconnected = {
                _isConnected.value = false
                connectionHandler = null // FIX 2: ডিসকানেক্ট হলে হ্যান্ডলার ক্লিয়ার করে দিন
            }
        )
    }

    fun sendMessage(text: String) {
        if (text.isNotBlank()) {
            viewModelScope.launch {
                chatRepository.saveOutgoingMessage(text, currentPeerMacAddress)

                // সকেট সচল থাকলে মেসেজ পাঠান
                if (_isConnected.value) {
                    connectionHandler?.sendMessage(text)
                }
            }
        }
    }

    // ViewModel যখন ধ্বংস হবে (App Close), তখন সকেট পরিষ্কার করা ভালো
    override fun onCleared() {
        super.onCleared()
        _isConnected.value = false
        // এখানে চাইলে কানেকশন ক্লোজ করার লজিক দিতে পারেন
    }
}