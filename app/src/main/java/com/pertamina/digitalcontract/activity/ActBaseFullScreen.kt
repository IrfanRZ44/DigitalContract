package com.pertamina.digitalcontract.activity

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Base64
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.pertamina.digitalcontract.GlideApp
import com.pertamina.digitalcontract.R
import com.pertamina.digitalcontract.rest.ApiInterface
import com.pertamina.digitalcontract.util.SessionManager
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.content_loading.*
import retrofit2.HttpException
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import java.io.ByteArrayOutputStream
import java.io.IOException

open class ActBaseFullScreen : AppCompatActivity() {

    lateinit var anim : AnimationDrawable
    lateinit var session: SessionManager
    lateinit var disposable : Disposable
    open val service by lazy{
        ApiInterface.create()
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true)
        }
        if (Build.VERSION.SDK_INT >= 19) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
            window.statusBarColor = Color.TRANSPARENT
        }
    }

    fun onLoading(){
        cover.visibility = View.VISIBLE
        progress.visibility = View.VISIBLE
        refresh.visibility = View.GONE
        anim?.start()
    }

     fun onComplete(){
        cover.visibility = View.GONE
        progress.visibility = View.GONE
        refresh.visibility = View.GONE
        anim?.stop()
    }

    fun onError(){
        cover.visibility = View.VISIBLE
        progress.visibility = View.GONE
        refresh.visibility = View.VISIBLE
        anim?.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
//        disposable?.dispose()
        anim?.stop()
    }

    fun errorKoneksi(e : Throwable){
        onError()

        if (e is HttpException) { //error server
            GlideApp.with(this).load(R.drawable.no_internet).into(ivRefresh)
            tvRefreshTitle.text = resources.getString(R.string.errorKoneksi)
            tvRefreshDesc.text = resources.getString(R.string.errorKoneksiDetail)
        } else {
            GlideApp.with(this).load(R.drawable.no_internet).into(ivRefresh)
            tvRefreshTitle.text = resources.getString(R.string.errorKoneksi)
            tvRefreshDesc.text = resources.getString(R.string.errorKoneksiDetail)
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        System.gc()
    }

    fun Logout() {
        Thread(Runnable {
            try {
                FirebaseInstanceId.getInstance().deleteInstanceId()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }).start()

        FirebaseMessaging.getInstance().unsubscribeFromTopic("all")
        session?.logoutUser()
        finish()
    }

    open fun setWindowFlag(bits: Int, on: Boolean) {
        val win = window
        val winParams = win.attributes
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }

    open fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    open fun base64ToBitmap(b64: String): Bitmap {

        val imageAsBytes = Base64.decode(b64.toByteArray(), Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.size)
    }
}