package com.touchizen.idolface.ui.idols

import android.app.Dialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.touchizen.idolface.MainActivity
import com.touchizen.idolface.R
import com.touchizen.idolface.model.IdolProfile

class IdolsAdapter(
    val items: List<IdolProfile>,
    fragment: IdolsFragment,
    dialog: Dialog
) : RecyclerView.Adapter<IdolViewHolder>() {

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