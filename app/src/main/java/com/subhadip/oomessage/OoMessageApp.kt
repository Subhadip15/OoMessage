package com.subhadip.oomessage

import android.app.Application
import com.subhadip.oomessage.data.AppDatabase
import com.subhadip.oomessage.repository.ChatRepository
import com.subhadip.oomessage.repository.PeerRepository

class OoMessageApp : Application() {

    // Lazy initialization ensures the database is created only when needed
    // and keeps a single instance (Singleton) for the entire app's lifecycle.
    val database by lazy { AppDatabase.getDatabase(this) }

    // Initialize shared dependencies here so ViewModels can access them easily
    val peerRepository by lazy { PeerRepository(database.userDao()) }

    // ChatRepository now only needs the messageDao to save/load your local chat history
    val chatRepository by lazy { ChatRepository(database.messageDao()) }

    override fun onCreate() {
        super.onCreate()
        // Any other global initialization goes here
    }
}

