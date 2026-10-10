package com.aitutorial.app

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aitutorial.app.databinding.ItemHeroHeaderBinding

class HeroHeaderAdapter(
    private var spotlightTopic: AiTopic?,
    private var sectionTitle: String,
    private val onExploreClick: (AiTopic) -> Unit
) : RecyclerView.Adapter<HeroHeaderAdapter.HeaderViewHolder>() {

    fun update(newSpotlight: AiTopic?, newTitle: String) {
        val changed = spotlightTopic != newSpotlight || sectionTitle != newTitle
        if (changed) {
            spotlightTopic = newSpotlight
            sectionTitle = newTitle
            notifyItemChanged(0)
        }
    }

    override fun getItemCount(): Int = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        val binding = ItemHeroHeaderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HeaderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        holder.bind(spotlightTopic, sectionTitle)
    }

    inner class HeaderViewHolder(private val binding: ItemHeroHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(spotlight: AiTopic?, title: String) {
            binding.tvSectionHeader.text = title

            if (spotlight != null) {
                binding.cardHeroFeatured.visibility = View.VISIBLE
                binding.tvHeroTitle.text = spotlight.title
                binding.tvHeroSummary.text = spotlight.shortSummary

                if (spotlight.isLiveFeed) {
                    binding.tvHeroBadge.text = "🔥 LIVE TRENDING SPOTLIGHT"
                    binding.tvHeroBadge.setTextColor(Color.parseColor("#F43F5E"))
                    binding.tvHeroSource.visibility = View.VISIBLE
                    binding.tvHeroSource.text = "Source: ${spotlight.sourceName} • ${spotlight.category}"
                } else {
                    binding.tvHeroBadge.text = "TOP TRENDING SPOTLIGHT"
                    binding.tvHeroBadge.setTextColor(Color.parseColor("#FBBF24"))
                    binding.tvHeroSource.visibility = View.GONE
                }

                binding.btnHeroExplore.setOnClickListener {
                    onExploreClick(spotlight)
                }
            } else {
                binding.cardHeroFeatured.visibility = View.GONE
            }
        }
    }
}
