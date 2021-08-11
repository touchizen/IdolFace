package com.touchizen.idolface.ui.idolgallery

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.touchizen.idolface.R
import com.touchizen.idolface.model.IdolImage
import com.touchizen.idolface.utils.NavigationHelper
import com.touchizen.swipe.SwipeLayout

class IdolGalleryAdapter(fragment: IdolGalleryFragment):
    PagingDataAdapter<IdolImage, IdolGalleryViewHolder>(Companion),
    SwipeLayout.SwipeListener
{

    companion object : DiffUtil.ItemCallback<IdolImage>() {
        override fun areItemsTheSame(oldItem: IdolImage, newItem: IdolImage): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: IdolImage, newItem: IdolImage): Boolean {
            return oldItem == newItem
        }
    }

    val frag = fragment
    val activity = fragment.requireActivity()

    override fun onBindViewHolder(holder: IdolGalleryViewHolder, position: Int) {
        val idolImage:IdolImage = getItem(position) ?: return

        idolImage.id = getItem(position)!!.id

        holder.swipeLayout.showMode = SwipeLayout.ShowMode.PullOut
        holder.swipeLayout.addDrag(
            SwipeLayout.DragEdge.Right,
            holder.swipeLayout.findViewById<View>(R.id.right_view)
        )

        holder.swipeLayout.getSurfaceView().setOnClickListener {
            NavigationHelper.openIdolImageProfile(
                activity,
                idolImage,
                frag.viewModel.idolProfile.value
            )
        }

        holder.swipeLayout.findViewById<View>(R.id.favorite).setOnClickListener{
        }

        holder.swipeLayout.findViewById<View>(R.id.idol_profile).setOnClickListener{
        }

        holder.swipeLayout.addSwipeListener(this)

        holder.bind(idolImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IdolGalleryViewHolder {
        val view = frag.layoutInflater.inflate(R.layout.item_idol_gallery, parent, false)
        return IdolGalleryViewHolder(view, createOnClickListener(), activity)
    }

    private fun createOnClickListener(): IdolGalleryViewHolder.OnClickListener {
        return IdolGalleryViewHolder.OnClickListener { position, view, (id) ->
            //Log.d("test", "" + id)
        }
    }

    /**
     * SwipeLayout.SwipeListener events.
     */
    fun showDropright(layout: SwipeLayout?) {
        val ivDropRight = layout?.findViewById<ImageView>(R.id.dropright)
        ivDropRight?.setImageResource(R.drawable.ic_arrow_dropright)

    }
    fun showDropleft(layout: SwipeLayout?) {
        val ivDropRight = layout?.findViewById<ImageView>(R.id.dropright)
        ivDropRight?.setImageResource(R.drawable.ic_arrow_dropleft)
    }

    override fun onStartOpen(layout: SwipeLayout?) {
        showDropright(layout)
    }

    override fun onOpen(layout: SwipeLayout?) {
        showDropright(layout)
    }

    override fun onStartClose(layout: SwipeLayout?) {
        showDropleft(layout)
    }

    override fun onClose(layout: SwipeLayout?) {
        showDropleft(layout)
    }

    override fun onUpdate(layout: SwipeLayout?, leftOffset: Int, topOffset: Int) {
    }

    override fun onHandRelease(layout: SwipeLayout?, xvel: Float, yvel: Float) {
    }
}