package com.sara.assistant.logging

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AuditLogger(private val context: Context) {
    private val dateFmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    private val logFile: File by lazy {
        File(context.filesDir, "audit.log").apply {
            if (!exists()) createNewFile()
        }
    }

    @Synchronized
    fun log(event: String, details: String = "", status: String = "OK") {
        val line = "${dateFmt.format(Date())}\t$status\t$event\t${details.replace('\n', ' ')}\n"
        logFile.appendText(line)
    }

    fun readAll(): String = if (logFile.exists()) logFile.readText() else ""
    fun filePath(): String = logFile.absolutePath
}
