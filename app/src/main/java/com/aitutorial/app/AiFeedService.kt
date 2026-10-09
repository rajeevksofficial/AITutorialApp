package com.aitutorial.app

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executors
import kotlin.math.abs

object AiFeedService {

    data class FeedEndpoint(
        val url: String,
        val sourceName: String,
        val feedTag: String
    )

    val endpoints = listOf(
        FeedEndpoint(
            url = "https://rss.arxiv.org/rss/cs.AI",
            sourceName = "ArXiv AI Research",
            feedTag = "ArXiv AI"
        ),
        FeedEndpoint(
            url = "https://techcrunch.com/category/artificial-intelligence/feed/",
            sourceName = "TechCrunch AI",
            feedTag = "TechCrunch"
        ),
        FeedEndpoint(
            url = "https://huggingface.co/blog/feed.xml",
            sourceName = "Hugging Face Blog",
            feedTag = "Hugging Face"
        ),
        FeedEndpoint(
            url = "https://blog.google/technology/ai/rss/",
            sourceName = "Google AI Technology",
            feedTag = "Google AI"
        ),
        FeedEndpoint(
            url = "https://hnrss.org/newest?q=AI+OR+LLM+OR+GPT&points=10",
            sourceName = "Hacker News AI",
            feedTag = "Hacker News"
        )
    )

