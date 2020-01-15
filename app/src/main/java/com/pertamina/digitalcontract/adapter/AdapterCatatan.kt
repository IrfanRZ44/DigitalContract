package com.pertamina.digitalcontract.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import com.pertamina.digitalcontract.R
import com.pertamina.digitalcontract.model.ModelLogRejected
import kotlinx.android.synthetic.main.list_catatan.*


class AdapterCatatan(
    private val list: ArrayList<ModelLogRejected>,
    private val context: Context
) :
    RecyclerView.Adapter<AdapterCatatan.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(context, list[position], position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return AdapterCatatan.ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.list_catatan,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView),
        LayoutContainer {

        @SuppressLint("SetTextI18n")
        fun bindItem(context: Context, reviewer: ModelLogRejected, position: Int) {
            textMessage.text = reviewer.message
            textDate.text = reviewer.dateTime
            textUser.text = reviewer.status + " oleh " + reviewer.user

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (reviewer.status.contains("Accept")) {
                    textUser.setTextColor(context.getColor(R.color.material_green_600))
                } else {
                    textUser.setTextColor(context.getColor(R.color.material_red_500))
                }
            }
        }
    }
}