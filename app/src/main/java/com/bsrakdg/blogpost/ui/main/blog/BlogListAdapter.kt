package com.bsrakdg.blogpost.ui.main.blog

import android.view.LayoutInflater.from
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncDifferConfig.Builder
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView
import com.bsrakdg.blogpost.R
import com.bsrakdg.blogpost.models.BlogPost
import com.bsrakdg.blogpost.utils.DateConvertUtils
import com.bsrakdg.blogpost.utils.GenericViewHolder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import kotlinx.android.synthetic.main.layout_blog_list_item.view.*

class BlogListAdapter(
    private val interaction: Interaction? = null,
    private val requestManager: RequestManager
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val NO_MORE_RESULTS = -1
    private val BLOG_ITEM = 0
    private val NO_MORE_RESULTS_BLOG_MARKER = BlogPost(
        pk = NO_MORE_RESULTS,
        title = "",
        slug = "",
        body = "",
        image = "",
        date_updated = 0,
        username = ""
    )

    private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<BlogPost>() {

        override fun areItemsTheSame(oldItem: BlogPost, newItem: BlogPost): Boolean {
            return oldItem.pk == newItem.pk
        }

        override fun areContentsTheSame(oldItem: BlogPost, newItem: BlogPost): Boolean {
            return oldItem == newItem
        }

    }

    private val differ = AsyncListDiffer(
        BlogRecyclerChangeCallback(adapter = this),
        Builder(DIFF_CALLBACK).build()
    )

    internal inner class BlogRecyclerChangeCallback(
        private val adapter: BlogListAdapter
    ) : ListUpdateCallback {

        override fun onChanged(position: Int, count: Int, payload: Any?) {
            adapter.notifyItemRangeChanged(position, count, payload)
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            adapter.notifyDataSetChanged()
        }

        override fun onInserted(position: Int, count: Int) {
            adapter.notifyItemRangeChanged(position, count)
        }

        override fun onRemoved(position: Int, count: Int) {
            adapter.notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            NO_MORE_RESULTS -> {
                return GenericViewHolder(
                    from(parent.context).inflate(
                        R.layout.layout_no_more_results,
                        parent,
                        false
                    )
                )
            }

            BLOG_ITEM -> {
                return BlogViewHolder(
                    from(parent.context).inflate(
                        R.layout.layout_blog_list_item,
                        parent,
                        false
                    ),
                    interaction = interaction,
                    requestManager = requestManager
                )
            }

            else -> {
                return BlogViewHolder(
                    from(parent.context).inflate(
                        R.layout.layout_blog_list_item,
                        parent,
                        false
                    ),
                    interaction = interaction,
                    requestManager = requestManager
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is BlogViewHolder -> {
                holder.bind(differ.currentList[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun getItemViewType(position: Int): Int {
        if (differ.currentList[position].pk > -1) {
            return BLOG_ITEM
        }
        return differ.currentList[position].pk // NO MORE RESULTS
    }

    fun submitList(list: List<BlogPost>?, isQueryExhausted: Boolean) {
        val newList = list?.toMutableList()
        if (isQueryExhausted) {
            newList?.add(NO_MORE_RESULTS_BLOG_MARKER)
        }
        differ.submitList(list)
    }

    class BlogViewHolder
    constructor(
        itemView: View,
        private val interaction: Interaction?,
        private val requestManager: RequestManager
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: BlogPost) = with(itemView) {
            itemView.setOnClickListener {
                interaction?.onItemSelected(layoutPosition, item)
            }

            requestManager
                .load(item.image)
                .transition(withCrossFade())
                .into(itemView.blog_image)

            itemView.blog_title.text = item.title
            itemView.blog_author.text = item.username
            itemView.blog_update_date.text =
                DateConvertUtils.convertLongToStringDate(item.date_updated)
        }
    }

    interface Interaction {
        fun onItemSelected(position: Int, item: BlogPost)
    }
}

