package com.pertamina.digitalcontract.activity

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.AnimationDrawable
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.iid.FirebaseInstanceId
import com.pertamina.digitalcontract.Login
import com.pertamina.digitalcontract.R
import com.pertamina.digitalcontract.rest.ApiInterface
import com.pertamina.digitalcontract.util.SessionManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.act_login.*
import okhttp3.ResponseBody
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper

class ActLogin : AppCompatActivity(), View.OnClickListener {

    private var mImei = ""
    lateinit var anim : AnimationDrawable

    private var disposable : Disposable? = null
    private val service by lazy{
        ApiInterface.create()
    }

    lateinit var session : SessionManager

    companion object {

        const val TAG = "Pertamina"
        val SUPPORT_FINGERPRINT = true
        val PERMISSIONS_REQUEST_READ_PHONE_STATE = 101

        /*fun setToken(context: Context, myId: String, token: String?) {
            if (myId == "") {
                Log.i(TAG, "setToken: gagal set token")
                return
            }
            val queue = Volley.newRequestQueue(context)
            val url = ActLogin.URL + "set_token"

            val params = HashMap<String?, String?>()
            params["id_user"] = myId
            params["token"] = token

            Log.i(TAG, "setToken $myId  ===  $token")
            val stringRequest = JsonObjectRequest(Request.Method.POST, url, JSONObject(params),
                    Response.Listener { response ->
                        Log.i(TAG, "response dari token " + response.toString())
                        //                            Log.i(TAG, "simpan imei " + pref.getString("imei",""));
                    }, Response.ErrorListener { error -> error.printStackTrace() })//
            queue.add(stringRequest)
        }*/
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_login)

        FirebaseApp.initializeApp(this@ActLogin)
        session = SessionManager(this)

        imeiMatched()

        /*//check if match fingerprint
        version.text = BuildConfig.VERSION_NAME + if (SUPPORT_FINGERPRINT) "-fp" else ""*/

        //set loading animation
        progress.setBackgroundResource(R.drawable.animation_loading)
        anim = progress.background as AnimationDrawable

        //onclick action
        loginBtn.setOnClickListener(this)

    }

    private fun showLoading(show: Boolean) {
        if (show) {
            cover.visibility = View.VISIBLE
            anim.start()
        } else {
            cover.visibility = View.GONE
            anim.stop()
        }
    }

    private fun onLoading(){
        cover.visibility = View.VISIBLE
        anim.start()
    }

    private fun onComplete(){
        cover.visibility = View.GONE
        anim.stop()
    }

    private fun onError(){
        cover.visibility = View.GONE
        anim.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
        anim.stop()
    }

    private fun login(username : String, password : String, imei : String){
        val body = HashMap<String,String>()
        body.put("password", password)
        body.put("imei", imei)
        body.put("username", username)
        Log.e("Body", body.toString())

        disposable = service.login(body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { onLoading() }
                .subscribe(
                    { result -> showResult(result) },
                    { error -> errorKoneksi(error) }
                )

    }

    private fun showResult(result:Login){
        if (result.response == 1) {

            var token = ""
            FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
                token = it.token
                setOtherData(result,  it.token)
                Log.e("Result", token)
            }




        } else {
            showLoading(false)
            tvResponse.text = "Wrong username or password"
        }
    }

    private fun setOtherData(login : Login, token : String){
        val body = HashMap<String,String>()
        body.put("id_user", login.id?:"")
        body.put("token", token)
        body.put("imei", mImei)
        disposable = service.setToken(body)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { onLoading() }
            .subscribe(
                { result -> onSuccessSetToken(login,result) },
                { error -> errorKoneksi(error) }
            )

    }

    private fun onSuccessSetToken(login: Login,result : ResponseBody) {
        session.createLoginSession(
            etUsername.text.toString(),
            etPassword.text.toString(),
            login.id?:"",
            login.name?:"",
            mImei,
            login.role?:"0"
        )
//        session.createLoginSession(
//            etUsername.text.toString(),
//            etPassword.text.toString(),
//            login.id?:"",
//            login.name?:"",
//            mImei,
//            "8"
//        )

        val i = Intent(this, MainActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK )
        startActivity(i)
        finish()
    }

    private fun errorKoneksi(e : Throwable){

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSIONS_REQUEST_READ_PHONE_STATE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val tlpMgr = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                mImei = tlpMgr.deviceId
            } else {
                mImei = ""
            }
        }
    }

    private fun imeiMatched() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(android.Manifest.permission.READ_PHONE_STATE),
                        PERMISSIONS_REQUEST_READ_PHONE_STATE)
            }
        } else {
            val tlpMgr = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            mImei = tlpMgr.deviceId
            Log.i(TAG, "imeiMatched: $mImei")
            //            registerImei();
        }
    }

    override fun onClick(v: View?) {
        val userInput = etUsername.text.toString()
        val passwordInput = etPassword.text.toString()

        if (userInput.isEmpty() || passwordInput.isEmpty()) {
            tvResponse!!.text = "Silahkan lengkapi username dan password"
        }
        else login(userInput,passwordInput,mImei)
    }
}
