package com.ryanschoen.radius.ui.venues

import android.view.LayoutInflater
import android.view.ViewGroup
import android.content.Context
import android.widget.CheckBox
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.ryanschoen.radius.databinding.ListItemVenueBinding
import com.ryanschoen.radius.domain.Venue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import timber.log.Timber

private val ITEM_VIEW_TYPE_VENUE = 1

private val adapterScope = CoroutineScope(Dispatchers.Default)

class VenueAdapter(private val venues: List<Venue>, private val onClickListener: OnClickListener, private val onCheckListener: OnCheckListener) : ListAdapter<Venue, RecyclerView.ViewHolder>(VenueDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_VENUE -> ViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType ${viewType}")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is ViewHolder -> {
                holder.bind(getItem(position), onClickListener, onCheckListener)
            }
        }
    }


    class ViewHolder private constructor (val binding: ListItemVenueBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Venue, onClickListener: OnClickListener, onCheckListener: OnCheckListener) {
            binding.venue = item

            //var requestOptions = RequestOptions()
           // requestOptions = requestOptions.transform(RoundedCorners(16))
            Glide.with(this.itemView.context)
                .load(item.imageUrl)
                //.apply(requestOptions)
                .skipMemoryCache(true) //for caching the image url in case phone is offline
                .centerCrop()
                .into(binding.venuePhoto)

            binding.visitedCheckbox.setOnClickListener {
                if(it is CheckBox) {
                    onCheckListener.onCheck(item, it.isChecked)
                }
            }

            // TODO: add click listener to rest of everything??


            if(item.id == "9yM20-7fj4LMrOO30nqkBw") {
                Timber.i("Found our venue. We're about to execute bindings with visited = ${item.visited}")
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
    class OnCheckListener(val checkListener: (venue: Venue, checked: Boolean) -> Unit) {
        fun onCheck(venue: Venue, checked: Boolean) = checkListener(venue, checked)
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