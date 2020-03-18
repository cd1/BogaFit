package com.gmail.cristiandeives.bogafit

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.MainThread
import androidx.annotation.UiThread
import com.gmail.cristiandeives.bogafit.data.Physictivity

@MainThread
class PhysictivityTypeSpinnerAdapter(ctx: Context) : ArrayAdapter<Physictivity.Type>(ctx, android.R.layout.simple_spinner_item) {
    var data = emptyArray<Physictivity.Type>()
        @UiThread
        set(value) {
            field = value

            notifyDataSetChanged()
        }

    init {
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    }

    override fun getCount() = data.size

    override fun getItem(position: Int) = data[position]

    override fun getView(position: Int, convertView: View?, parent: ViewGroup) =
        getItemView(super.getView(position, convertView, parent), position)

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup) =
        getItemView(super.getDropDownView(position, convertView, parent), position)

    @UiThread
    private fun getItemView(view: View, position: Int): View {
        val type = getItem(position)

        val res = context.resources
        val resId = res.getIdentifier(type.toString(), "string", context.packageName)

        val textView = view as TextView
        textView.text = res.getString(resId)

        return textView
    }
}