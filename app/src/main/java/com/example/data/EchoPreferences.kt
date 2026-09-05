package com.example.data

import android.content.Context
import android.content.SharedPreferences

class EchoPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("echo_game_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CURRENT_LEVEL = "echo_current_level"
        private const val KEY_TOKENS = "echo_tokens"
        private const val KEY_ECHO_BREAKERS = "echo_breakers"
        private const val KEY_TOTAL_ECHOES = "echo_stats_total_echoes"
        private const val KEY_COMPLETED_LEVELS = "echo_completed_levels"
        private const val KEY_AD_FREE = "echo_is_ad_free"
        private const val KEY_SOUND = "echo_sound_enabled"
        private const val KEY_HAPTICS = "echo_haptics_enabled"
        private const val KEY_STROKE_THEME = "echo_stroke_theme"
        private const val KEY_ECHO_THEME = "echo_theme_choice"
        private const val KEY_LAST_DAILY = "echo_last_daily_date"
        private const val KEY_TEST_ADS = "echo_test_ads_enabled"
        private const val KEY_LANGUAGE = "echo_language_code"
    }

    var languageCode: String
        get() = prefs.getString(KEY_LANGUAGE, "en") ?: "en"
        set(value) = prefs.edit().putString(KEY_LANGUAGE, value).apply()

    var currentLevelIndex: Int
        get() = prefs.getInt(KEY_CURRENT_LEVEL, 0)
        set(value) = prefs.edit().putInt(KEY_CURRENT_LEVEL, value).apply()

    var tokens: Int
        get() = prefs.getInt(KEY_TOKENS, 5) // 5 hint tokens
        set(value) = prefs.edit().putInt(KEY_TOKENS, value.coerceAtLeast(0)).apply()

    var echoBreakers: Int
        get() = prefs.getInt(KEY_ECHO_BREAKERS, 2) // Start with 2 free Echo Breakers
        set(value) = prefs.edit().putInt(KEY_ECHO_BREAKERS, value.coerceAtLeast(0)).apply()

    var totalEchoes: Int
        get() = prefs.getInt(KEY_TOTAL_ECHOES, 0)
        set(value) = prefs.edit().putInt(KEY_TOTAL_ECHOES, value).apply()

    var isAdFree: Boolean
        get() = prefs.getBoolean(KEY_AD_FREE, false)
        set(value) = prefs.edit().putBoolean(KEY_AD_FREE, value).apply()

    var soundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND, value).apply()

    var hapticsEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTICS, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTICS, value).apply()

    var strokeThemeName: String
        get() = prefs.getString(KEY_STROKE_THEME, "NEON_CYAN") ?: "NEON_CYAN"
        set(value) = prefs.edit().putString(KEY_STROKE_THEME, value).apply()

    var echoThemeName: String
        get() = prefs.getString(KEY_ECHO_THEME, "ELECTRIC_RED") ?: "ELECTRIC_RED"
        set(value) = prefs.edit().putString(KEY_ECHO_THEME, value).apply()

    var lastDailyCompletedDate: String
        get() = prefs.getString(KEY_LAST_DAILY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LAST_DAILY, value).apply()

    var isTestAdsEnabled: Boolean
        get() = prefs.getBoolean(KEY_TEST_ADS, false)
        set(value) = prefs.edit().putBoolean(KEY_TEST_ADS, value).apply()

    fun getCompletedLevels(): Set<Int> {
        val stringSet = prefs.getStringSet(KEY_COMPLETED_LEVELS, emptySet()) ?: emptySet()
        return stringSet.mapNotNull { it.toIntOrNull() }.toSet()
    }

    fun markLevelCompleted(levelIndex: Int) {
        val currentSet = getCompletedLevels().toMutableSet()
        currentSet.add(levelIndex)
        prefs.edit().putStringSet(KEY_COMPLETED_LEVELS, currentSet.map { it.toString() }.toSet()).apply()
        if (levelIndex >= currentLevelIndex) {
            currentLevelIndex = levelIndex + 1
        }
    }

    fun incrementTotalEchoes() {
        totalEchoes += 1
    }

    fun addTokens(count: Int) {
        tokens += count
    }

    fun addBreakers(count: Int) {
        echoBreakers += count
    }

    fun useToken(): Boolean {
        return if (tokens > 0) {
            tokens -= 1
            true
        } else false
    }

    fun useBreaker(): Boolean {
        return if (echoBreakers > 0) {
            echoBreakers -= 1
            true
        } else false
    }

    var hasFreshStartV2: Boolean
        get() = prefs.getBoolean("echo_v2_fresh_start_done", false)
        set(value) = prefs.edit().putBoolean("echo_v2_fresh_start_done", value).apply()

    fun resetAllProgress() {
        prefs.edit()
            .putInt(KEY_CURRENT_LEVEL, 0)
            .putInt(KEY_TOTAL_ECHOES, 0)
            .putInt(KEY_TOKENS, 5)
            .putInt(KEY_ECHO_BREAKERS, 2)
            .remove(KEY_COMPLETED_LEVELS)
            .remove(KEY_LAST_DAILY)
            .apply()
    }
}
