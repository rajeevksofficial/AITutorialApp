package com.aitutorial.app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.aitutorial.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var heroHeaderAdapter: HeroHeaderAdapter
    private lateinit var topicAdapter: AiTopicAdapter

    private var selectedCategory: String = "All"
    private var isBookmarkFilterActive = false

    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSearch()
        setupCategoryChips()
        setupBookmarkFilter()
        setupSyncButton()
        updateTopicsList()

        // Automatically fetch live AI feeds in background on launch
        syncTrendingFeeds(isUserInitiated = false)
    }

    override fun onResume() {
        super.onResume()
        updateTopicsList()
    }

    override fun onDestroy() {
        super.onDestroy()
        searchRunnable?.let { searchHandler.removeCallbacks(it) }
    }

    private fun setupRecyclerView() {
        heroHeaderAdapter = HeroHeaderAdapter(
            spotlightTopic = AiTopicRepository.getTrendingSpotlight(),
            sectionTitle = "Trending AI Topics",
            onExploreClick = { topic ->
                val intent = Intent(this, TopicDetailActivity::class.java).apply {
                    putExtra(TopicDetailActivity.EXTRA_TOPIC_ID, topic.id)
                }
                startActivity(intent)
            }
        )

        topicAdapter = AiTopicAdapter(
            topicList = emptyList(),
            onBookmarkToggle = { topic ->
                AiTopicRepository.toggleBookmark(topic.id)
                updateProgressUI()
            },
            onItemClick = { topic ->
                val intent = Intent(this, TopicDetailActivity::class.java).apply {
                    putExtra(TopicDetailActivity.EXTRA_TOPIC_ID, topic.id)
                }
                startActivity(intent)
            }
        )

        binding.rvTopics.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = ConcatAdapter(heroHeaderAdapter, topicAdapter)
            setHasFixedSize(true)
        }
    }

    private fun setupSearch() {
        binding.etSearchTopics.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Debounce search keystrokes to maintain smooth 60-120fps UI
                searchRunnable?.let { searchHandler.removeCallbacks(it) }
                searchRunnable = Runnable { updateTopicsList() }
                searchHandler.postDelayed(searchRunnable!!, 150)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupCategoryChips() {
        val chipMap = mapOf(
            binding.chipAll to "All",
            binding.chipLiveFeeds to "Live Feeds",
            binding.chipHuggingFace to "Hugging Face",
            binding.chipPapersWithCode to "Papers With Code",
            binding.chipGitHub to "GitHub Trending",
            binding.chipDailyDev to "daily.dev",
            binding.chipWikipedia to "Wikipedia",
            binding.chipGenAi to "Generative AI",
            binding.chipAgents to "AI Agents",
            binding.chipDeepLearning to "Deep Learning",
            binding.chipVision to "Vision & Multimodal",
            binding.chipSafety to "Safety & Alignment"
        )

        for ((chipView, category) in chipMap) {
            chipView.setOnClickListener {
                selectedCategory = category
                updateChipStyles(chipMap, chipView)
                updateTopicsList()
            }
        }
    }

    private fun updateChipStyles(chipMap: Map<TextView, String>, selectedChip: TextView) {
        val selectedBg = ContextCompat.getDrawable(this, R.drawable.bg_chip_selected)
        val unselectedBg = ContextCompat.getDrawable(this, R.drawable.bg_chip_unselected)
        val selectedColor = ContextCompat.getColor(this, R.color.white)
        val unselectedColor = ContextCompat.getColor(this, R.color.text_secondary)

        for ((chip, _) in chipMap) {
            if (chip == selectedChip) {
                chip.background = selectedBg
                chip.setTextColor(selectedColor)
            } else {
                chip.background = unselectedBg
                chip.setTextColor(unselectedColor)
            }
        }
    }

    private fun setupBookmarkFilter() {
        binding.btnFilterBookmarked.setOnClickListener {
            isBookmarkFilterActive = !isBookmarkFilterActive
            if (isBookmarkFilterActive) {
                binding.btnFilterBookmarked.setImageResource(R.drawable.ic_bookmark_filled)
            } else {
                binding.btnFilterBookmarked.setImageResource(R.drawable.ic_bookmark)
            }
            updateTopicsList()
        }
    }

    private fun setupSyncButton() {
        binding.btnRefreshFeeds.setOnClickListener {
            syncTrendingFeeds(isUserInitiated = true)
        }
    }

    private fun syncTrendingFeeds(isUserInitiated: Boolean) {
        if (isUserInitiated) {
            val rotate = RotateAnimation(
                0f, 360f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
            ).apply {
                duration = 600
                repeatCount = 1
            }
            binding.btnRefreshFeeds.startAnimation(rotate)
            Toast.makeText(this, "Fetching latest feeds from Hugging Face, Papers With Code, GitHub & daily.dev...", Toast.LENGTH_SHORT).show()
        }

        binding.tvSubtitle.text = "Syncing feeds from Hugging Face, Papers With Code, GitHub & daily.dev..."

        AiTopicRepository.fetchTrendingFeeds { success, count ->
            runOnUiThread {
                if (success && count > 0) {
                    binding.tvSubtitle.text = "● Live AI Feeds: Up to date ($count stories)"
                    updateTopicsList()
                    if (isUserInitiated) {
                        Toast.makeText(this@MainActivity, "Updated with $count live trending AI topics!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    binding.tvSubtitle.text = "Interactive Lessons & AI Documentation"
                    if (isUserInitiated) {
                        Toast.makeText(this@MainActivity, "Feeds already up to date", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun updateTopicsList() {
        val query = binding.etSearchTopics.text?.toString()?.trim() ?: ""
        var filtered = AiTopicRepository.filterTopics(selectedCategory, query)

        if (isBookmarkFilterActive) {
            filtered = filtered.filter { it.isBookmarked }
        }

        val spotlightTopic = if (query.isEmpty() && !isBookmarkFilterActive && selectedCategory == "All") {
            AiTopicRepository.getTrendingSpotlight()
        } else {
            null
        }

        val headerText = when {
            isBookmarkFilterActive -> "Bookmarked Topics (${filtered.size})"
            selectedCategory == "Live Feeds" -> "Latest AI Feeds (${filtered.size})"
            selectedCategory != "All" -> "$selectedCategory Topics (${filtered.size})"
            else -> "Trending AI Topics (${filtered.size})"
        }

        heroHeaderAdapter.update(spotlightTopic, headerText)
        topicAdapter.updateList(filtered)

        // Empty state vs RecyclerView
        if (filtered.isEmpty()) {
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.rvTopics.visibility = View.GONE
        } else {
            binding.layoutEmptyState.visibility = View.GONE
            binding.rvTopics.visibility = View.VISIBLE
        }

        updateProgressUI()
    }

    private fun updateProgressUI() {
        val (completed, total) = AiTopicRepository.getProgress()
        val percentage = if (total > 0) (completed * 100) / total else 0

        binding.tvProgressStats.text = "$completed/$total Studied • $percentage%"
        binding.progressMastery.progress = percentage
    }
}
