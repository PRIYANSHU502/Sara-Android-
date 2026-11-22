package com.sara.assistant.security

data class CommandDecision(
    val allowed: Boolean,
    val requiresConfirmation: Boolean = false,
    val reason: String = ""
)

class SecurityPolicy {
    // Trusted/local allowlist. Unknown commands are denied by default.
    private val trustedIntents = setOf(
        "TIME", "DATE", "OPEN_APP", "WIFI_STATUS", "BATTERY_STATUS",
        "SEARCH_WEB", "HELLO", "HELP", "LOGS"
    )

    private val confirmationRequired = setOf("OPEN_URL", "SEARCH_WEB")

    private val blockedKeywords = listOf(
        "rm -rf", "shutdown", "reboot", "format", "mkfs", "dd if=", "su ", "sudo "
    )

    fun evaluate(intent: String, rawCommand: String): CommandDecision {
        val lower = rawCommand.lowercase()
        if (blockedKeywords.any { lower.contains(it) }) {
            return CommandDecision(false, reason = "Blocked keyword detected")
        }
        if (intent !in trustedIntents && intent != "OPEN_URL") {
            return CommandDecision(false, reason = "Command not in trusted allowlist")
        }
        if (intent in confirmationRequired) {
            return CommandDecision(true, requiresConfirmation = true, reason = "Confirmation required")
        }
        return CommandDecision(true)
    }
}
