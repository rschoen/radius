package com.ryanschoen.radius.ui.venues

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ryanschoen.radius.databinding.ListItemVenueBinding
import com.ryanschoen.radius.domain.Venue
import java.util.*

private const val ITEM_VIEW_TYPE_VENUE = 1


class VenueAdapter(
    venues: List<Venue>,
    private val onClickListener: OnClickListener,
    private val onLongClickListener: OnLongClickListener,
    private val onCheckListener: OnCheckListener
) : ListAdapter<Venue, RecyclerView.ViewHolder>(VenueDiffCallback()), Filterable {

    var originalVenues: List<Venue> = venues
    var modelVenues: List<Venue> = listOf()
    private val filter = VenueFilter()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_VENUE -> ViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun getFilter(): Filter {
        return filter
    }

    fun filterAndSubmitList(list: List<Venue>) {
        originalVenues = list
        filter.filter("")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                holder.bind(
                    getItem(position),
                    onClickListener,
                    onLongClickListener,
                    onCheckListener
                )
            }
        }
    }


    class ViewHolder private constructor(private val binding: ListItemVenueBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: Venue,
            onClickListener: OnClickListener,
            onLongClickListener: OnLongClickListener,
            onCheckListener: OnCheckListener
        ) {
            binding.venue = item

            Glide.with(this.itemView.context)
                .load(item.imageUrl)
                .skipMemoryCache(true) //for caching the image url in case phone is offline
                .centerCrop()
                .into(binding.venuePhoto)

            binding.visitedCheckbox.setOnClickListener {
                if (it is CheckBox) {
                    onCheckListener.onCheck(item, it.isChecked)
                }
            }

            this.itemView.setOnClickListener {
                onClickListener.onClick(item)
            }
            this.itemView.setOnLongClickListener {
                onLongClickListener.onLongClick(item)
            }

            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemVenueBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return ITEM_VIEW_TYPE_VENUE
    }

    class OnClickListener(val clickListener: (venue: Venue) -> Unit) {
        fun onClick(venue: Venue) = clickListener(venue)
    }

    class OnLongClickListener(val longClickListener: (venue: Venue) -> Boolean) {
        fun onLongClick(venue: Venue) = longClickListener(venue)
    }

    class OnCheckListener(val checkListener: (venue: Venue, checked: Boolean) -> Unit) {
        fun onCheck(venue: Venue, checked: Boolean) = checkListener(venue, checked)
    }

    inner class VenueFilter : Filter() {

        private var showVisited = true
        private var showUnvisited = true
        private var showHidden = false

        fun setFilterAttributes(visited: Boolean, unvisited: Boolean, hidden: Boolean) {
            showVisited = visited
            showUnvisited = unvisited
            showHidden = hidden
        }

        override fun performFiltering(charSequence: CharSequence): FilterResults {
            val results = FilterResults()
            val list: List<Venue> = originalVenues
            val count = list.size
            val newList: MutableList<Venue> = ArrayList(count)

            for (i in 0 until count) {
                val a: Venue = list[i]
                val matchesVisited = (a.visited && showVisited) || (!a.visited && showUnvisited)
                if ((a.hidden && showHidden) || (!a.hidden && matchesVisited)) {
                    newList.add(a)
                }
            }
            results.values = newList
            results.count = newList.size
            return results
        }

        override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
            @Suppress("UNCHECKED_CAST")
            modelVenues = filterResults.values as List<Venue>
            submitList(modelVenues)
        }
    }
}

class VenueDiffCallback : DiffUtil.ItemCallback<Venue>() {
    override fun areItemsTheSame(oldItem: Venue, newItem: Venue): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Venue, newItem: Venue): Boolean {
        return (oldItem.id == newItem.id &&
                oldItem.name == newItem.name &&
                oldItem.reviews == newItem.reviews &&
                oldItem.rating == newItem.rating &&
                oldItem.imageUrl == newItem.imageUrl &&
                oldItem.hidden == newItem.hidden &&
                oldItem.visited == newItem.visited)
    }
}

