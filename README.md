# OoMessage

A secure, internet-free peer-to-peer (P2P) messaging application for Android.

## Overview

OoMessage enables users to discover, connect, and chat with nearby devices entirely over local Wi-Fi networks using Wi-Fi Direct. It requires no cellular data or internet connection, providing a private and decentralized communication channel.

## Features

* **Offline Discovery:** Scans for nearby devices using Wi-Fi Direct.
* **Peer-to-Peer Connection:** Establishes direct TCP socket connections between devices.
* **Real-time Chat:** Instant messaging interface built with Jetpack Compose.
* **Background Resilience:** Uses Foreground Services to keep the connection alive even when the app is minimized.
* **Local Storage:** Chat history is saved locally on the device using Room Database.
* **End-to-End Encryption (Planned):** Infrastructure in place for future E2EE implementation.
* **Modern UI:** Built with Material 3 design principles, featuring "Pull-to-Refresh" for easy discovery.

## Tech Stack

* **Language:** Kotlin
* **UI Framework:** Jetpack Compose (Material 3)
* **Networking:** Android Wi-Fi Direct (`WifiP2pManager`), TCP Sockets (`java.net.Socket`)
* **Architecture:** MVVM (Model-View-ViewModel)
* **Concurrency:** Kotlin Coroutines & Flow
* **Database:** Room (SQLite)

## Setup and Installation

### Prerequisites

* Android Studio (Jellyfish or newer recommended)
* Two physical Android devices (Wi-Fi Direct cannot be tested on emulators)
* Devices must have Android 7.0 (API 24) or higher

### Building the Project

1.  **Clone the repository:**
    ```bash
    git clone [https://github.com/YOUR_USERNAME/OoMessage.git](https://github.com/YOUR_USERNAME/OoMessage.git)
    cd OoMessage
    ```
2.  **Open in Android Studio:**
    * Launch Android Studio.
    * Select "Open" and navigate to the cloned `OoMessage` directory.
3.  **Sync Gradle:**
    * Allow Android Studio to sync the project files and download dependencies.
4.  **Run on Physical Devices:**
    * Connect two Android devices via USB (or wireless debugging).
    * Select each device from the run menu and click the "Run" button to install the app.

## Testing the App

1.  Ensure **Wi-Fi** and **Location** services are enabled on both devices.
2.  Open the app on both devices and grant the necessary permissions (Location, Nearby Devices).
3.  On Device A, pull down to refresh the discovery screen to find Device B.
4.  Tap on Device B's name in the list.
5.  Device B may receive a system prompt to accept the Wi-Fi Direct connection.
6.  Once connected, both devices will be routed to the chat screen.
7.  Start messaging!

## Future Enhancements

* Implementation of AES-256 End-to-End Encryption.
* Media sharing capabilities (images, audio).
* Group chat functionality.
* Read receipts.

## License

MIT
