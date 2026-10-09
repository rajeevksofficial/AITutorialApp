package com.aitutorial.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AiTopicRepositoryTest {

    @Test
    fun testTopicsLoaded() {
        val topics = AiTopicRepository.getAllTopics()
        assertTrue("Should have at least 10 trending AI topics", topics.size >= 10)
    }

    @Test
    fun testFilteringByCategory() {
        val genAiTopics = AiTopicRepository.filterTopics("Generative AI", null)
        assertTrue(genAiTopics.isNotEmpty())
        for (topic in genAiTopics) {
            assertEquals("Generative AI", topic.category)
        }
    }

    @Test
    fun testSearchFiltering() {
        val ragTopics = AiTopicRepository.filterTopics(null, "RAG")
        assertTrue(ragTopics.isNotEmpty())
        assertTrue(ragTopics.any { it.title.contains("RAG") })
    }

    @Test
    fun testBookmarkAndProgress() {
        val initialProgress = AiTopicRepository.getProgress()
        assertEquals(0, initialProgress.first)

        val firstTopic = AiTopicRepository.getAllTopics().first()
        AiTopicRepository.toggleCompleted(firstTopic.id)

        val updatedProgress = AiTopicRepository.getProgress()
        assertEquals(1, updatedProgress.first)
    }
}
