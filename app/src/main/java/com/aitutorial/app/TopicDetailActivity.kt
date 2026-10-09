package com.aitutorial.app

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.aitutorial.app.databinding.ActivityTopicDetailBinding

class TopicDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTopicDetailBinding
    private var currentTopic: AiTopic? = null

    companion object {
        const val EXTRA_TOPIC_ID = "extra_topic_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTopicDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val topicId = intent.getStringExtra(EXTRA_TOPIC_ID) ?: ""
        currentTopic = AiTopicRepository.getTopicById(topicId)

        if (currentTopic == null) {
            Toast.makeText(this, "Topic not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupViews()
        loadWikipediaContent()
    }

    private fun setupViews() {
        val topic = currentTopic ?: return

        binding.tvDetailTitle.text = topic.title
        binding.tvDetailCategory.text = topic.category
        binding.tvDetailDifficulty.text = topic.difficulty
        binding.tvDetailReadTime.text = "${topic.readTime} read"
        binding.tvDetailShortSummary.text = topic.shortSummary
        binding.tvTutorialContent.text = topic.tutorialContent

        // Difficulty Color
        when (topic.difficulty.lowercase()) {
            "beginner" -> binding.tvDetailDifficulty.setTextColor(Color.parseColor("#10B981"))
            "intermediate" -> binding.tvDetailDifficulty.setTextColor(Color.parseColor("#F59E0B"))
            "advanced" -> binding.tvDetailDifficulty.setTextColor(Color.parseColor("#EF4444"))
        }

        // Live Feed Banner
        if (topic.isLiveFeed) {
            binding.tvDetailLiveBanner.visibility = View.VISIBLE
            binding.tvDetailLiveBanner.text = "● LIVE FEED: ${topic.sourceName.uppercase()}"
            binding.btnOpenFullWeb.text = "Open Original Paper / Publication →"
        } else {
            binding.tvDetailLiveBanner.visibility = View.GONE
            binding.btnOpenFullWeb.text = getString(R.string.btn_open_web)
        }

        updateActionIcons()

        // Populate Core Concepts List
        binding.layoutCoreConceptsList.removeAllViews()
        for (concept in topic.coreConcepts) {
            val itemRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 6, 0, 8)
            }

            val icon = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(40, 40).apply {
                    setMargins(0, 4, 16, 0)
                }
                setImageResource(R.drawable.ic_check_circle)
            }

            val text = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                this.text = concept
                setTextColor(ContextCompat.getColor(this@TopicDetailActivity, R.color.text_primary))
                textSize = 13.5f
                setLineSpacing(4f, 1f)
            }

            itemRow.addView(icon)
            itemRow.addView(text)
            binding.layoutCoreConceptsList.addView(itemRow)
        }

        // Listeners
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnDetailBookmark.setOnClickListener {
            AiTopicRepository.toggleBookmark(topic.id)
            updateActionIcons()
            val msg = if (topic.isBookmarked) "Added to study bookmarks" else "Removed from bookmarks"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        binding.btnDetailComplete.setOnClickListener {
            AiTopicRepository.toggleCompleted(topic.id)
            updateActionIcons()
            val msg = if (topic.isCompleted) "Marked as completed!" else "Marked as incomplete"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        binding.btnOpenFullWeb.setOnClickListener {
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(topic.webUrl))
                startActivity(browserIntent)
            } catch (_: Exception) {
                Toast.makeText(this, "Could not open browser", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateActionIcons() {
        val topic = currentTopic ?: return

        // Bookmark
        if (topic.isBookmarked) {
            binding.btnDetailBookmark.setImageResource(R.drawable.ic_bookmark_filled)
        } else {
            binding.btnDetailBookmark.setImageResource(R.drawable.ic_bookmark)
        }

        // Complete
        if (topic.isCompleted) {
            binding.btnDetailComplete.setColorFilter(Color.parseColor("#10B981"))
        } else {
            binding.btnDetailComplete.setColorFilter(Color.parseColor("#94A3B8"))
        }
    }

    private fun loadWikipediaContent() {
        val topic = currentTopic ?: return

        if (topic.liveWikiExtract != null) {
            binding.tvWikiExtract.text = topic.liveWikiExtract
            binding.progressWikiLoading.visibility = View.GONE
            return
        }

        binding.progressWikiLoading.visibility = View.VISIBLE
        binding.tvWikiExtract.text = "Fetching latest Wikipedia documentation..."

        AiTopicRepository.fetchWikipediaSummary(topic.wikiSlug) { extract ->
            runOnUiThread {
                binding.progressWikiLoading.visibility = View.GONE
                if (!extract.isNullOrBlank()) {
                    binding.tvWikiExtract.text = extract
                } else {
                    // Graceful fallback to short summary
                    binding.tvWikiExtract.text = topic.shortSummary + "\n\n(Live web fetch offline — using verified reference summary)"
                }
            }
        }
    }
}
