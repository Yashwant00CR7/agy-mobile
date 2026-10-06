package com.yashwant.agymobile.core

import com.yashwant.agymobile.model.AgentStatus
import com.yashwant.agymobile.model.AgyEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream

@OptIn(ExperimentalCoroutinesApi::class)
class AgyProcessEngineTest {

    @Test
    fun testParseLineEvents() {
        val engine = AgyProcessEngine()

        // 1. Thought
        val evThought = engine.parseLine("{\"type\":\"thought\",\"delta\":\"Reasoning step...\"}", true)
        assertTrue(evThought is AgyEvent.Thought)
        assertEquals("Reasoning step...", (evThought as AgyEvent.Thought).delta)

        // 2. Text
        val evText = engine.parseLine("{\"type\":\"text\",\"delta\":\"Hello from Kotlin Compose!\"}", true)
        assertTrue(evText is AgyEvent.Text)
        assertEquals("Hello from Kotlin Compose!", (evText as AgyEvent.Text).delta)

        // 3. Autonomous Tool Call
        val evTool = engine.parseLine("{\"type\":\"tool_call\",\"id\":\"123\",\"name\":\"git_status\",\"args\":{}}", true)
        assertTrue(evTool is AgyEvent.ToolCall)
        assertEquals("git_status", (evTool as AgyEvent.ToolCall).name)

        // 4. Supervised Tool Call -> Approval Required
        val evAppr = engine.parseLine("{\"type\":\"tool_call\",\"id\":\"call_99\",\"name\":\"delete_file\",\"args\":{\"path\":\"temp.txt\"}}", false)
        assertTrue(evAppr is AgyEvent.ApprovalRequired)
        assertEquals("call_99", (evAppr as AgyEvent.ApprovalRequired).id)
        assertEquals("delete_file", (evAppr as AgyEvent.ApprovalRequired).name)

        // 5. Diff
        val evDiff = engine.parseLine("{\"type\":\"diff\",\"file\":\"MainActivity.kt\",\"diff\":\"+ fun test()\"}", true)
        assertTrue(evDiff is AgyEvent.Diff)
        assertEquals("MainActivity.kt", (evDiff as AgyEvent.Diff).file)

        // 6. Plaintext
        val evPlain = engine.parseLine("Simple log message", true)
        assertTrue(evPlain is AgyEvent.Text)
        assertTrue((evPlain as AgyEvent.Text).delta.contains("Simple log message"))
    }

    @Test
    fun testProcessStreamWithApprovalResolution() = runTest {
        val testDispatcher = kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)
        val engine = AgyProcessEngine(scope = this, dispatcher = testDispatcher)
        val streamData = buildString {
            append("{\"type\":\"text\",\"delta\":\"Starting work\"}\n")
            append("{\"type\":\"tool_call\",\"id\":\"c1\",\"name\":\"exec_cmd\",\"args\":{\"cmd\":\"ls\"}}\n")
            append("{\"type\":\"turn_complete\",\"summary\":\"Done\"}\n")
        }

        val emitted = mutableListOf<AgyEvent>()
        val collectJob = launch(testDispatcher) {
            engine.events.collect { emitted.add(it) }
        }

        val processJob = launch(testDispatcher) {
            engine.processStream(ByteArrayInputStream(streamData.toByteArray()), autonomous = false)
        }

        assertEquals(AgentStatus.WAITING_APPROVAL, engine.status.value)
        assertTrue(emitted.any { it is AgyEvent.ApprovalRequired })

        // Resolve approval
        val resolved = engine.resolveApproval("c1", approved = true)
        assertTrue(resolved)

        processJob.join()
        collectJob.cancel()

        assertTrue(emitted.any { it is AgyEvent.TurnComplete })
    }
}
