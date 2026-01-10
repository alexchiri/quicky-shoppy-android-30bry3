package com.kroslabs.quickyshoppy.data.local

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DebugLogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val tag: String,
    val message: String,
    val level: LogLevel = LogLevel.INFO
) {
    val formattedTimestamp: String
        get() = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(timestamp))

    val formattedDate: String
        get() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
}

enum class LogLevel {
    DEBUG, INFO, WARNING, ERROR, SUCCESS
}

object DebugLogManager {
    private const val MAX_LOGS = 500

    private val _logs = MutableStateFlow<List<DebugLogEntry>>(emptyList())
    val logs: StateFlow<List<DebugLogEntry>> = _logs.asStateFlow()

    fun log(tag: String, message: String, level: LogLevel = LogLevel.INFO) {
        val entry = DebugLogEntry(
            tag = tag,
            message = message,
            level = level
        )
        synchronized(_logs) {
            val currentLogs = _logs.value.toMutableList()
            currentLogs.add(0, entry) // Add to beginning for newest first
            if (currentLogs.size > MAX_LOGS) {
                _logs.value = currentLogs.take(MAX_LOGS)
            } else {
                _logs.value = currentLogs
            }
        }
        // Also log to Android logcat for debugging
        android.util.Log.d("DebugLog/$tag", message)
    }

    fun debug(tag: String, message: String) = log(tag, message, LogLevel.DEBUG)
    fun info(tag: String, message: String) = log(tag, message, LogLevel.INFO)
    fun warning(tag: String, message: String) = log(tag, message, LogLevel.WARNING)
    fun error(tag: String, message: String) = log(tag, message, LogLevel.ERROR)
    fun success(tag: String, message: String) = log(tag, message, LogLevel.SUCCESS)

    fun clear() {
        _logs.value = emptyList()
    }

    fun getLogsAsText(): String {
        return _logs.value.joinToString("\n") { entry ->
            "[${entry.formattedDate}] [${entry.level}] [${entry.tag}] ${entry.message}"
        }
    }
}
