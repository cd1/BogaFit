package com.gmail.cristiandeives.bogafit

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.gmail.cristiandeives.bogafit.data.Physictivity
import com.gmail.cristiandeives.bogafit.databinding.ViewHolderListPhysictivityBinding
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class ListPhysictivityRecyclerAdapter : RecyclerView.Adapter<ListPhysictivityRecyclerAdapter.ViewHolder>() {
    private val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)

    var data = listOf<Physictivity>()
        set(value) {
            val callback = CB(field, value)
            field = value

            DiffUtil.calculateDiff(callback, false).dispatchUpdatesTo(this)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.v(TAG, "> onCreateViewHolder(...)")

        val context = parent.context
        val inflater = LayoutInflater.from(context)

        val binding = ViewHolderListPhysictivityBinding.inflate(inflater, parent, false)

        val viewHolder = ViewHolder(binding)
        Log.v(TAG, "< onCreateViewHolder(...): $viewHolder")
        return viewHolder
    }

    override fun getItemCount(): Int {
        Log.v(TAG, "> getItemCount()")

        val count = data.size
        Log.v(TAG, "< getItemCount(): $count")
        return count
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.v(TAG, "> onBindViewHolder(..., position=$position)")

        val context = holder.itemView.context
        val physictivity = data[position]

        holder.binding.apply {
            typeText.setText(physictivity.type?.toStringResource(context) ?: R.string.unknown_physictivity)
            dateText.text = dateFormatter.format(physictivity.date)
        }

        Log.v(TAG, "< onBindViewHolder(..., position=$position)")
    }

    class ViewHolder(val binding: ViewHolderListPhysictivityBinding) : RecyclerView.ViewHolder(binding.root)

    class CB(private val oldData: List<Physictivity>, private val newData: List<Physictivity>) : DiffUtil.Callback() {
        override fun getOldListSize() = oldData.size

        override fun getNewListSize() = newData.size

        override fun areItemsTheSame(oldPosition: Int, newPosition: Int) =
            oldData[oldPosition].id == newData[newPosition].id

        override fun areContentsTheSame(oldPosition: Int, newPosition: Int) =
            oldData[oldPosition] == newData[newPosition]
    }

    companion object {
        private val TAG = ListPhysictivityRecyclerAdapter::class.java.simpleName

        @StringRes
        private fun Physictivity.Type.toStringResource(ctx: Context) =
            ctx.resources.getIdentifier(toString(), "string", ctx.packageName)
    }
}