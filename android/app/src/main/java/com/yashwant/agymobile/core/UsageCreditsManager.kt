package com.yashwant.agymobile.core

import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class UsageMetrics(
    val dailyPromptTokens: Long = 0,
    val dailyCompletionTokens: Long = 0,
    val dailyQuotaTokens: Long = 1_000_000,
    val totalPromptTokens: Long = 0,
    val totalCompletionTokens: Long = 0,
    val totalTurns: Long = 0,
    val lastDateKey: String = "",
    val modelBreakdown: Map<String, Long> = emptyMap(),
    val tierLabel: String = "Free Tier"
) {
    val dailyTotalTokens: Long get() = dailyPromptTokens + dailyCompletionTokens
    val totalAllTimeTokens: Long get() = totalPromptTokens + totalCompletionTokens
    val dailyUsagePercent: Float get() = if (dailyQuotaTokens <= 0) 0f else (dailyTotalTokens.toFloat() / dailyQuotaTokens).coerceIn(0f, 1f)
    val remainingDailyTokens: Long get() = (dailyQuotaTokens - dailyTotalTokens).coerceAtLeast(0)
}

class UsageCreditsManager(
    private val storageDir: File
) {
    private val configFile = File(storageDir, "usage_credits.json")
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    @Volatile
    private var cachedMetrics: UsageMetrics = loadMetrics()

    private fun currentDateKey(): String = dateFormat.format(Date())

    @Synchronized
    fun getMetrics(): UsageMetrics {
        checkDailyReset()
        return cachedMetrics
    }

    @Synchronized
    private fun checkDailyReset() {
        val today = currentDateKey()
        if (cachedMetrics.lastDateKey.isNotBlank() && cachedMetrics.lastDateKey != today) {
            // New day: reset daily counters, preserve total and quota
            cachedMetrics = cachedMetrics.copy(
                dailyPromptTokens = 0,
                dailyCompletionTokens = 0,
                lastDateKey = today
            )
            saveMetrics(cachedMetrics)
        } else if (cachedMetrics.lastDateKey.isBlank()) {
            cachedMetrics = cachedMetrics.copy(lastDateKey = today)
            saveMetrics(cachedMetrics)
        }
    }

    fun estimateTokens(text: String): Long {
        if (text.isBlank()) return 0
        return Math.max(1, (text.length / 3.8).toLong())
    }

    @Synchronized
    fun recordTurn(
        promptText: String,
        completionText: String,
        model: String,
        exactPromptTokens: Long? = null,
        exactCompletionTokens: Long? = null
    ): UsageMetrics {
        checkDailyReset()

        val promptTokens = exactPromptTokens ?: estimateTokens(promptText)
        val completionTokens = exactCompletionTokens ?: estimateTokens(completionText)
        val turnTokens = promptTokens + completionTokens

        val currentBreakdown = cachedMetrics.modelBreakdown.toMutableMap()
        currentBreakdown[model] = (currentBreakdown[model] ?: 0L) + turnTokens

        cachedMetrics = cachedMetrics.copy(
            dailyPromptTokens = cachedMetrics.dailyPromptTokens + promptTokens,
            dailyCompletionTokens = cachedMetrics.dailyCompletionTokens + completionTokens,
            totalPromptTokens = cachedMetrics.totalPromptTokens + promptTokens,
            totalCompletionTokens = cachedMetrics.totalCompletionTokens + completionTokens,
            totalTurns = cachedMetrics.totalTurns + 1,
            lastDateKey = currentDateKey(),
            modelBreakdown = currentBreakdown
        )

        saveMetrics(cachedMetrics)
        return cachedMetrics
    }

    @Synchronized
    fun resetDailyUsage() {
        cachedMetrics = cachedMetrics.copy(
            dailyPromptTokens = 0,
            dailyCompletionTokens = 0,
            lastDateKey = currentDateKey()
        )
        saveMetrics(cachedMetrics)
    }

    @Synchronized
    fun setDailyQuota(newQuota: Long) {
        cachedMetrics = cachedMetrics.copy(dailyQuotaTokens = newQuota.coerceAtLeast(10_000))
        saveMetrics(cachedMetrics)
    }

    @Synchronized
    fun setTier(tier: String) {
        cachedMetrics = cachedMetrics.copy(tierLabel = tier)
        saveMetrics(cachedMetrics)
    }

    @Synchronized
    fun resetAllMetrics() {
        cachedMetrics = UsageMetrics(lastDateKey = currentDateKey())
        saveMetrics(cachedMetrics)
    }

    private fun loadMetrics(): UsageMetrics {
        if (!configFile.exists()) {
            return UsageMetrics(lastDateKey = currentDateKey())
        }
        return try {
            val json = JSONObject(configFile.readText())
            val breakdown = mutableMapOf<String, Long>()
            val breakdownObj = json.optJSONObject("modelBreakdown")
            if (breakdownObj != null) {
                for (key in breakdownObj.keys()) {
                    breakdown[key] = breakdownObj.optLong(key, 0L)
                }
            }

            UsageMetrics(
                dailyPromptTokens = json.optLong("dailyPromptTokens", 0L),
                dailyCompletionTokens = json.optLong("dailyCompletionTokens", 0L),
                dailyQuotaTokens = json.optLong("dailyQuotaTokens", 1_000_000L),
                totalPromptTokens = json.optLong("totalPromptTokens", 0L),
                totalCompletionTokens = json.optLong("totalCompletionTokens", 0L),
                totalTurns = json.optLong("totalTurns", 0L),
                lastDateKey = json.optString("lastDateKey", currentDateKey()),
                modelBreakdown = breakdown,
                tierLabel = json.optString("tierLabel", "Free Tier")
            )
        } catch (_: Exception) {
            UsageMetrics(lastDateKey = currentDateKey())
        }
    }

    private fun saveMetrics(metrics: UsageMetrics) {
        try {
            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }
            val json = JSONObject().apply {
                put("dailyPromptTokens", metrics.dailyPromptTokens)
                put("dailyCompletionTokens", metrics.dailyCompletionTokens)
                put("dailyQuotaTokens", metrics.dailyQuotaTokens)
                put("totalPromptTokens", metrics.totalPromptTokens)
                put("totalCompletionTokens", metrics.totalCompletionTokens)
                put("totalTurns", metrics.totalTurns)
                put("lastDateKey", metrics.lastDateKey)
                put("tierLabel", metrics.tierLabel)
                put("modelBreakdown", JSONObject().apply {
                    metrics.modelBreakdown.forEach { (k, v) -> put(k, v) }
                })
            }
            configFile.writeText(json.toString(2))
        } catch (_: Exception) {}
    }
}
