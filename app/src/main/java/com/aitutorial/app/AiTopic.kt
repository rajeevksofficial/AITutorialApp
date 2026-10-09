package com.aitutorial.app

data class AiTopic(
    val id: String,
    val title: String,
    val wikiSlug: String,
    val category: String,
    val difficulty: String,
    val readTime: String,
    val shortSummary: String,
    val coreConcepts: List<String>,
    val tutorialContent: String,
    val webUrl: String,
    var liveWikiExtract: String? = null,
    var isBookmarked: Boolean = false,
    var isCompleted: Boolean = false
)
