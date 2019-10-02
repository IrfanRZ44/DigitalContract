package com.pertamina.digitalcontract.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.Adapter
import androidx.recyclerview.widget.RecyclerView
import com.pertamina.digitalcontract.Extra
import com.pertamina.digitalcontract.util.SessionManager
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_contract.*
import com.pertamina.digitalcontract.R
import kotlinx.android.synthetic.main.list_reviewer.*


class AdapterReviewer(private val list:ArrayList<String>, private val context: Context) : RecyclerView.Adapter<AdapterReviewer.ViewHolder>(){
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(context, list[position], position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return AdapterReviewer.ViewHolder(LayoutInflater.from(context).inflate(R.layout.list_reviewer, parent, false))
    }

    override fun getItemCount(): Int{
        return list.size
    }

    class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView),
            LayoutContainer {

        @SuppressLint("SetTextI18n")
        fun bindItem(context: Context, items: String, position: Int) {
            rbReviewer.text = items
        }
    }
}