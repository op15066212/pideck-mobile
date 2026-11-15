package dev.pideck.nativeapp.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 目标模式注入的记账行 → 一条小横条。
 */
class GoalMarkerTest {

    @Test
    fun `goal mode banner line is recognised`() {
        assertTrue(isGoalModeLine("[GOAL MODE ACTIVE] Round: 1/32"))
        assertTrue(isGoalModeLine("  [goal mode active] round: 3/32  "))
    }

    @Test
    fun `rounds used and continue prompt are recognised`() {
        assertTrue(isGoalModeLine("Rounds used: 0/32"))
        assertTrue(isGoalModeLine("Continue the current goal."))
    }

    @Test
    fun `ordinary sentence mentioning goal is not a banner`() {
        assertFalse(isGoalModeLine("这一轮的目标是把 git 历史清一下，Round: 1/32 是宿主自己写的"))
        assertFalse(isGoalModeLine(""))
    }

    @Test
    fun `banner is lifted out of the body`() {
        val text = "[GOAL MODE ACTIVE] Round: 1/32\n正文第一段\n正文第二段"
        val (banner, body) = splitGoalBanner(text)
        assertEquals("[GOAL MODE ACTIVE] Round: 1/32", banner)
        assertEquals("正文第一段\n正文第二段", body)
    }

    @Test
    fun `several markers collapse into one line`() {
        val text = "Continue the current goal.\nRounds used: 0/32\nIf done, write GOAL_COMPLETE."
        val (banner, body) = splitGoalBanner(text)
        assertTrue(banner!!.contains("Continue the current goal."))
        assertTrue(banner.contains("Rounds used: 0/32"))
        assertTrue(banner.contains(" · "))
        assertEquals("", body)
    }

    @Test
    fun `text without markers is returned untouched`() {
        val text = "普通回复\n第二行"
        assertEquals(null, splitGoalBanner(text).first)
        assertEquals(text, splitGoalBanner(text).second)
    }
}
