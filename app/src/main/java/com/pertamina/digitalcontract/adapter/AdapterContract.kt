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

class AdapterContract(private val context: Context, private val items: MutableList<Contract>, private val listener: (Contract, Int, Int) -> Unit)
    : RecyclerView.Adapter<AdapterContract.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_contract, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(context,items[position], position, listener)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView),
            LayoutContainer {

        lateinit var session : SessionManager

        @SuppressLint("SetTextI18n")
        fun bindItem(context: Context, items: Contract, position: Int, listener: (Contract, Int, Int) -> Unit) {
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
            val mUserRole = UserRole.values()[session.role?.toInt()?:-1]
            if (mUserRole == UserRole.Reviewer) {
                myStatus = items.REVIEWER_STATUS?.toInt()?:0
            } else if (mUserRole == UserRole.Legal) {
                myStatus = items.LEGAL_STATUS?.toInt()?:0
            } else if (mUserRole == UserRole.Finance) {
                myStatus = items.FINANCE_STATUS?.toInt()?:0
            } else if (mUserRole == UserRole.Officer) {
                val statusOfficer = items.OFFICER_CERTIFICATE
                if (statusOfficer == "") {
                    myStatus = 0
                } else {
                    myStatus = Integer.parseInt(statusOfficer)
                }
            } else if (mUserRole == UserRole.Vendor) {
                val statusOfficer = items.VENDOR_CERTIFICATE
                if (statusOfficer == "") {
                    myStatus = 0
                } else {
                    myStatus = Integer.parseInt(statusOfficer)
                }
            }

            when (myStatus) {
                3 //approved
                -> {
                    tvStatus.setText(R.string.fa_check)
                    tvStatus.setTextColor(ResourcesCompat.getColor(itemView.resources, R.color.cpb_green_dark, null))
                    viewColor.background = ResourcesCompat.getDrawable(itemView.resources, R.color.cpb_green_dark,null)
                }
                2 //reject
                -> {
                    tvStatus.setText(R.string.fa_times)
                    tvStatus.setTextColor(ResourcesCompat.getColor(itemView.resources, R.color.redSoft, null))
                    viewColor.background = ResourcesCompat.getDrawable(itemView.resources, R.color.redSoft,null)
                }
                else //pending
                -> {
                    tvStatus.setText(R.string.fa_pending)
                    tvStatus.setTextColor(ResourcesCompat.getColor(itemView.resources, R.color.material_grey_600, null))
                    viewColor.background = ResourcesCompat.getDrawable(itemView.resources, R.color.blue,null)
                }
            }

            //read
            if(myStatus > 0){
                CalligraphyUtils.applyFontToTextView(context,tvTitle,"fonts/Montserrat-Regular.ttf")
            }
            //unread
            else{
                CalligraphyUtils.applyFontToTextView(context,tvTitle,"fonts/Montserrat-Bold.ttf")
            }

            containerView.setOnClickListener { listener(items,position,myStatus) }
        }

        fun convertRupiah(angka:Double):String{
            val localeID = Locale("in", "ID")
            val formatRupiah = NumberFormat.getCurrencyInstance(localeID)
            return formatRupiah.format(angka).replace("Rp","Rp ")
        }
    }

    fun addItem(dataObj: MutableList<Contract>) {
        items.addAll(dataObj)
        notifyDataSetChanged()
    }
}