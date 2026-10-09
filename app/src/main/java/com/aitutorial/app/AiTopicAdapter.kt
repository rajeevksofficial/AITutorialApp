package com.aitutorial.app

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.aitutorial.app.databinding.ItemAiTopicBinding

class AiTopicAdapter(
    private var topicList: List<AiTopic>,
    private val onBookmarkToggle: (AiTopic) -> Unit,
    private val onItemClick: (AiTopic) -> Unit
) : RecyclerView.Adapter<AiTopicAdapter.TopicViewHolder>() {

    fun updateList(newList: List<AiTopic>) {
        topicList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicViewHolder {
        val binding = ItemAiTopicBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TopicViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TopicViewHolder, position: Int) {
        holder.bind(topicList[position])
    }

    override fun getItemCount(): Int = topicList.size

    inner class TopicViewHolder(private val binding: ItemAiTopicBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(topic: AiTopic) {
            val context = binding.root.context

            binding.tvTopicTitle.text = topic.title
            binding.tvTopicSummary.text = topic.shortSummary
            binding.tvTopicCategory.text = topic.category
            binding.tvTopicDifficulty.text = topic.difficulty
            binding.tvTopicReadTime.text = "${topic.readTime} read"

            // Difficulty Color
            when (topic.difficulty.lowercase()) {
                "beginner" -> binding.tvTopicDifficulty.setTextColor(Color.parseColor("#10B981"))
                "intermediate" -> binding.tvTopicDifficulty.setTextColor(Color.parseColor("#F59E0B"))
                "advanced" -> binding.tvTopicDifficulty.setTextColor(Color.parseColor("#EF4444"))
                else -> binding.tvTopicDifficulty.setTextColor(Color.parseColor("#94A3B8"))
            }

            // Completed Status Icon
            binding.ivCompletedStatus.visibility = if (topic.isCompleted) View.VISIBLE else View.GONE

            // Live Feed Indicator
            if (topic.isLiveFeed) {
                binding.tvLiveBadge.visibility = View.VISIBLE
                binding.tvLiveBadge.text = "● LIVE ${topic.feedTag.uppercase()}"
                binding.tvTopicSource.text = topic.sourceName
                binding.ivTopicSourceIcon.setImageResource(R.drawable.ic_sparkles)

                val sourceColor = when {
                    topic.sourceName.contains("ArXiv") -> Color.parseColor("#F43F5E")
                    topic.sourceName.contains("TechCrunch") -> Color.parseColor("#10B981")
                    topic.sourceName.contains("Hugging Face") -> Color.parseColor("#F59E0B")
                    topic.sourceName.contains("Google") -> Color.parseColor("#3B82F6")
                    topic.sourceName.contains("Hacker News") -> Color.parseColor("#FB923C")
                    else -> Color.parseColor("#06B6D4")
                }
                binding.tvTopicSource.setTextColor(sourceColor)
            } else {
                binding.tvLiveBadge.visibility = View.GONE
                binding.tvTopicSource.text = topic.sourceName
                binding.tvTopicSource.setTextColor(Color.parseColor("#818CF8"))
                binding.ivTopicSourceIcon.setImageResource(R.drawable.ic_web)
            }

            // Bookmark Status Icon
            if (topic.isBookmarked) {
                binding.btnBookmark.setImageResource(R.drawable.ic_bookmark_filled)
            } else {
                binding.btnBookmark.setImageResource(R.drawable.ic_bookmark)
            }

            binding.btnBookmark.setOnClickListener {
                onBookmarkToggle(topic)
                bind(topic)
            }

            binding.root.setOnClickListener {
                onItemClick(topic)
            }
        }
    }
}
