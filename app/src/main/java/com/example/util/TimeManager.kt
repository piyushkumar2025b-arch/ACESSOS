package com.example.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class TimeZoneOption(
    val id: String,
    val displayName: String,
    val offsetLabel: String
)

object TimeManager {
    val AVAILABLE_TIMEZONES = listOf(
        TimeZoneOption("Asia/Kolkata", "India Standard Time (IST)", "GMT +5:30"),
        TimeZoneOption("SYSTEM", "Device / System Time", "Auto"),
        TimeZoneOption("UTC", "UTC / GMT Universal", "GMT +0:00"),
        TimeZoneOption("Asia/Dubai", "Gulf Standard Time (GST)", "GMT +4:00"),
        TimeZoneOption("Europe/London", "London / UK Time (GMT/BST)", "GMT +0/+1"),
        TimeZoneOption("America/New_York", "US Eastern Time (EST/EDT)", "GMT -5:00"),
        TimeZoneOption("America/Los_Angeles", "US Pacific Time (PST/PDT)", "GMT -8:00"),
        TimeZoneOption("Asia/Singapore", "Singapore Time (SGT)", "GMT +8:00")
    )

    // Default to IST (Asia/Kolkata) which is appropriate for the target locale & users
    private val _selectedTimeZoneId = MutableStateFlow("Asia/Kolkata")
    val selectedTimeZoneId: StateFlow<String> = _selectedTimeZoneId.asStateFlow()

    private val _currentTime = MutableStateFlow("")
    val currentTime: StateFlow<String> = _currentTime.asStateFlow()

    private val _currentDate = MutableStateFlow("")
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()

    private val _timeZoneLabel = MutableStateFlow("IST")
    val timeZoneLabel: StateFlow<String> = _timeZoneLabel.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        updateTimeValues()
        scope.launch {
            while (true) {
                updateTimeValues()
                delay(1000)
            }
        }
    }

    fun setTimeZone(timeZoneId: String) {
        _selectedTimeZoneId.value = timeZoneId
        updateTimeValues()
    }

    private fun getActiveTimeZone(): TimeZone {
        val id = _selectedTimeZoneId.value
        return if (id == "SYSTEM") {
            TimeZone.getDefault()
        } else {
            TimeZone.getTimeZone(id)
        }
    }

    private fun updateTimeValues() {
        val now = Date()
        val tz = getActiveTimeZone()

        val timeFormat = SimpleDateFormat("h:mm:ss a", Locale.ENGLISH).apply {
            timeZone = tz
        }
        val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.ENGLISH).apply {
            timeZone = tz
        }

        _currentTime.value = timeFormat.format(now)
        _currentDate.value = dateFormat.format(now)

        val selectedOption = AVAILABLE_TIMEZONES.find { it.id == _selectedTimeZoneId.value }
        _timeZoneLabel.value = when (_selectedTimeZoneId.value) {
            "Asia/Kolkata" -> "IST (India)"
            "SYSTEM" -> "System"
            "UTC" -> "UTC"
            "Asia/Dubai" -> "GST (Dubai)"
            "Europe/London" -> "London"
            "America/New_York" -> "EST (New York)"
            "America/Los_Angeles" -> "PST (LA)"
            "Asia/Singapore" -> "SGT (Singapore)"
            else -> selectedOption?.displayName ?: tz.displayName
        }
    }
}
