package com.sara.assistant.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.sara.assistant.R
import com.sara.assistant.local.LocalCommandHandler
import com.sara.assistant.logging.AuditLogger
import com.sara.assistant.online.OnlineSearch
import com.sara.assistant.security.SecurityPolicy
import com.sara.assistant.voice.VoiceInput
import com.sara.assistant.wake.WakeWordService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var txtStatus: TextView
    private lateinit var txtTranscript: TextView
    private lateinit var txtResult: TextView
    private lateinit var logger: AuditLogger
    private lateinit var security: SecurityPolicy
    private lateinit var localHandler: LocalCommandHandler
    private val onlineSearch = OnlineSearch()

    private val speechLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val text = VoiceInput.extractText(result.data)
            txtTranscript.text = text.ifBlank { "(kuch suna nahi gaya)" }
            if (text.isNotBlank()) handleCommand(text)
        } else {
            txtStatus.text = "Status: Voice input cancelled/failed"
        }
    }

    private val micPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startListening() else txtStatus.text = "Status: Mic permission denied"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtStatus = findViewById(R.id.txtStatus)
        txtTranscript = findViewById(R.id.txtTranscript)
        txtResult = findViewById(R.id.txtResult)
        logger = AuditLogger(this)
        security = SecurityPolicy()
        localHandler = LocalCommandHandler(this)

        findViewById<Button>(R.id.btnListen).setOnClickListener { ensureMicAndListen() }
        findViewById<Button>(R.id.btnStartWake).setOnClickListener { startWakeService() }
        findViewById<Button>(R.id.btnStopWake).setOnClickListener { stopWakeService() }

        logger.log("APP_START", "MainActivity created")
    }

    private fun ensureMicAndListen() {
        val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) startListening() else micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    private fun startListening() {
        txtStatus.text = "Status: Sun rahi hoon..."
        logger.log("VOICE_LISTEN_START")
        speechLauncher.launch(VoiceInput.createIntent())
    }

    private fun handleCommand(raw: String) {
        val intentName = detectIntent(raw)
        val decision = security.evaluate(intentName, raw)
        logger.log("COMMAND_RECEIVED", "intent=$intentName raw=$raw", if (decision.allowed) "OK" else "DENIED")

        if (!decision.allowed) {
            txtStatus.text = "Status: Command blocked"
            txtResult.text = "Blocked: ${decision.reason}"
            return
        }

        if (decision.requiresConfirmation) {
            AlertDialog.Builder(this)
                .setTitle("Confirmation")
                .setMessage("Kya main ye command chalaun?\n\n$raw")
                .setPositiveButton("Haan") { _, _ -> executeIntent(intentName, raw) }
                .setNegativeButton("Nahi") { _, _ ->
                    logger.log("COMMAND_CONFIRM", "intent=$intentName", "CANCELLED")
                    txtResult.text = "User ne cancel kar diya."
                }
                .show()
        } else {
            executeIntent(intentName, raw)
        }
    }

    private fun executeIntent(intentName: String, raw: String) {
        txtStatus.text = "Status: Processing..."
        when (intentName) {
            "SEARCH_WEB" -> runOnlineSearch(raw)
            "LOGS" -> {
                val logs = logger.readAll().takeLast(2000)
                txtResult.text = if (logs.isBlank()) "No logs yet." else logs
                txtStatus.text = "Status: Logs shown"
                logger.log("LOGS_VIEWED")
            }
            else -> {
                val result = localHandler.handle(intentName, raw)
                txtResult.text = result
                txtStatus.text = "Status: Done"
                logger.log("COMMAND_EXECUTED", "intent=$intentName result=$result")
            }
        }
    }

    private fun runOnlineSearch(raw: String) {
        val query = raw.replace(Regex("(?i)^(sara\\s*)?(search|khojo|dhundo)\\s*"), "").trim()
            .ifBlank { raw }
        CoroutineScope(Dispatchers.Main).launch {
            txtResult.text = "Online search chal rahi hai..."
            val result = try {
                withContext(Dispatchers.IO) { onlineSearch.search(query) }
            } catch (e: Exception) {
                "Online error: ${e.message}"
            }
            txtResult.text = result
            txtStatus.text = "Status: Done"
            logger.log("ONLINE_SEARCH", "query=$query result=$result")
        }
    }

    private fun detectIntent(raw: String): String {
        val t = raw.lowercase()
        return when {
            listOf("hello", "hi", "namaste").any { t.contains(it) } -> "HELLO"
            listOf("help", "madad").any { t.contains(it) } -> "HELP"
            listOf("time", "samay").any { t.contains(it) } -> "TIME"
            listOf("date", "tarikh", "tareekh").any { t.contains(it) } -> "DATE"
            listOf("battery").any { t.contains(it) } -> "BATTERY_STATUS"
            listOf("wifi", "wi-fi").any { t.contains(it) } -> "WIFI_STATUS"
            listOf("settings", "setting khol").any { t.contains(it) } -> "OPEN_APP"
            listOf("search", "khojo", "dhundo", "internet").any { t.contains(it) } -> "SEARCH_WEB"
            listOf("log", "logs", "audit").any { t.contains(it) } -> "LOGS"
            else -> "UNKNOWN"
        }
    }

    private fun startWakeService() {
        val intent = Intent(this, WakeWordService::class.java)
        ContextCompat.startForegroundService(this, intent)
        txtStatus.text = "Status: Wake service start requested"
        logger.log("WAKE_SERVICE_START_REQUESTED")
    }

    private fun stopWakeService() {
        stopService(Intent(this, WakeWordService::class.java))
        txtStatus.text = "Status: Wake service stop requested"
        logger.log("WAKE_SERVICE_STOP_REQUESTED")
    }
}
