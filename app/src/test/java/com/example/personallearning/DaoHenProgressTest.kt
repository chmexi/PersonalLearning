package com.example.personallearning

import com.example.personallearning.data.model.DaoHenEntry
import com.example.personallearning.ui.viewmodel.toProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DaoHenProgressTest {
    @Test
    fun oldRecordUsesAllSevenQuestions() {
        val entry = DaoHenEntry(date = "2026-01-01", q1 = "1", q2 = "2", q3 = "3", q4 = "4", q5 = "5", q6 = "6")
        val progress = entry.toProgress()
        assertEquals(6, progress.answeredCount)
        assertFalse(progress.isComplete)
        assertEquals("6", progress.mainStone)
    }

    @Test
    fun newRecordUsesFiveAnalysisFields() {
        val entry = DaoHenEntry(date = "2026-01-01", transcript = "讲述", facts = "事实", emotions = "情绪", stone = "石头", betterChoice = "选择")
        val progress = entry.toProgress()
        assertEquals(5, progress.answeredCount)
        assertTrue(progress.isComplete)
        assertEquals("石头", progress.mainStone)
    }
}