    private val dateFormats = arrayOf(
        SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US),
        SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US),
        SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.US),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US)
    )

    private val executor = Executors.newFixedThreadPool(4)

    /**
     * Fetches all live feeds concurrently and converts them into AiTopic models
     */
    fun fetchTrendingFeeds(onComplete: (List<AiTopic>, Boolean) -> Unit) {
        executor.execute {
            val allTopics = mutableListOf<AiTopic>()
            var anySuccess = false

            for (endpoint in endpoints) {
                try {
                    val topics = fetchSingleFeed(endpoint)
                    if (topics.isNotEmpty()) {
                        allTopics.addAll(topics)
                        anySuccess = true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Sort by newest publication date first
            allTopics.sortByDescending { it.publishedTime }

            onComplete(allTopics, anySuccess)
        }
    }

    fun fetchSingleFeed(endpoint: FeedEndpoint): List<AiTopic> {
        var connection: HttpURLConnection? = null
        var inputStream: InputStream? = null
        return try {
            val url = URL(endpoint.url)
            connection = url.openConnection() as HttpURLConnection
            connection.apply {
                connectTimeout = 6000
                readTimeout = 6000
                requestMethod = "GET"
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14) AITutorialApp/1.0")
                setRequestProperty("Accept", "application/rss+xml, application/atom+xml, application/xml, text/xml, */*")
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inputStream = connection.inputStream
                parseFeed(inputStream, endpoint)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        } finally {
            try {
                inputStream?.close()
                connection?.disconnect()
            } catch (_: Exception) {}
        }
    }

    fun parseFeed(inputStream: InputStream, endpoint: FeedEndpoint): List<AiTopic> {
        val topics = mutableListOf<AiTopic>()
        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = false
            val parser = factory.newPullParser()
            parser.setInput(inputStream, "UTF-8")

            var eventType = parser.eventType
            var inItem = false
            var title = ""
            var description = ""
            var link = ""
            var pubDateStr = ""

            while (eventType != XmlPullParser.END_DOCUMENT) {
                val tagName = parser.name?.lowercase(Locale.ROOT)

                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (tagName == "item" || tagName == "entry") {
                            inItem = true
                            title = ""
                            description = ""
                            link = ""
                            pubDateStr = ""
                        } else if (inItem) {
                            when (tagName) {
                                "title" -> title = parser.nextText().trim()
                                "description", "summary", "content" -> {
                                    val text = parser.nextText().trim()
                                    if (description.isBlank() || text.length > description.length) {
                                        description = text
                                    }
                                }
                                "link" -> {
                                    val href = parser.getAttributeValue(null, "href")
                                    link = if (!href.isNullOrBlank()) href else parser.nextText().trim()
                                }
                                "pubdate", "published", "updated" -> {
                                    if (pubDateStr.isBlank()) {
                                        pubDateStr = parser.nextText().trim()
                                    }
                                }
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (tagName == "item" || tagName == "entry") {
                            if (title.isNotBlank()) {
                                val topic = convertToAiTopic(title, description, link, pubDateStr, endpoint)
                                if (topic != null) {
                                    topics.add(topic)
                                }
                            }
                            inItem = false
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return topics
    }

    fun convertToAiTopic(
        rawTitle: String,
        rawDescription: String,
        rawLink: String,
        rawPubDate: String,
        endpoint: FeedEndpoint
    ): AiTopic? {
        val cleanTitle = cleanHtml(rawTitle)
        if (cleanTitle.isBlank()) return null

        var cleanDesc = cleanHtml(rawDescription)

        // ArXiv description cleanup: "arXiv:xxxx Abstract: ..."
        if (cleanDesc.contains("Abstract:", ignoreCase = true)) {
            val idx = cleanDesc.indexOf("Abstract:", ignoreCase = true)
            cleanDesc = cleanDesc.substring(idx + "Abstract:".length).trim()
        }

        // Clean link
        val cleanLink = rawLink.trim().ifBlank { "https://arxiv.org/abs/" }

        val pubTime = parseDate(rawPubDate)
        val category = inferCategory(cleanTitle, cleanDesc)
        val difficulty = inferDifficulty(endpoint.sourceName, cleanTitle)
        val wikiSlug = inferWikiSlug(cleanTitle, category)

        val shortSummary = if (cleanDesc.length > 220) {
            val truncated = cleanDesc.take(220)
            val lastSpace = truncated.lastIndexOf(' ')
            if (lastSpace > 100) "${truncated.substring(0, lastSpace)}..." else "$truncated..."
        } else if (cleanDesc.isNotBlank()) {
            cleanDesc
        } else {
            "Latest trending AI advancement reported live from ${endpoint.sourceName}."
        }

        // Read time estimation based on word count
        val wordCount = cleanDesc.split("\\s+".toRegex()).size
        val readTimeMinutes = (wordCount / 120).coerceIn(3, 7)
        val readTime = "$readTimeMinutes min"

        // Generate Core Concepts from sentences
        val sentences = cleanDesc.split(Regex("[.!?]\\s+"))
            .map { it.trim() }
            .filter { it.length in 25..160 }

        val coreConcepts = mutableListOf<String>()
        coreConcepts.add("Breakthrough Focus: $cleanTitle")
        if (sentences.isNotEmpty()) {
            coreConcepts.add("Methodology: ${sentences[0]}")
        }
        if (sentences.size > 1) {
            coreConcepts.add("Key Finding: ${sentences[1]}")
        } else {
            coreConcepts.add("Practical Impact: Accelerates $category adoption and engineering.")
        }
        coreConcepts.add("Verified Source: ${endpoint.sourceName} (Live AI Feed)")
        coreConcepts.add("Direct Paper / Article: $cleanLink")

        val tutorialContent = """
            LIVE AI TRENDING BRIEFING: $cleanTitle
            -------------------------------------------------------------
            Source Feed: ${endpoint.sourceName}
            Target Domain: $category
            Estimated Study Time: $readTime

            EXECUTIVE SUMMARY
            -------------------------------------------------------------
            ${cleanDesc.ifBlank { "This breakthrough publication covers emerging techniques in $category, advancing the state-of-the-art across contemporary artificial intelligence systems." }}

            WHY THIS ADVANCEMENT MATTERS
            -------------------------------------------------------------
            Artificial intelligence is advancing at unprecedented velocity. Trending feeds from ${endpoint.sourceName} spotlight high-impact research, open-source model releases, and architectural innovations.
            Studying this topic deepens understanding of $category foundations, providing actionable context for LLM reasoning, multi-agent frameworks, and multimodal architectures.

            CORE TAKEAWAYS FOR AI PRACTITIONERS
            -------------------------------------------------------------
            1. Domain Alignment: $category
            2. Technical Difficulty: $difficulty Level
            3. Publication Timestamp: ${if (rawPubDate.isNotBlank()) rawPubDate else "Latest Live Stream"}
            4. Original Publication Resource: $cleanLink

            HOW TO APPLY THIS KNOWLEDGE
            -------------------------------------------------------------
            Review the core concepts listed in this lesson, cross-reference with Wikipedia background knowledge below, and click 'Open Original Resource' to study the primary source paper or publication.
        """.trimIndent()

        val uniqueId = "feed_" + abs(cleanLink.hashCode() + cleanTitle.hashCode())

        return AiTopic(
            id = uniqueId,
            title = cleanTitle,
            wikiSlug = wikiSlug,
            category = category,
            difficulty = difficulty,
            readTime = readTime,
            shortSummary = shortSummary,
            coreConcepts = coreConcepts,
            tutorialContent = tutorialContent,
            webUrl = cleanLink,
            sourceName = endpoint.sourceName,
            isLiveFeed = true,
            feedTag = endpoint.feedTag,
            publishedTime = pubTime
        )
    }

    fun inferCategory(title: String, desc: String): String {
        val text = "$title $desc".lowercase(Locale.ROOT)
        return when {
            text.contains("agent") || text.contains("autonomous") || text.contains("reasoning") ||
            text.contains("tool calling") || text.contains("mcp") || text.contains("workflow") ||
            text.contains("simulation") -> "AI Agents"

            text.contains("diffusion") || text.contains("vision") || text.contains("image") ||
            text.contains("video") || text.contains("multimodal") || text.contains("vlm") ||
            text.contains("audio") || text.contains("speech") || text.contains("flux") -> "Vision & Multimodal"

            text.contains("safety") || text.contains("alignment") || text.contains("rlhf") ||
            text.contains("dpo") || text.contains("security") || text.contains("ethics") ||
            text.contains("red team") || text.contains("jailbreak") || text.contains("bias") -> "Safety & Alignment"

            text.contains("neural") || text.contains("transformer") || text.contains("deep learning") ||
            text.contains("backprop") || text.contains("layer") || text.contains("loss") ||
            text.contains("weights") || text.contains("gradient") || text.contains("attention") ||
            text.contains("gpu") || text.contains("cluster") -> "Deep Learning"

            else -> "Generative AI"
        }
    }

    fun inferDifficulty(sourceName: String, title: String): String {
        val t = title.lowercase(Locale.ROOT)
        return when {
            sourceName.contains("ArXiv", ignoreCase = true) -> "Advanced"
            sourceName.contains("Hugging Face", ignoreCase = true) -> "Intermediate"
            t.contains("architecture") || t.contains("framework") || t.contains("theoretical") -> "Advanced"
            t.contains("guide") || t.contains("intro") || t.contains("overview") || t.contains("beginner") -> "Beginner"
            else -> "Intermediate"
        }
    }

    fun inferWikiSlug(title: String, category: String): String {
        val t = title.lowercase(Locale.ROOT)
        return when {
            t.contains("rag") || t.contains("retrieval") -> "Retrieval-augmented_generation"
            t.contains("agent") -> "Intelligent_agent"
            t.contains("transformer") || t.contains("attention") -> "Transformer_(deep_learning_architecture)"
            t.contains("diffusion") -> "Diffusion_model"
            t.contains("multimodal") || t.contains("vision") -> "Multimodal_learning"
            t.contains("safety") || t.contains("alignment") || t.contains("rlhf") -> "AI_safety"
            t.contains("llm") || t.contains("language model") || t.contains("gpt") -> "Large_language_model"
            t.contains("prompt") -> "Prompt_engineering"
            t.contains("vector") -> "Vector_database"
            category == "Deep Learning" -> "Deep_learning"
            category == "AI Agents" -> "Intelligent_agent"
            category == "Vision & Multimodal" -> "Multimodal_learning"
            category == "Safety & Alignment" -> "AI_safety"
            else -> "Artificial_intelligence"
        }
    }

    fun cleanHtml(raw: String): String {
        if (raw.isBlank()) return ""
        var cleaned = raw.replace(Regex("<[^>]*>"), " ")
        cleaned = cleaned.replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&#39;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&#8217;", "'")
            .replace("&#8216;", "'")
            .replace("&#8220;", "\"")
            .replace("&#8221;", "\"")
            .replace("&#8212;", "—")
            .replace(Regex("\\s+"), " ")
            .trim()
        return cleaned
    }

    fun parseDate(dateStr: String): Long {
        if (dateStr.isBlank()) return System.currentTimeMillis()
        for (format in dateFormats) {
            try {
                val parsed = format.parse(dateStr.trim())
                if (parsed != null) return parsed.time
            } catch (_: Exception) {}
        }
        return System.currentTimeMillis()
    }
}
