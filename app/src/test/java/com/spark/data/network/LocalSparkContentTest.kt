package com.spark.data.network

import com.spark.ui.screens.dashboard.DynamicSparkCatalog
import org.junit.Assert.*
import org.junit.Test

class LocalSparkContentTest {
    @Test
    fun bundledSuggestionsRemainSmallDiverseAndNonRepeatingUntilExhausted() {
        val seen = mutableSetOf<String>()
        repeat(6) {
            val cards = LocalSparkContent.getDiverseMultidisciplinaryMix(seen)
            assertEquals(6, cards.size)
            assertEquals(6, cards.map { it.domain }.toSet().size)
            cards.forEach { card ->
                assertTrue(card.question.length in 12..200)
                assertTrue(card.question.endsWith("?"))
                assertTrue(seen.add(card.question))
            }
        }
        assertEquals(36, seen.size)
    }

    @Test
    fun allDirectFallbackRoutesStayLocalAndHaveCompleteBoundedLessons() {
        listOf("black hole", "dopamine", "antikythera", "octopus", "crispr", "benford", "quantum", "gps", "unmatched topic").forEach { topic ->
            val task = LocalSparkContent.getCuratedHighQualityFallback(topic)
            assertFalse(task.isAI)
            assertEquals(150, task.xp)
            assertFalse(task.title.isNullOrBlank())
            assertFalse(task.description.isNullOrBlank())
            assertTrue(task.description!!.split(Regex("\\s+")).size <= 150)
            assertFalse(task.steps.isNullOrEmpty())
            assertFalse(task.funFact.isNullOrBlank())
        }
    }

    @Test
    fun catalogKeepsExistingSizeAndProvidesShortCompleteLessons() {
        assertEquals(29, DynamicSparkCatalog.catalog.size)
        assertEquals(29, DynamicSparkCatalog.catalog.map { it.title }.toSet().size)
        DynamicSparkCatalog.catalog.forEach { lesson ->
            assertTrue(lesson.title.endsWith("?"))
            assertTrue(lesson.description.split(Regex("\\s+")).size in 30..150)
            assertTrue(lesson.steps.isNotEmpty())
            assertTrue(lesson.funFact.isNotBlank())
        }
    }
}
