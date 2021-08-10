package com.touchizen.idolface.ui.idols

import android.app.Dialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.touchizen.idolface.MainActivity
import com.touchizen.idolface.R
import com.touchizen.idolface.model.IdolProfile

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
) : PagingDataAdapter<IdolProfile, IdolViewHolder>(IDOL_COMPARATOR){

    val mDialog: Dialog = dialog
    val mSwipeContainer: SwipeRefreshLayout? = fragment.swipeContainer

    val activity: MainActivity = fragment.requireActivity() as MainActivity

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IdolViewHolder {

        val inflater = LayoutInflater.from(parent.getContext())
        val itemView: View = inflater.inflate(R.layout.item_idol, parent, false)
        return IdolViewHolder(itemView, createOnClickListener(), activity)

    }

    override fun onBindViewHolder(holder: IdolViewHolder, position: Int) {

        holder.bind(items[position])
    }

    //보여줄 아이템 개수가 몇개인지 알려줍니다
    override fun getItemCount(): Int = items.size

    private fun createOnClickListener(): IdolViewHolder.OnClickListener {
        return IdolViewHolder.OnClickListener { position, view, (id) ->
            //Log.d("test", "" + id)
        }
    }
}