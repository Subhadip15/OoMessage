package com.subhadip.oomessage.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Converts a raw ByteArray into a readable Hex String (Great for debugging Crypto logs)
fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }

// Formats your Room DB Long timestamps into human-readable strings for the Chat UI (e.g., "10:16 PM")
fun Long.toReadableTime(): String {
    val date = Date(this)
    val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return format.format(date)
}