package com.sara.assistant.local

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.provider.Settings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LocalCommandHandler(private val context: Context) {

    fun handle(intentName: String, raw: String): String {
        return when (intentName) {
            "HELLO" -> "Namaste! Main Sara hoon."
            "HELP" -> "Main time, date, battery, wifi status, aur web search kar sakti hoon."
            "TIME" -> "Abhi time hai: " + SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            "DATE" -> "Aaj ki date hai: " + SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
            "BATTERY_STATUS" -> batteryStatus()
            "WIFI_STATUS" -> "Wi-Fi settings khol rahi hoon."
            "OPEN_APP" -> openSettingsApp(raw)
            else -> "Local handler ke paas is command ka support nahi hai."
        }
    }

    private fun batteryStatus(): String {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val status = context.registerReceiver(null, filter) ?: return "Battery status unavailable."
        val level = status.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = status.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val pct = if (level >= 0 && scale > 0) (level * 100 / scale) else -1
        return if (pct >= 0) "Battery $pct% hai." else "Battery status unavailable."
    }

    private fun openSettingsApp(raw: String): String {
        // Simple example: open Android settings. Extend with package allowlist.
        val i = Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(i)
        return "Settings app khol di."
    }
}
