package com.sara.assistant.voice

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import java.util.Locale

object VoiceInput {
    const val REQ_CODE_SPEECH = 1001

    fun createIntent(): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Boliye...")
        }
    }

    fun extractText(data: Intent?): String {
        val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        return results?.firstOrNull().orEmpty()
    }
}
