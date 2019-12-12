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
import com.pertamina.digitalcontract.GlideApp
import com.pertamina.digitalcontract.util.SessionManager
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_contract.*
import com.pertamina.digitalcontract.R
import com.pertamina.digitalcontract.activity.ActDocumentViewer
import com.pertamina.digitalcontract.model.ModelReviewer
import com.pertamina.digitalcontract.rest.RetrofitApi
import com.pertamina.digitalcontract.util.FontAwasomeTextView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.list_reviewer.*
import kotlinx.android.synthetic.main.sub_fab.*
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.jetbrains.anko.textColor
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.HashMap
import java.util.concurrent.TimeUnit


class AdapterReviewer(
    private val list: ArrayList<ModelReviewer>,
    private val context: Context,
    private val roleUser: Int,
    private val dialog: Dialog,
    private val activity: Activity
) :
    RecyclerView.Adapter<AdapterReviewer.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(context, list[position], position, roleUser, dialog, activity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return AdapterReviewer.ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.list_reviewer,
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
        fun bindItem(context: Context, reviewer: ModelReviewer, position: Int, roleUser: Int, dialog: Dialog, activity: Activity) {
            rbReviewer.text = reviewer.name

            rbReviewer.setOnClickListener(View.OnClickListener { v ->

//                Log.e("Data", reviewer.userId + "  " +reviewer.username)
                sendReviewer(reviewer, context, roleUser, dialog, activity)
            })
        }

        fun sendReviewer(reviewer: ModelReviewer, context: Context, roleUser: Int, dialog: Dialog, activity: Activity) {
            val progressDialog = ProgressDialog(context, R.style.MyProgressDialogTheme)
            var jenisMgr = ""

            dialog.dismiss()
            progressDialog.setMessage("Mohon tunggu...")
            progressDialog.setCancelable(false)
            progressDialog.show()

            //role user  37 = MGR Finance, 38 = MGR Legal, 19 = MGR HSSE
            //role user  39 = Staff Finance, 40 = Staff Legal, 33 = Staff HSSE
            if (roleUser == 37) {
                jenisMgr = "FINANCE_ID"
            } else if (roleUser == 38) {
                jenisMgr = "LEGAL_ID"
            } else if (roleUser == 19) {
                jenisMgr = "HSSE_ID"
            } else if ((roleUser >= 8) && (roleUser <= 18) or (roleUser == 20) or (roleUser == 21)){
                jenisMgr = "REVIEWER_ID"
            }

            val body = HashMap<String, String>()
            body["id_user"] = reviewer.userId
            body["id_contract"] = reviewer.id_contract
            body["jenis_mgr"] = jenisMgr
            Log.e("Body select", body.toString())

            val gson = GsonBuilder().setLenient().create()
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .writeTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(RetrofitApi.JSON_SENDER2)
                .addConverterFactory(
                    GsonConverterFactory.create(gson)
                )
                .client(okHttpClient)
            val api = retrofit.build().create(RetrofitApi::class.java)

            val call = api.setReviewer(
                body,
                "application/json"
            )

            call.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    val result = response.body()
                    val message = result?.string()

                    if (message?.contains("Success")!!){
                        changeStatusContract(3, reviewer, context, dialog, activity, progressDialog)
                    }
                    else{
                        dialogFailed("Gagal memilih reviewer", dialog, context)
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    progressDialog.dismiss()
                    dialogFailed(t.message.toString(), dialog, context)
                }
            })

        }

        private fun changeStatusContract(currentStatus: Int, reviewer: ModelReviewer, context: Context, dialog: Dialog, activity: Activity, progressDialog: ProgressDialog) {
            val session = SessionManager(context)
            val body = HashMap<String, String>()
            body["id_user"] = session?.id.toString()
            body["id_contract"] = reviewer?.id_contract.toString()
            body["user_status"] = currentStatus.toString()
            body["note"] = ""

            Log.e("Body choose set status ", body.toString())

            val gson = GsonBuilder().setLenient().create()
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .writeTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(RetrofitApi.JSON_SENDER1)
                .addConverterFactory(
                    GsonConverterFactory.create(gson)
                )
                .client(okHttpClient)
            val api = retrofit.build().create(RetrofitApi::class.java)

            val call = api.set_status(
                body,
                "application/json"
            )

            call.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    val result = response.body()
                    val message = result?.string()
                    val obj = JSONObject(message)
                    val response = obj.getInt("response")

                    progressDialog.dismiss()
                    if (response == 1) {
                        dialogSuccess("Berhasil memilih reviewer", dialog, context, activity, currentStatus)
                    }
                    else{
                        dialogFailed("Gagal memilih reviewer", dialog, context)
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    progressDialog.dismiss()
                    dialogFailed(t.message.toString(), dialog, context)
                }
            })
        }

        fun dialogSuccess(teks: String, dialog: Dialog, context: Context, activity: Activity, status : Int) {
            dialog?.setContentView(R.layout.dialog_status)

            val faIcon = dialog?.findViewById<FontAwasomeTextView>(R.id.faIcon)
            val tvTitle = dialog?.findViewById<TextView>(R.id.tvTitle)
            val tvDeskripsi = dialog?.findViewById<TextView>(R.id.tvDeskripsi)
            val btOk = dialog?.findViewById<Button>(R.id.btOk)
            faIcon?.text = context.resources.getText(R.string.fa_check)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                faIcon?.background = context.resources.getDrawable(R.drawable.bg_circle_green, null)
                btOk?.background = context.resources.getDrawable(R.drawable.bg_outer_green, null)
            } else {
                faIcon?.background = context.resources.getDrawable(R.drawable.bg_circle_green)
                btOk?.background = context.resources.getDrawable(R.drawable.bg_outer_green)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                btOk?.textColor = context.resources.getColor(R.color.cpb_green, null)
                tvTitle?.textColor = context.resources.getColor(R.color.cpb_green, null)
            } else {
                btOk?.textColor = context.resources.getColor(R.color.cpb_green)
                tvTitle?.textColor = context.resources.getColor(R.color.cpb_green)
            }
            tvTitle?.text = "Success"
            tvDeskripsi?.text = teks
            btOk?.setOnClickListener {
                activity.tvSign.text = activity.resources.getString(R.string.already_verified)
                val i = Intent(activity, ActDocumentViewer::class.java)
                val mDocTitle = activity.intent.getStringExtra("DOC_TITLE")
                val mDocPath = activity.intent.getStringExtra("DOC_PATH")
                val mContractId = activity.intent.getStringExtra("DOC_ID")
                val mContractStatus = status

                i.putExtra("DOC_TITLE", mDocTitle)
                if(mDocPath != null){
                    i.putExtra("DOC_PATH", mDocPath)
                }else{
                    i.putExtra("DOC_PATH", "")
                }
                i.putExtra("DOC_ID", mContractId)
                i.putExtra("DOC_STATUS", mContractStatus)
                i.putExtra("DOC_DOWNLOAD", true)

                activity.startActivity(i)
                dialog?.dismiss()
                activity.finish()

            }
            dialog?.show()
        }

        fun dialogFailed(teks: String, dialog: Dialog, context: Context) {
            dialog?.setContentView(R.layout.dialog_status)

            val faIcon = dialog?.findViewById<FontAwasomeTextView>(R.id.faIcon)
            val tvTitle = dialog?.findViewById<TextView>(R.id.tvTitle)
            val tvDeskripsi = dialog?.findViewById<TextView>(R.id.tvDeskripsi)
            val btOk = dialog?.findViewById<Button>(R.id.btOk)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                tvTitle?.textColor = context.resources.getColor(R.color.cpb_red, null)
                btOk?.textColor = context.resources.getColor(R.color.cpb_red, null)
            } else {
                tvTitle?.textColor = context.resources.getColor(R.color.cpb_red)
                btOk?.textColor = context.resources.getColor(R.color.cpb_red)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                faIcon?.background = context.resources.getDrawable(R.drawable.bg_circle_red, null)
                btOk?.background = context.resources.getDrawable(R.drawable.bg_outer_red, null)
            } else {
                faIcon?.background = context.resources.getDrawable(R.drawable.bg_circle_red)
                btOk?.background = context.resources.getDrawable(R.drawable.bg_outer_red)
            }
            faIcon?.text = context.resources.getText(R.string.fa_times)
            tvTitle?.text = "Failed"
            tvDeskripsi?.text = teks
            btOk?.setOnClickListener {
                dialog?.dismiss()
            }

            dialog?.show()
        }
    }
}