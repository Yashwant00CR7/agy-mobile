package com.yashwant.agymobile.core

import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files

class UsageCreditsManagerTest {

    private lateinit var tempDir: File
    private lateinit var manager: UsageCreditsManager

    @Before
    fun setUp() {
        tempDir = Files.createTempDirectory("usage_credits_test").toFile()
        manager = UsageCreditsManager(tempDir)
    }

    @After
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun testInitialMetrics() {
        val metrics = manager.getMetrics()
        assertEquals(0L, metrics.dailyPromptTokens)
        assertEquals(0L, metrics.dailyCompletionTokens)
        assertEquals(1_000_000L, metrics.dailyQuotaTokens)
        assertEquals(0L, metrics.totalTurns)
        assertEquals(0f, metrics.dailyUsagePercent, 0.001f)
        assertEquals(1_000_000L, metrics.remainingDailyTokens)
    }

    @Test
    fun testRecordTurnEstimation() {
        manager.recordTurn(
            promptText = "Explain quantum computing in simple words",
            completionText = "Quantum computing uses qubits which can be 0, 1, or both simultaneously.",
            model = "Gemini 3.8 Flash (High)"
        )

        val metrics = manager.getMetrics()
        assertTrue("Prompt tokens should be estimated > 0", metrics.dailyPromptTokens > 0)
        assertTrue("Completion tokens should be estimated > 0", metrics.dailyCompletionTokens > 0)
        assertEquals(1L, metrics.totalTurns)
        assertEquals(metrics.dailyPromptTokens, metrics.totalPromptTokens)
        assertEquals(metrics.dailyCompletionTokens, metrics.totalCompletionTokens)

        val breakdown = metrics.modelBreakdown
        assertTrue(breakdown.containsKey("Gemini 3.8 Flash (High)"))
        assertEquals(metrics.dailyTotalTokens, breakdown["Gemini 3.8 Flash (High)"])
    }

    @Test
    fun testRecordTurnExactTokens() {
        manager.recordTurn(
            promptText = "Hello",
            completionText = "World",
            model = "Gemini 3.1 Pro (High)",
            exactPromptTokens = 150L,
            exactCompletionTokens = 350L
        )

        val metrics = manager.getMetrics()
        assertEquals(150L, metrics.dailyPromptTokens)
        assertEquals(350L, metrics.dailyCompletionTokens)
        assertEquals(500L, metrics.dailyTotalTokens)
        assertEquals(1L, metrics.totalTurns)
        assertEquals(999_500L, metrics.remainingDailyTokens)
        assertEquals(0.0005f, metrics.dailyUsagePercent, 0.0001f)
    }

    @Test
    fun testPersistence() {
        manager.recordTurn(
            promptText = "Persistent test",
            completionText = "Saved to file",
            model = "Gemini 3.8 Flash (High)",
            exactPromptTokens = 500L,
            exactCompletionTokens = 1000L
        )

        // Instantiate a second manager on the same directory
        val secondManager = UsageCreditsManager(tempDir)
        val metrics = secondManager.getMetrics()

        assertEquals(500L, metrics.dailyPromptTokens)
        assertEquals(1000L, metrics.dailyCompletionTokens)
        assertEquals(1500L, metrics.dailyTotalTokens)
        assertEquals(1L, metrics.totalTurns)
    }

    @Test
    fun testResetDailyUsage() {
        manager.recordTurn(
            promptText = "Test",
            completionText = "Response",
            model = "Gemini 3.8 Flash",
            exactPromptTokens = 200L,
            exactCompletionTokens = 800L
        )

        manager.resetDailyUsage()
        val metrics = manager.getMetrics()

        assertEquals(0L, metrics.dailyPromptTokens)
        assertEquals(0L, metrics.dailyCompletionTokens)
        assertEquals(0L, metrics.dailyTotalTokens)
        // All-time metrics should be preserved
        assertEquals(200L, metrics.totalPromptTokens)
        assertEquals(800L, metrics.totalCompletionTokens)
        assertEquals(1L, metrics.totalTurns)
    }
}
