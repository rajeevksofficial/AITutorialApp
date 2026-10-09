package com.aitutorial.app

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.aitutorial.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: AiTopicAdapter

    private var selectedCategory: String = "All"
    private var isBookmarkFilterActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSearch()
        setupCategoryChips()
        setupFeaturedCard()
        setupBookmarkFilter()
        updateTopicsList()
    }

    override fun onResume() {
        super.onResume()
        updateTopicsList()
    }

    private fun setupRecyclerView() {
        adapter = AiTopicAdapter(
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
            adapter = this@MainActivity.adapter
        }
    }

    private fun setupSearch() {
        binding.etSearchTopics.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateTopicsList()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupCategoryChips() {
        val chipMap = mapOf(
            binding.chipAll to "All",
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

    private fun setupFeaturedCard() {
        val featuredTopic = AiTopicRepository.getTopicById("rag") ?: AiTopicRepository.getAllTopics().firstOrNull()
        if (featuredTopic != null) {
            binding.tvHeroTitle.text = featuredTopic.title
            binding.tvHeroSummary.text = featuredTopic.shortSummary
            binding.btnHeroExplore.setOnClickListener {
                val intent = Intent(this, TopicDetailActivity::class.java).apply {
                    putExtra(TopicDetailActivity.EXTRA_TOPIC_ID, featuredTopic.id)
                }
                startActivity(intent)
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

    private fun updateTopicsList() {
        val query = binding.etSearchTopics.text.toString().trim()
        var filtered = AiTopicRepository.filterTopics(selectedCategory, query)

        if (isBookmarkFilterActive) {
            filtered = filtered.filter { it.isBookmarked }
        }

        adapter.updateList(filtered)

        // Empty state vs RecyclerView
        if (filtered.isEmpty()) {
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.rvTopics.visibility = View.GONE
        } else {
            binding.layoutEmptyState.visibility = View.GONE
            binding.rvTopics.visibility = View.VISIBLE
        }

        binding.tvSectionHeader.text = if (isBookmarkFilterActive) {
            "Bookmarked Topics (${filtered.size})"
        } else {
            "Trending AI Topics (${filtered.size})"
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
