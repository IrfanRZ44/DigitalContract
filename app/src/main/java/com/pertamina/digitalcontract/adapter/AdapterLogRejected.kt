package com.pertamina.digitalcontract.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.Adapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.GsonBuilder
import com.pertamina.digitalcontract.Contract
import com.pertamina.digitalcontract.Extra
import com.pertamina.digitalcontract.util.SessionManager
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_contract.*
import com.pertamina.digitalcontract.R
import com.pertamina.digitalcontract.activity.ActDocumentViewer
import com.pertamina.digitalcontract.model.ModelLogRejected
import com.pertamina.digitalcontract.rest.RetrofitApi
import com.pertamina.digitalcontract.util.FontAwasomeTextView
import kotlinx.android.synthetic.main.list_log_rejected.*
import kotlinx.android.synthetic.main.list_reviewer.*
import kotlinx.android.synthetic.main.sub_fab.*
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.jetbrains.anko.textColor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.HashMap
import java.util.concurrent.TimeUnit


class AdapterLogRejected(
    private val list: ArrayList<ModelLogRejected>,
    private val context: Context
) :
    RecyclerView.Adapter<AdapterLogRejected.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(context, list[position], position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return AdapterLogRejected.ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.list_log_rejected,
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
        }
    }
}