package com.pertamina.digitalcontract.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.pertamina.digitalcontract.R
import com.pertamina.digitalcontract.util.UserRole
import com.pertamina.digitalcontract.Contract
import com.pertamina.digitalcontract.Extra
import com.pertamina.digitalcontract.util.SessionManager
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_contract.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.textColor
import uk.co.chrisjenx.calligraphy.CalligraphyUtils
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class AdapterExtra(private val context: Context, private val items: MutableList<Extra>, private val listener: (Extra, Int) -> Unit)
    : RecyclerView.Adapter<AdapterExtra.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_extra, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(context,items[position], position, listener)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView),
            LayoutContainer {

        lateinit var session : SessionManager

        @SuppressLint("SetTextI18n")
        fun bindItem(context: Context, items: Extra, position: Int, listener: (Extra, Int) -> Unit) {
            session = SessionManager(context)

            tvTitle.text = items.name

            containerView.setOnClickListener { listener(items,position) }
        }
    }

    fun addItem(dataObj: MutableList<Extra>) {
        items.addAll(dataObj)
        notifyDataSetChanged()
    }
}