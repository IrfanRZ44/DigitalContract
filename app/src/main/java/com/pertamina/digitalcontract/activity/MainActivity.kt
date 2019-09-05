package com.pertamina.digitalcontract.activity

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.pertamina.digitalcontract.Contract
import com.pertamina.digitalcontract.GlideApp
import com.pertamina.digitalcontract.R
import com.pertamina.digitalcontract.ResultContract
import com.pertamina.digitalcontract.adapter.AdapterContract
import com.pertamina.digitalcontract.util.SessionManager
import com.pertamina.digitalcontract.util.UserRole
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_loading.*
import kotlinx.android.synthetic.main.content_main.*
import okhttp3.ResponseBody
import org.json.JSONObject
import java.util.HashMap
import kotlin.collections.ArrayList
import kotlin.collections.MutableList
import kotlin.collections.set

class MainActivity : ActBaseFullScreen(), NavigationView.OnNavigationItemSelectedListener,
        (Contract, Int, Int) -> Unit {

    private var mLastListType: String? = null

    private var mUserRole: UserRole? = null
    private var imeiResponse : Int? = 0

    var list : MutableList<Contract>? = ArrayList()
    var adapter : AdapterContract? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this@MainActivity)
        FirebaseMessaging.getInstance().subscribeToTopic("all")
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            Log.d("balao main",it.token)
        }
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        session = SessionManager(this)
        val myName = session.name
        val userRoleIndex = session.role?.toInt()?:-1

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        //define drawer header layout
        val headerLayout = nav_view.getHeaderView(0)
        val txt = headerLayout.findViewById<TextView>(R.id.name)
        val role = headerLayout.findViewById<TextView>(R.id.title)
        txt.text = myName

        //define toolbar title
        title = myName

        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu)

        if (userRoleIndex >= 0) {
            mUserRole = UserRole.values()[userRoleIndex]
            role.text = mUserRole!!.toString()

            supportActionBar?.subtitle = mUserRole!!.toString()
        }

        //setup recylcer
        val layoutManager = LinearLayoutManager(this)
        recycler.layoutManager = layoutManager
        recycler.isNestedScrollingEnabled = false
        adapter = list?.let { it1 -> AdapterContract(this, it1,this) }
        recycler.adapter = adapter

        //setup drawer
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        //setup rest
        progress.setBackgroundResource(R.drawable.animation_loading)
        anim = progress.background as AnimationDrawable
        nav_view.setNavigationItemSelectedListener(this)

        swipeRefresh.setOnRefreshListener {
            swipeRefresh.isRefreshing = false
            checkActiveAccount(mLastListType?:"-1")
        }

        btRefresh.setOnClickListener{
            checkActiveAccount(mLastListType?:"-1")
        }
    }

    override fun onResume() {
        if (!session.isLoggedIn) {
            Logout()
        }
        else checkActiveAccount("-1")
        super.onResume()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_all -> {
                checkActiveAccount("-1")
            }
            R.id.nav_unread -> {
                checkActiveAccount("0")
            }
            R.id.nav_read -> {
                checkActiveAccount("1")
            }
            R.id.nav_rejected -> {
                checkActiveAccount("2")
            }
            R.id.nav_approved -> {
                checkActiveAccount("3")
            }
            R.id.nav_register-> {
                startActivity(Intent(this@MainActivity,ActRegister::class.java))
            }
            R.id.nav_about-> {
                startActivity(Intent(this@MainActivity,ActAbout::class.java))
            }
            R.id.nav_logout-> {
                Logout()
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    // -1 all
    // 0 : unread
    // 1 : read
    // 2 : rejected
    // 3 : approved
    private fun checkActiveAccount(code: String) {
        mLastListType = code

        val body = HashMap<String,String>()
        body["imei"] = session.imei?:""
        body["id_user"] = session.id?:""

        val body2 = HashMap<String,String>()
        body2["status"] = code
        body2["id_user"] = session.id?:""

        /*disposable = service.getContract(body2)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { onLoading() }
            .subscribe(
                { result -> onSuccessGetContract(result) },
                { error -> errorKoneksi(error) }
            )*/

        disposable = service.checkImei(body)
            .flatMap {
                    result -> onSuccessCheckImei(result)
                return@flatMap service.getContract(body2)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { onLoading() }
            .subscribe(
                { result -> onSuccessGetContract(result) },
                { error -> errorKoneksi(error) }
            )
    }

    private fun onSuccessCheckImei(result : ResponseBody){
        val obj = JSONObject(result.string())
        imeiResponse = obj.getInt("response")
    }

    private fun onSuccessGetContract(result : ResultContract){
        try {
            if (imeiResponse != 1) {
                Logout()
            } else {
                list?.clear()
                list?.addAll(result.response)

                if(list?.size?:0>0) {
                    onComplete()
                    adapter?.notifyDataSetChanged()
                }
                else{
                    onError()
                    GlideApp.with(this).load(R.drawable.not_found).into(ivRefresh)
                    tvRefreshTitle.text = resources.getString(R.string.noData)
                    tvRefreshDesc.text = resources.getString(R.string.noDataDetail)
                }
            }
        }
        catch (e : Exception){
            Logout()
        }
    }

    override fun invoke(data: Contract, pos: Int, myStatus: Int) {
        val i = Intent(this@MainActivity, ActDocumentViewer::class.java)
        i.putExtra("DOC_TITLE", data.CONTRACT_TITLE)
        i.putExtra("DOC_ID", data.CONTRACT_ID)
        i.putExtra("DOC_STATUS", myStatus)
        if(data.VENDOR_CERTIFICATE=="3" && data.OFFICER_CERTIFICATE == "3") i.putExtra("DOC_DOWNLOAD", true)
        else i.putExtra("DOC_DOWNLOAD", false)
        i.putExtra("DOC_STATUS", myStatus)
        startActivity(i)
        /*Log.d("balao",data.CONTRACT_ID)
        Log.d("balao",data.CONTRACT_TITLE)
        Log.d("balao",session.id)*/

    }
}
