package com.yashwant.agymobile.service

import org.junit.Assert.*
import org.junit.Test

class AgyAgentServiceTest {

    @Test
    fun testActionConstants() {
        assertEquals("com.yashwant.agymobile.ACTION_START", AgyAgentService.ACTION_START)
        assertEquals("com.yashwant.agymobile.ACTION_STOP", AgyAgentService.ACTION_STOP)
        assertEquals("com.yashwant.agymobile.ACTION_APPROVE", AgyAgentService.ACTION_APPROVE)
        assertEquals("com.yashwant.agymobile.ACTION_DENY", AgyAgentService.ACTION_DENY)
        assertEquals("extra_tool_id", AgyAgentService.EXTRA_TOOL_ID)
        assertEquals("agy_agent_service_channel", AgyAgentService.CHANNEL_ID)
        assertEquals(1001, AgyAgentService.NOTIFICATION_ID)
    }
}
