package com.sara.assistant.online

import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class OnlineSearch {
    private val client = OkHttpClient.Builder()
        .callTimeout(10, TimeUnit.SECONDS)
        .build()

    fun search(query: String): String {
        val q = URLEncoder.encode(query, "UTF-8")
        val url = "https://duckduckgo.com/html/?q=$q"
        val req = Request.Builder()
            .url(url)
            .header("User-Agent", "SaraAssistant/1.0")
            .build()

        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return "Online search failed: HTTP ${resp.code}"
            val body = resp.body?.string().orEmpty()
            // Very simple extraction: first result snippet-ish text fallback.
            val title = Regex("""class="result__a"[^>]*>(.*?)</a>""")
                .find(body)?.groupValues?.getOrNull(1)
                ?.replace(Regex("<.*?>"), "")
                ?.trim()
            return if (!title.isNullOrBlank()) {
                "Top result: $title\nSource: $url"
            } else {
                "Search complete. Source: $url"
            }
        }
    }
}
