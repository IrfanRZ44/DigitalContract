package com.pertamina.digitalcontract.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.GsonBuilder
import com.pertamina.digitalcontract.R
import com.pertamina.digitalcontract.util.UserRole
import com.pertamina.digitalcontract.Contract
import com.pertamina.digitalcontract.rest.RetrofitApi
import com.pertamina.digitalcontract.util.SessionManager
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_contract.*
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.db.INTEGER
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.textColor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import uk.co.chrisjenx.calligraphy.CalligraphyUtils
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class AdapterContract(
    private val context: Context,
    private val items: MutableList<Contract>,
    private val listener: (Contract, Int, Int) -> Unit
) : RecyclerView.Adapter<AdapterContract.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_contract,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(context, items[position], position, listener)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView),
        LayoutContainer {

        lateinit var session: SessionManager

        @SuppressLint("SetTextI18n")
        fun bindItem(
            context: Context,
            items: Contract,
            position: Int,
            listener: (Contract, Int, Int) -> Unit
        ) {
            session = SessionManager(context)

            /*if(position % 2 ==0){
                root.backgroundColor = ContextCompat.getColor(context, android.R.color.white)
            }
            else root.backgroundColor = ContextCompat.getColor(context, R.color.row_bright)*/

            val dformat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val date = dformat.parse(items.CREATED_ON)
            var dateFormatList = SimpleDateFormat("dd MMMM yyyy")

            tvTitle.text = items.CONTRACT_TITLE
            tvDate.text = dateFormatList.format(date)

            var myStatus = -1
            val mUserRole = UserRole.values()[session.role?.toInt() ?: -1]
            if (mUserRole == UserRole.Reviewer) {
                myStatus = items.REVIEWER_STATUS?.toInt() ?: 0
            } else if (mUserRole == UserRole.Legal) {
                myStatus = items.LEGAL_STATUS?.toInt() ?: 0
            } else if (mUserRole == UserRole.Finance) {
                myStatus = items.FINANCE_STATUS?.toInt() ?: 0
            } else if (mUserRole == UserRole.Officer) {
                val statusOfficer = items.OFFICER_CERTIFICATE
                val statusVendor = items.VENDOR_CERTIFICATE

                if (statusOfficer == "") {
                    myStatus = 0
                } else if (statusVendor == "5"){
                    myStatus = 3
                } else{
                    myStatus = Integer.parseInt(statusOfficer)
                }
                Log.e("officer ", statusOfficer + "saya null")
                Log.e("Vendor ", statusVendor)
                Log.e("status ", myStatus.toString())
            } else if (mUserRole == UserRole.Vendor) {
                val statusVendor = items.VENDOR_CERTIFICATE
                if (statusVendor == "") {
                    myStatus = 0
                } else if (statusVendor == "3"){
                    myStatus = 0
                }
                else if (statusVendor == "5"){
                    myStatus = 3;
                }
            } else if (mUserRole == UserRole.Mgr_Finance) {
                if ((items?.FINANCE_ID == "0") or (items?.FINANCE_ID == "")) {
                    myStatus = 0
                } else {
                    if (items?.FINANCE_STATUS == "3") {
                        myStatus = 6
                    } else if (items?.FINANCE_STATUS == "4") {
                        myStatus = 4
                    } else if (items?.FINANCE_STATUS == "5") {
                        myStatus = 5
                    } else {
                        myStatus = 7
                    }
                }
            } else if (mUserRole == UserRole.Mgr_HSSE_MOR_VII) {
                if ((items?.HSSE_ID == "0") or (items?.HSSE_ID == "")) {
                    myStatus = 0
                } else {
                    if (items?.HSSE_STATUS == "3") {
                        myStatus = 6
                    } else if (items?.HSSE_STATUS == "4") {
                        myStatus = 4
                    } else if (items?.HSSE_STATUS == "5") {
                        myStatus = 5
                    } else {
                        myStatus = 7
                    }
                }
            } else if (mUserRole == UserRole.Mgr_Legal) {
                if ((items?.LEGAL_ID == "0") or (items?.LEGAL_ID == "")) {
                    myStatus = 0
                } else {
                    if (items?.LEGAL_STATUS == "3") {
                        myStatus = 6
                    } else if (items?.LEGAL_STATUS == "4") {
                        myStatus = 4
                    } else if (items?.LEGAL_STATUS == "5") {
                        myStatus = 5
                    } else {
                        myStatus = 7
                    }
                }
            } else if ((mUserRole == UserRole.Mgr_HC) or (mUserRole == UserRole.Mgr_TSR_VII)
                or (mUserRole == UserRole.Mgr_Industri_Marine) or (mUserRole == UserRole.Mgr_Retail)
                or (mUserRole == UserRole.Mgr_QM) or (mUserRole == UserRole.Mgr_Internal_Audit)
                or (mUserRole == UserRole.Mgr_IT_MOR_VII) or (mUserRole == UserRole.Mgr_Marine_Region_VII)
                or (mUserRole == UserRole.Mgr_Domgas_Region_VII) or (mUserRole == UserRole.Mgr_Aviation_Region_VII)
                or (mUserRole == UserRole.Mgr_S_dan_D_Region_VII) or (mUserRole == UserRole.Mgr_Assets_Management_MOR_VII)
                or (mUserRole == UserRole.Mgr_Medical_Sulawesi)
            ) {
                if ((items?.REVIEWER_ID == "") or (items?.REVIEWER_ID == "0")) {
                    myStatus = 0
                } else {
                    if (items?.REVIEWER_STATUS == "3") {
                        myStatus = 6
                    } else if (items?.REVIEWER_STATUS == "4") {
                        myStatus = 4
                    } else if (items?.REVIEWER_STATUS == "5") {
                        myStatus = 5
                    } else {
                        myStatus = 7
                    }
                }
            } else if (mUserRole == UserRole.Reviewer_Vendor) {
                if (items?.VENDOR_CERTIFICATE == "") {
                    myStatus = 0
                } else {
                    myStatus = Integer.parseInt(items?.VENDOR_CERTIFICATE)
                }
            } else if (mUserRole == UserRole.Staf_Finance) {
                if (items?.FINANCE_STATUS == "") {
                    myStatus = 0
                } else {
                    myStatus = Integer.parseInt(items?.FINANCE_STATUS)
                }
            } else if (mUserRole == UserRole.Staf_HSSE_MOR_VII) {
                if (items?.HSSE_STATUS == "") {
                    myStatus = 0
                } else {
                    myStatus = Integer.parseInt(items?.HSSE_STATUS)
                }
            } else if (mUserRole == UserRole.Staf_Legal) {
                if (items?.LEGAL_STATUS == "") {
                    myStatus = 0
                } else {
                    myStatus = Integer.parseInt(items?.LEGAL_STATUS)
                }
            } else if ((mUserRole == UserRole.Staf_HC) or (mUserRole == UserRole.Staf_TSR_VII)
                or (mUserRole == UserRole.Staf_Industri_Marine) or (mUserRole == UserRole.Staf_Retail)
                or (mUserRole == UserRole.Staf_QM) or (mUserRole == UserRole.Staf_Internal_Audit)
                or (mUserRole == UserRole.Staf_IT_MOR_VII) or (mUserRole == UserRole.Staf_Marine_Region_VII)
                or (mUserRole == UserRole.Staf_Domgas_Region_VII) or (mUserRole == UserRole.Staf_Avigation_Region_VII)
                or (mUserRole == UserRole.Staf_S_dan_D_Region_VII) or (mUserRole == UserRole.Staf_Asset_Management_MOR_VII)
                or (mUserRole == UserRole.Staf_Medical_Sulawesi)
            ) {
                if (items?.REVIEWER_STATUS == "") {
                    myStatus = 0
                } else {
                    myStatus = Integer.parseInt(items?.REVIEWER_STATUS)
                }
            } else if (mUserRole == UserRole.Mgr_Procurement) {
                if (items.PUBLISHED == "1") {
                    myStatus = 3
                } else {
                    myStatus = Integer.parseInt(items.PUBLISHED)
                }
            }

            tvStatus2.visibility = View.GONE
            when (myStatus) {
                7 //already disposisi or reviewed by manager
                -> {
                    tvStatus.setText(R.string.fa_share)
                    tvStatus.setTextColor(
                        ResourcesCompat.getColor(
                            itemView.resources,
                            R.color.cpb_blue_dark,
                            null
                        )
                    )
                    viewColor.background =
                        ResourcesCompat.getDrawable(itemView.resources, R.color.cpb_blue_dark, null)
                }
                6 //already aprove by staff
                -> {
                    tvStatus.setText(R.string.fa_check)
                    tvStatus.setTextColor(
                        ResourcesCompat.getColor(
                            itemView.resources,
                            R.color.cpb_green_dark,
                            null
                        )
                    )
                    viewColor.background = ResourcesCompat.getDrawable(
                        itemView.resources,
                        R.color.cpb_green_dark,
                        null
                    )
                }
                5 //already aproved by manager
                -> {
                    tvStatus2.setText(R.string.fa_check)
                    tvStatus2.setTextColor(
                        ResourcesCompat.getColor(
                            itemView.resources,
                            R.color.cpb_green_dark,
                            null
                        )
                    )
                    tvStatus2.visibility = View.VISIBLE
                    tvStatus.setText(R.string.fa_check)
                    tvStatus.setTextColor(
                        ResourcesCompat.getColor(
                            itemView.resources,
                            R.color.cpb_green_dark,
                            null
                        )
                    )
                    viewColor.background = ResourcesCompat.getDrawable(
                        itemView.resources,
                        R.color.cpb_green_dark,
                        null
                    )
                }
                4 //reject
                -> {
                    tvStatus.setText(R.string.fa_unlike)
                    tvStatus.setTextColor(
                        ResourcesCompat.getColor(
                            itemView.resources,
                            R.color.redSoft,
                            null
                        )
                    )
                    viewColor.background =
                        ResourcesCompat.getDrawable(itemView.resources, R.color.redSoft, null)
                }
                3 //approved staff
                -> {
                    tvStatus.setText(R.string.fa_check)
                    tvStatus.setTextColor(
                        ResourcesCompat.getColor(
                            itemView.resources,
                            R.color.cpb_green_dark,
                            null
                        )
                    )
                    viewColor.background = ResourcesCompat.getDrawable(
                        itemView.resources,
                        R.color.cpb_green_dark,
                        null
                    )
                }
                2 //reject
                -> {
                    tvStatus.setText(R.string.fa_times)
                    tvStatus.setTextColor(
                        ResourcesCompat.getColor(
                            itemView.resources,
                            R.color.redSoft,
                            null
                        )
                    )
                    viewColor.background =
                        ResourcesCompat.getDrawable(itemView.resources, R.color.redSoft, null)
                }
                else //pending
                -> {
                    tvStatus.setText(R.string.fa_pending)
                    tvStatus.setTextColor(
                        ResourcesCompat.getColor(
                            itemView.resources,
                            R.color.material_grey_600,
                            null
                        )
                    )
                    viewColor.background =
                        ResourcesCompat.getDrawable(itemView.resources, R.color.blue, null)
                }
            }

            //read
            if (myStatus > 0) {
                CalligraphyUtils.applyFontToTextView(
                    context,
                    tvTitle,
                    "fonts/Montserrat-Regular.ttf"
                )
            }
            //unread
            else {
                CalligraphyUtils.applyFontToTextView(context, tvTitle, "fonts/Montserrat-Bold.ttf")
            }

            containerView.setOnClickListener { listener(items, position, myStatus) }
        }

        fun convertRupiah(angka: Double): String {
            val localeID = Locale("in", "ID")
            val formatRupiah = NumberFormat.getCurrencyInstance(localeID)
            return formatRupiah.format(angka).replace("Rp", "Rp ")
        }
    }

    fun addItem(dataObj: MutableList<Contract>) {
        items.addAll(dataObj)
        notifyDataSetChanged()
    }
}