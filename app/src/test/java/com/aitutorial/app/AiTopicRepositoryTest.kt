package com.aitutorial.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AiTopicRepositoryTest {

    @Test
    fun testTopicsLoaded() {
        val topics = AiTopicRepository.getAllTopics()
        assertTrue("Should have at least 10 foundation AI topics", topics.size >= 10)
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

    @Test
    fun testFeedParsingAndConversion() {
        val sampleEndpoint = AiFeedService.FeedEndpoint(
            url = "https://rss.arxiv.org/rss/cs.AI",
            sourceName = "ArXiv AI Research",
            feedTag = "ArXiv AI"
        )

        val rawTitle = "Hierarchical Multi-Agent Reinforcement Learning for Autonomous Drone Swarms"
        val rawDesc = "Abstract: We propose an autonomous agentic framework combining LLM reasoning with tool calling and decentralized planning to coordinate drone fleets in complex environments."
        val rawLink = "https://arxiv.org/abs/2610.99999"
        val rawDate = "Fri, 09 Oct 2026 12:00:00 GMT"

        val topic = AiFeedService.convertToAiTopic(rawTitle, rawDesc, rawLink, rawDate, sampleEndpoint)

        assertNotNull(topic)
        topic?.let {
            assertEquals("Hierarchical Multi-Agent Reinforcement Learning for Autonomous Drone Swarms", it.title)
            assertEquals("AI Agents", it.category)
            assertEquals("Advanced", it.difficulty)
            assertEquals("ArXiv AI Research", it.sourceName)
            assertEquals(true, it.isLiveFeed)
            assertTrue(it.coreConcepts.isNotEmpty())
            assertTrue(it.tutorialContent.contains("LIVE AI TRENDING BRIEFING"))
        }
    }

    @Test
    fun testHtmlCleaning() {
        val dirty = "<b>New Breakthrough</b> &amp; &quot;State-of-the-art&quot; Model&#39;s Launch"
        val cleaned = AiFeedService.cleanHtml(dirty)
        assertEquals("New Breakthrough & \"State-of-the-art\" Model's Launch", cleaned)
    }

    @Test
    fun testTrendingSpotlight() {
        val spotlight = AiTopicRepository.getTrendingSpotlight()
        assertNotNull(spotlight)
    }
}
