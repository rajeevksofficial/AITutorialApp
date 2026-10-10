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
    fun testDateParsing() {
        val parsedTime = AiFeedService.parseDate("Fri, 09 Oct 2026 12:00:00 GMT")
        assertTrue(parsedTime > 0)
    }

    @Test
    fun testWikiSlugInference() {
        val slug = AiFeedService.inferWikiSlug("Fine-tuning Large Language Models", "Generative AI")
        assertEquals("Large_language_model", slug)

        val agentSlug = AiFeedService.inferWikiSlug("Autonomous Agentic Decision Loops", "AI Agents")
        assertEquals("Intelligent_agent", agentSlug)
    }

    @Test
    fun testCategoryInference() {
        val agentCategory = AiFeedService.inferCategory("Autonomous Agent Swarms with Tool Calling", "Multi-agent coordination")
        assertEquals("AI Agents", agentCategory)

        val genAiCategory = AiFeedService.inferCategory("Prompt Engineering with Large Language Models", "Token generation")
        assertEquals("Generative AI", genAiCategory)
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

    @Test
    fun testFallbackTrendingTopicsCoverAllSources() {
        val fallbacks = AiFeedService.getFallbackTrendingTopics()
        assertTrue("Should have fallback topics", fallbacks.isNotEmpty())

        val sources = fallbacks.map { it.feedTag }
        assertTrue(sources.any { it.contains("Hugging Face", ignoreCase = true) })
        assertTrue(sources.any { it.contains("Papers With Code", ignoreCase = true) })
        assertTrue(sources.any { it.contains("GitHub Trending", ignoreCase = true) })
        assertTrue(sources.any { it.contains("daily.dev", ignoreCase = true) })
    }

    @Test
    fun testSourceFeedFiltering() {
        val fallbacks = AiFeedService.getFallbackTrendingTopics()
        AiTopicRepository.addOrUpdateLiveTopics(fallbacks)

        val hfTopics = AiTopicRepository.filterTopics("Hugging Face", null)
        assertTrue(hfTopics.isNotEmpty())
        assertTrue(hfTopics.all { it.feedTag.contains("Hugging Face", ignoreCase = true) || it.sourceName.contains("Hugging Face", ignoreCase = true) })

        val pwcTopics = AiTopicRepository.filterTopics("Papers With Code", null)
        assertTrue(pwcTopics.isNotEmpty())
        assertTrue(pwcTopics.all { it.feedTag.contains("Papers With Code", ignoreCase = true) || it.sourceName.contains("Papers With Code", ignoreCase = true) })

        val ghTopics = AiTopicRepository.filterTopics("GitHub Trending", null)
        assertTrue(ghTopics.isNotEmpty())
        assertTrue(ghTopics.all { it.feedTag.contains("GitHub", ignoreCase = true) || it.sourceName.contains("GitHub", ignoreCase = true) })

        val dailyTopics = AiTopicRepository.filterTopics("daily.dev", null)
        assertTrue(dailyTopics.isNotEmpty())
        assertTrue(dailyTopics.all { it.feedTag.contains("daily.dev", ignoreCase = true) || it.sourceName.contains("daily.dev", ignoreCase = true) })

        val wikiTopics = AiTopicRepository.filterTopics("Wikipedia", null)
        assertTrue(wikiTopics.isNotEmpty())
        assertTrue(wikiTopics.all { it.feedTag.contains("Wikipedia", ignoreCase = true) || it.sourceName.contains("Wikipedia", ignoreCase = true) || !it.isLiveFeed })
    }
}
