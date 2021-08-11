package com.touchizen.idolface.ui.idols

import android.app.Dialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.touchizen.idolface.MainActivity
import com.touchizen.idolface.R
import com.touchizen.idolface.model.IdolProfile
import com.touchizen.idolface.utils.NavigationHelper
import com.touchizen.swipe.SwipeLayout

val IDOL_COMPARATOR = object : DiffUtil.ItemCallback<IdolProfile>() {
    override fun areItemsTheSame(oldItem: IdolProfile, newItem: IdolProfile): Boolean =
        // User ID serves as unique ID
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: IdolProfile, newItem: IdolProfile): Boolean =
        // Compare full contents (note: Java users should call .equals())
        oldItem == newItem
}

class IdolsAdapter(
    val items: List<IdolProfile>,
    fragment: IdolsFragment,
    dialog: Dialog
) : PagingDataAdapter<IdolProfile, IdolViewHolder>(IDOL_COMPARATOR),
    SwipeLayout.SwipeListener
{

    val mDialog: Dialog = dialog
    val mSwipeContainer: SwipeRefreshLayout? = fragment.swipeContainer

    val activity: MainActivity = fragment.requireActivity() as MainActivity

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IdolViewHolder {

        val inflater = LayoutInflater.from(parent.getContext())
        val itemView: View = inflater.inflate(R.layout.item_idol, parent, false)
        return IdolViewHolder(itemView, createOnClickListener(), activity)

    }

    override fun onBindViewHolder(holder: IdolViewHolder, position: Int) {

        val idol:IdolProfile = items[position]

        holder.swipeLayout.showMode = SwipeLayout.ShowMode.PullOut
        holder.swipeLayout.addDrag(
            SwipeLayout.DragEdge.Right,
            holder.swipeLayout.findViewById<View>(R.id.right_view)
        )

        holder.swipeLayout.getSurfaceView().setOnClickListener {
            NavigationHelper.openIdolGalllery(activity,idol)
        }

        holder.swipeLayout.findViewById<View>(R.id.favorite).setOnClickListener{
        }

        holder.swipeLayout.findViewById<View>(R.id.idol_profile).setOnClickListener{
            goToProfile(idol)
        }

        holder.swipeLayout.addSwipeListener(this)

        holder.bind(idol)
    }

    fun goToProfile(idol: IdolProfile?) {
        NavigationHelper.openIdolProfile(activity, idol)
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


    //보여줄 아이템 개수가 몇개인지 알려줍니다
    override fun getItemCount(): Int = items.size

    private fun createOnClickListener(): IdolViewHolder.OnClickListener {
        return IdolViewHolder.OnClickListener { position, view, (id) ->
            //Log.d("test", "" + id)
        }
    }
}