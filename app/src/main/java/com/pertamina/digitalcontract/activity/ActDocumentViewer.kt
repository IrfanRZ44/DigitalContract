package com.pertamina.digitalcontract.activity

import android.Manifest
import android.app.Dialog
import android.app.KeyguardManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.AnimationDrawable
import android.hardware.fingerprint.FingerprintManager
import android.net.Uri
import android.os.*
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.gcacace.signaturepad.views.SignaturePad
import com.google.gson.GsonBuilder
import com.pertamina.digitalcontract.*
import com.pertamina.digitalcontract.TesRetrofit.RetrofitApi
import com.pertamina.digitalcontract.adapter.AdapterExtra
import com.pertamina.digitalcontract.adapter.AdapterReviewer
import com.pertamina.digitalcontract.fingerprint.FingerprintAuthenticationDialogFragment
import com.pertamina.digitalcontract.rest.ApiInterface
import com.pertamina.digitalcontract.util.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.content_act_new2.*
import kotlinx.android.synthetic.main.content_loading.*
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
import java.io.File
import java.io.IOException
import java.net.URL
import java.net.URLEncoder
import java.security.*
import java.security.cert.CertificateException
import java.util.*
import java.util.concurrent.TimeUnit
import javax.crypto.*


class ActDocumentViewer : ActBase(),
        FingerprintAuthenticationDialogFragment.Callback, (Extra, Int) -> Unit {

    private lateinit var keyStore: KeyStore
    private lateinit var keyGenerator: KeyGenerator

    private var dialog: Dialog? = null
    private var mDialogL: Dialog? = null
    private var mDialogR: Dialog? = null
    private var mSignPad: SignaturePad? = null
    private var mRejectionNote: EditText? = null            //catatan jika di reject
    private var mPadSigned = false

    private var mDocTitle = ""//, mRole = "";
    private var mDocPath = ""
    private var mUserRole: Int = 0
    private var mContractId: Int = 0
    private var mContractStatus: Int = 0
    private var mUserId: String? = null//, mWebViewContent;
    private var imeiResponse: Int? = 0
    private var canApprove = false
    private var webviewLoad = false
    private var isDownloaded = false
    private var passPhrase = ""

    var listPdf: MutableList<Extra>? = ArrayList()

    private var fabExpanded = false
    open val serviceBSRE by lazy {
        ApiInterface.createBSRE()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.pertamina.digitalcontract.R.layout.act_document_viewer)

        session = SessionManager(this)
        mDocTitle = intent.getStringExtra("DOC_TITLE")
        mDocPath = intent.getStringExtra("DOC_PATH")
        mContractId = intent.getStringExtra("DOC_ID").toInt()
        mContractStatus = intent.getIntExtra("DOC_STATUS", -1)
        isDownloaded = intent.getBooleanExtra("DOC_DOWNLOAD", false)
        mUserRole = session.role?.toInt() ?: -1
        mUserId = session.id
        canApprove = (mUserRole < 5) or (mUserRole == 7) //3 - finance 4 - legal 5 - officer 6 - vendor 7 - reviewer
        title = mDocTitle

        dialog = Dialog(this, R.style.CustomDialogTheme)
        mDialogL = Dialog(this, R.style.CustomDialogTheme)
        mDialogR = Dialog(this, R.style.CustomDialogTheme)
        mDialogL?.setCancelable(true)
        mDialogR?.setCancelable(true)

        progress.setBackgroundResource(R.drawable.animation_loading)
        anim = progress.background as AnimationDrawable

        //init web viewer
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                webviewLoad = true
                onLoading()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                onComplete()

                if (!webviewLoad) {
                    webviewLoad = false
                    webView.reload()
                    swipeRefresh.isEnabled = true
                } else {
                    swipeRefresh.isEnabled = false
                    fabAction.visibility = View.VISIBLE
                }

                //tampilkan aksi hanya setelah web load
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                onError()

                GlideApp.with(this@ActDocumentViewer).load(R.drawable.no_internet).into(ivRefresh)
                tvRefreshTitle.text = resources.getString(R.string.errorKoneksi)
                tvRefreshDesc.text = resources.getString(R.string.errorKoneksiDetail)
            }
        }

        fabAction.setOnClickListener { view ->
            if (fabExpanded == true) {
                closeSubMenusFab()
            } else {
                openSubMenusFab()
            }
        }

        fabAttachment.setOnClickListener {
            dialogExtra()
        }

        if (mContractId != -1 && mContractStatus == 0) {
            changeStatusContract(1, "", false)
        }

        //Only main FAB is visible in the beginning
        closeSubMenusFab()

        //check kesesuaian imei di akun
        checkActiveAccount()

        btRefresh.setOnClickListener {
            reload()
        }

        swipeRefresh.setOnRefreshListener {
            swipeRefresh.isRefreshing = false
            reload()
        }
    }

    //closes FAB submenus
    private fun closeSubMenusFab() {
        layoutSubFab.visibility = View.GONE
        GlideApp.with(this).load(R.drawable.ic_more_vert_white).into(fabAction)
        fabExpanded = false
    }

    //Opens FAB submenus
    private fun openSubMenusFab() {
        layoutSubFab.visibility = View.VISIBLE
        GlideApp.with(this).load(R.drawable.ic_close_white).into(fabAction)
        fabExpanded = true
    }

    //cek imei dan get pdf
    private fun checkActiveAccount() {
        val body = HashMap<String, String>()
        body["imei"] = session.imei ?: ""
        body["id_user"] = session.id ?: ""

        val body2 = HashMap<String, String>()
        body2["id_contract"] = mContractId.toString()
        body2["id_user"] = session.id ?: ""

        disposable = service.checkImei(body)
                .flatMap { result ->
                    onSuccessCheckImei(result)
                    return@flatMap service.getDocument(body2)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { onLoading() }
                .subscribe(
                        { result -> onSuccessGetDocument(result) },
                        { error -> errorKoneksi(error) }
                )
    }

    //get pdf saja
    private fun reload() {
        val body = HashMap<String, String>()
        body["id_contract"] = mContractId.toString()
        body["id_user"] = session.id ?: ""

        disposable = service.getDocument(body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { onLoading() }
                .subscribe(
                        { result -> onSuccessGetDocument(result) },
                        { error -> errorKoneksi(error) }
                )
    }


    private fun onSuccessCheckImei(result: ResponseBody) {
        val obj = JSONObject(result.string())
        imeiResponse = obj.getInt("response")
    }

    private fun onSuccessGetDocument(result: ResponseBody) {
        if (imeiResponse != 1) {
            Logout()
        } else {
            val objResult = JSONObject(result.string())
            val obj = objResult.getJSONObject("response")
            val objAdditional = objResult.getJSONArray("extra")

            //kalau ada isi object extra tampilkan button extra
            val jumlahExtra = objAdditional.length()
            if (jumlahExtra > 0) {

                for (i in 0..(objAdditional.length() - 1)) {
                    val item = objAdditional.getJSONObject(i)
                    listPdf?.add(Extra(item.getString("NAME"),
                            item.getString("SIZE"),
                            item.getString("TOKEN"),
                            item.getString("PATH")
                    ))
                }

                layoutFabAttachment.visibility = View.VISIBLE

            } else {
                layoutFabAttachment.visibility = View.GONE
            }

            updateDocumentView()

            //jika contract masih pending diperlukan aksi
            if (mContractStatus <= 1) {
                //seleksi tombol antara (approve dan reject) atau (sign)
                if (canApprove) {                                                       //seleksi aksi yang sesuai untuk contract
                    InitApprover()                                                      //selain vendor dan officer bisa approve tombol
                } else {
                    if (mUserRole == 8) {
                        InitChooseViewer()
                    } else {
                        InitSigner()                                                        //vendor dan officer hanya bisa sign
                    }
                }
            }
            //sudah pernah ada aksi sebelumnya jadi tidak ada aksi
            else {
                if (canApprove) {
                    //contract status 2 berarti reject
                    val rejected = mContractStatus == 2
                    if (rejected) {
                        layoutFabApprove.visibility = View.GONE
                        layoutFabReject.visibility = View.VISIBLE
                        layoutFabSign.visibility = View.GONE
                        tvReject.text = "Rejected"
                        tvReject.setTextColor(resources.getColor(R.color.cpb_red))
                    } else {
                        layoutFabApprove.visibility = View.VISIBLE
                        layoutFabReject.visibility = View.GONE
                        layoutFabSign.visibility = View.GONE
                        tvApprove.text = "Approved"
                        tvApprove.setTextColor(resources.getColor(R.color.cpb_green_dark))
                    }
                } else {
                    if (mUserRole == 8) {
                        layoutFabApprove.visibility = View.GONE
                        layoutFabReject.visibility = View.GONE
                        layoutFabSign.visibility = View.VISIBLE
                        tvSign.text = "Choose"
                        tvSign.setTextColor(resources.getColor(R.color.cpb_green_dark))

                        if (isDownloaded) {
                            layoutFabDownload.visibility = View.VISIBLE
                            fabDownload.setOnClickListener {
                                //            checkPDF()
                                checkPermission()
                                if (fabExpanded == true) {
                                    closeSubMenusFab()
                                } else {
                                    openSubMenusFab()
                                }
                            }
                        }
                    } else {
                        layoutFabApprove.visibility = View.GONE
                        layoutFabReject.visibility = View.GONE
                        layoutFabSign.visibility = View.VISIBLE
                        tvSign.text = "Signed"
                        tvSign.setTextColor(resources.getColor(R.color.cpb_green_dark))

                        if (isDownloaded) {
                            layoutFabDownload.visibility = View.VISIBLE
                            fabDownload.setOnClickListener {
                                //            checkPDF()
                                checkPermission()
                                if (fabExpanded == true) {
                                    closeSubMenusFab()
                                } else {
                                    openSubMenusFab()
                                }
                            }
                        }
                    }
                }

                fabSign.isEnabled = false
                fabApprove.isEnabled = false
                fabReject.isEnabled = false
//                tidak bisa di klik karena sudah pernah ada aksi dan hanya 1 button yg muncul
            }
        }
    }

    private fun updateDocumentView() {
        var title = mDocPath

        title = URLEncoder.encode(title, "UTF-8")
        var content = Config.BASE_URL + "export/" + title + ".pdf"
        content = "https://drive.google.com/viewerng/viewer?embedded=true&url=$content"
        Log.e("Url doc", content)

        webView.loadUrl(content)
    }

    //fungsi hanya jika bisa approve
    private fun InitApprover() {
        layoutFabApprove.visibility = View.VISIBLE
        layoutFabReject.visibility = View.VISIBLE
        layoutFabSign.visibility = View.GONE
        mDialogL?.setContentView(R.layout.dialog_universal_warning)

        //3 = approved
        val okBtn = mDialogL?.findViewById<TextView>(R.id.dialog_universal_warning_ok)
        val cancelBtn = mDialogL?.findViewById<TextView>(R.id.dialog_universal_warning_cancel)
        okBtn?.setOnClickListener {
            mDialogL?.dismiss()
            changeStatusContract(3, "", true)
        }
        cancelBtn?.setOnClickListener { mDialogL!!.dismiss() }

        mDialogR?.setContentView(R.layout.dialog_reject)
        val okBtnR = mDialogR?.findViewById<TextView>(R.id.dialog_universal_warning_ok)
        mRejectionNote = mDialogR?.findViewById<EditText>(R.id.rejection)
        mRejectionNote?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                okBtnR?.isEnabled = mRejectionNote?.text?.length ?: 0 > 0
            }
        })


        //2 = reject
        okBtnR?.isEnabled = mRejectionNote?.text?.length ?: 0 > 0
        okBtnR?.setOnClickListener {
            mDialogR?.dismiss()
            changeStatusContract(2, mRejectionNote?.text.toString(), true)
        }
        val cancelBtnR = mDialogR?.findViewById<TextView>(R.id.dialog_universal_warning_cancel)
        cancelBtnR?.setOnClickListener { mDialogR?.dismiss() }

        fabApprove.setOnClickListener {
            mDialogL?.show()
            if (fabExpanded == true) {
                closeSubMenusFab()
            } else {
                openSubMenusFab()
            }
        }
        fabReject.setOnClickListener {
            mDialogR?.show()
            if (fabExpanded == true) {
                closeSubMenusFab()
            } else {
                openSubMenusFab()
            }
        }
    }


    //fungsi hanya untuk yang bisa tanda tangan (officer & vendor)
    private fun InitSigner() {
        layoutFabApprove.visibility = View.GONE
        layoutFabReject.visibility = View.GONE
        layoutFabSign.visibility = View.VISIBLE

        mPadSigned = false
        mDialogL?.setContentView(R.layout.dialog_password)

        val cancelBtn = mDialogL?.findViewById<TextView>(R.id.dialog_universal_warning_cancel)
        val okBtn = mDialogL?.findViewById<TextView>(R.id.dialog_universal_warning_ok)
        val etPass = mDialogL?.findViewById<EditText>(R.id.etPassword)

        okBtn?.setOnClickListener {
            passPhrase = etPass?.text.toString()
            etPass?.text?.clear()
            mDialogL?.dismiss()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                setupKeyStoreAndKeyGenerator()
                val (defaultCipher: javax.crypto.Cipher, cipherNotInvalidated: javax.crypto.Cipher) = setupCiphers()
                setUpFingerPrint(cipherNotInvalidated, defaultCipher)
            } else {
                sendSign()
            }
        }

        cancelBtn?.setOnClickListener {
            mSignPad?.clear()
            mDialogL?.dismiss()
        }

        fabSign.setOnClickListener {
            mDialogL?.show()

            if (fabExpanded == true) {
                closeSubMenusFab()
            } else {
                openSubMenusFab()
            }
        }
    }

    private fun InitChooseViewer() {
        layoutFabApprove.visibility = View.GONE
        layoutFabReject.visibility = View.GONE
        layoutFabSign.visibility = View.VISIBLE

        tvSign.text = resources.getString(R.string.choose_viewer)
        GlideApp.with(this).load(R.drawable.ic_check2_white).into(fabSign)

        mDialogL?.setContentView(R.layout.dialog_choose_reviewer)
        val cancelBtn = mDialogL?.findViewById<TextView>(R.id.dialog_universal_warning_cancel)
        val rcReviewer = mDialogL?.findViewById<RecyclerView>(R.id.rcReviewer)

        val list = ArrayList<String>()

        rcReviewer?.setHasFixedSize(true)
        rcReviewer?.layoutManager = LinearLayoutManager(this)

        list.add("GOOGLE")
        list.add("Microsoft")
        list.add("Aple")
        list.add("Laptop")

        val adapter = AdapterReviewer(list, this)

        rcReviewer?.adapter = adapter

        fabSign.setOnClickListener {
            Log.e("Show", "Dialog")
            mDialogL?.show()
            if (fabExpanded == true) {
                closeSubMenusFab()
            } else {
                openSubMenusFab()
            }
        }

        cancelBtn?.setOnClickListener {
            mSignPad?.clear()
            mDialogL?.dismiss()
        }
    }

    companion object {
        val TAG = "ActDocumentViewer"
        private val SECRET_MESSAGE = "Very secret message"
        private val ANDROID_KEY_STORE = "AndroidKeyStore"
        private val DIALOG_FRAGMENT_TAG = "myFragment"
        private val KEY_NAME_NOT_INVALIDATED = "key_not_invalidated"
        private val KEY_PERMISSION_WRITE_EX = 100
    }

    // 0 : unread, 1 : read, 2 : rejected, 3 : approved
    private fun changeStatusContract(currentStatus: Int, note: String, useLoading: Boolean) {
        val body = HashMap<String, String>()
        body["id_user"] = mUserId.toString()
        body["id_contract"] = mContractId.toString()
        body["user_status"] = currentStatus.toString()
        body["note"] = note

        disposable = service.setReadStatus(body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    if (useLoading) onLoading()
                }
                .subscribe(
                        { result ->
                            if (useLoading) onComplete()

                            val obj = JSONObject(result.string())
                            val response = obj.getInt("response")
                            if (response == 1) {
                                mContractStatus = currentStatus
                                when (currentStatus) {
                                    2 -> dialogSuccess("Document has been rejected", currentStatus)
                                    3 -> dialogSuccess("Document has been approved", currentStatus)
                                }
                            }

                            /*  if (refreshAfterFinish) {

                              }*/
                        },
                        { error ->
                            onComplete()
                            when (currentStatus) {
                                2 -> dialogFailed("Error occured, the document has been failed to reject")
                                3 -> dialogFailed("Error occured, the document has been failed to approve")
                            }
                            Log.d(TAG, "Change Status Error" + error.message)
                        }
                )
    }

    fun dialogSuccess(teks: String, currentStatus: Int) {
        dialog?.setContentView(R.layout.dialog_status)

        val faIcon = dialog?.findViewById<FontAwasomeTextView>(R.id.faIcon)
        val tvTitle = dialog?.findViewById<TextView>(R.id.tvTitle)
        val tvDeskripsi = dialog?.findViewById<TextView>(R.id.tvDeskripsi)
        val btOk = dialog?.findViewById<Button>(R.id.btOk)
        faIcon?.text = resources.getText(R.string.fa_check)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            faIcon?.background = resources.getDrawable(R.drawable.bg_circle_green, null)
            btOk?.background = resources.getDrawable(R.drawable.bg_outer_green, null)
        } else {
            faIcon?.background = resources.getDrawable(R.drawable.bg_circle_green)
            btOk?.background = resources.getDrawable(R.drawable.bg_outer_green)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            btOk?.textColor = resources.getColor(R.color.cpb_green, null)
            tvTitle?.textColor = resources.getColor(R.color.cpb_green, null)
        } else {
            btOk?.textColor = resources.getColor(R.color.cpb_green)
            tvTitle?.textColor = resources.getColor(R.color.cpb_green)
        }
        tvTitle?.text = "Success"
        tvDeskripsi?.text = teks
        btOk?.setOnClickListener {
            dialog?.dismiss()

            if (currentStatus == 2 || currentStatus == 3) {
                val intent = intent
                finish()
                intent.putExtra("DOC_STATUS", currentStatus)
                startActivity(intent)
            }
        }
        dialog?.show()
    }

    fun dialogExtra() {
        dialog?.setContentView(R.layout.dialog_extra)

        var adapter: AdapterExtra?
        val recycler = dialog?.findViewById<RecyclerView>(R.id.recycler)

        //setup recylcer
        val layoutManager = LinearLayoutManager(this)
        recycler?.layoutManager = layoutManager
        recycler?.isNestedScrollingEnabled = false
        adapter = listPdf?.let { it1 -> AdapterExtra(this, it1, this) }
        recycler?.adapter = adapter

        Log.d("bayao", listPdf?.size.toString())

        dialog?.show()
    }

    override fun invoke(data: Extra, pos: Int) {
        dialog?.dismiss()
        if (fabExpanded) {
            closeSubMenusFab()
        } else {
            openSubMenusFab()
        }
        val i = Intent(this, ActAttachment::class.java)
        i.putExtra("DOC_TITLE", data.name)
        startActivity(i)
    }

    fun dialogFailed(teks: String) {
        dialog?.setContentView(R.layout.dialog_status)

        val faIcon = dialog?.findViewById<FontAwasomeTextView>(R.id.faIcon)
        val tvTitle = dialog?.findViewById<TextView>(R.id.tvTitle)
        val tvDeskripsi = dialog?.findViewById<TextView>(R.id.tvDeskripsi)
        val btOk = dialog?.findViewById<Button>(R.id.btOk)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tvTitle?.textColor = resources.getColor(R.color.cpb_red, null)
            btOk?.textColor = resources.getColor(R.color.cpb_red, null)
        } else {
            tvTitle?.textColor = resources.getColor(R.color.cpb_red)
            btOk?.textColor = resources.getColor(R.color.cpb_red)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            faIcon?.background = resources.getDrawable(R.drawable.bg_circle_red, null)
            btOk?.background = resources.getDrawable(R.drawable.bg_outer_red, null)
        } else {
            faIcon?.background = resources.getDrawable(R.drawable.bg_circle_red)
            btOk?.background = resources.getDrawable(R.drawable.bg_outer_red)
        }
        faIcon?.text = resources.getText(R.string.fa_times)
        tvTitle?.text = "Failed"
        tvDeskripsi?.text = teks
        btOk?.setOnClickListener {
            dialog?.dismiss()
        }

        dialog?.show()
    }

    //download pdf setelah semua ter approve
    private fun checkPDF() {
        val pdfFile = File(Environment.getExternalStorageDirectory().absolutePath + "/Digital Contract/"
                + mContractId.toString() + "_" + mDocTitle + ".pdf")

//        if(pdfFile.exists())
        checkPermission()
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    KEY_PERMISSION_WRITE_EX)
        } else downloadPDF()
    }

    private fun downloadPDF() {
        var content = Config.BASE_URL + "export_signed/" + mContractId.toString() + "_" + mDocPath + "_final.pdf"
//        content = "https://drive.google.com/viewerng/viewer?embedded=true&url=$content"
        DownloadFile().execute(content, mContractId.toString() + "_" + mDocPath + "_final.pdf")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            KEY_PERMISSION_WRITE_EX -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    downloadPDF()
                else {
                    showSnackbar("Write External Storage Permission isn't granted")
                }
            }
        }
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    private inner class DownloadFile : AsyncTask<String, Void, Void>() {
        override fun onPreExecute() {
            super.onPreExecute()
            onLoading()
        }

        override fun doInBackground(vararg params: String?): Void? {
            val fileUrl = params[0]
            val fileName = params[1]
            val extStorageDirectory = Environment.getExternalStorageDirectory().toString()
            val folder = File(extStorageDirectory, "Digital Contract")
            folder.mkdir()

            val pdfFile = File(folder, fileName)
            try {
                pdfFile.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            FileDownloader.downloadFile(fileUrl, pdfFile)
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            onComplete()
            viewPDF()
        }
    }

//    fun download2(){
//        FileDownloadS
//    }

    open fun viewPDF() {
        val pdfFile = File(Environment.getExternalStorageDirectory().absolutePath + "/Digital Contract/"
                + mContractId.toString() + "_" + mDocPath + "_final.pdf")

        if (pdfFile.exists()) {
            val pdfIntent = Intent(Intent.ACTION_VIEW)
            pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            val apkURI = FileProvider.getUriForFile(this,
                    this.applicationContext
                            .packageName + ".provider", pdfFile)
            pdfIntent.setDataAndType(apkURI, "application/pdf")
            pdfIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            try {
                startActivity(pdfIntent)
            } catch (e: ActivityNotFoundException) {
                showSnackbar("No Application available to view PDF")
            }
        }
    }

    /**
     * Enables or disables purchase buttons and sets the appropriate click listeners.
     *
     * @param cipherNotInvalidated cipher for the not invalidated purchase button
     * @param defaultCipher the default cipher, used for the purchase button
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun setUpFingerPrint(cipherNotInvalidated: Cipher, defaultCipher: Cipher) {
        val keyguardManager = getSystemService(KeyguardManager::class.java)
        if (!keyguardManager.isKeyguardSecure) {
            //user hasn't set up a fingerprint or lock screen.
            Log.d("balao", "fingerprint hasn't setup yet")
//            purchaseButton.isEnabled = false
            sendSign()
            return
        }

        val fingerprintManager = getSystemService(FingerprintManager::class.java)
        if (!fingerprintManager.hasEnrolledFingerprints()) {
//            purchaseButton.isEnabled = false
            //no fingerprints are registered
            Log.d("balao", "no fingerprint registered")
            sendSign()
            return
        }

        createKey(DEFAULT_KEY_NAME)
        createKey(KEY_NAME_NOT_INVALIDATED, false)

        val fragment = FingerprintAuthenticationDialogFragment()
        fragment.setCryptoObject(FingerprintManager.CryptoObject(defaultCipher))
        fragment.setCallback(this@ActDocumentViewer)

        // Set up the crypto object for later, which will be authenticated by fingerprint usage.
        if (initCipher(defaultCipher, DEFAULT_KEY_NAME)) {
            fragment.setStage(Stage.FINGERPRINT)
        }
        fragment.show(fragmentManager, DIALOG_FRAGMENT_TAG)
    }

    /**
     * Sets up KeyStore and KeyGenerator
     */
    private fun setupKeyStoreAndKeyGenerator() {
        try {
            keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
        } catch (e: KeyStoreException) {
            throw RuntimeException("Failed to get an instance of KeyStore", e)
        }

        try {
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
        } catch (e: Exception) {
            when (e) {
                is NoSuchAlgorithmException,
                is NoSuchProviderException ->
                    throw RuntimeException("Failed to get an instance of KeyGenerator", e)
                else -> throw e
            }
        }
    }

    /**
     * Sets up default cipher and a non-invalidated cipher
     */
    private fun setupCiphers(): Pair<Cipher, Cipher> {
        val defaultCipher: Cipher
        val cipherNotInvalidated: Cipher
        try {
            val cipherString = "${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}"
            defaultCipher = Cipher.getInstance(cipherString)
            cipherNotInvalidated = Cipher.getInstance(cipherString)
        } catch (e: Exception) {
            when (e) {
                is NoSuchAlgorithmException,
                is NoSuchPaddingException ->
                    throw RuntimeException("Failed to get an instance of Cipher", e)
                else -> throw e
            }
        }
        return Pair(defaultCipher, cipherNotInvalidated)
    }

    /**
     * Initialize the [Cipher] instance with the created key in the [createKey] method.
     *
     * @param keyName the key name to init the cipher
     * @return `true` if initialization succeeded, `false` if the lock screen has been disabled or
     * reset after key generation, or if a fingerprint was enrolled after key generation.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun initCipher(cipher: Cipher, keyName: String): Boolean {
        try {
            keyStore.load(null)
            cipher.init(Cipher.ENCRYPT_MODE, keyStore.getKey(keyName, null) as SecretKey)
            return true
        } catch (e: Exception) {
            when (e) {
                is KeyPermanentlyInvalidatedException -> return false
                is KeyStoreException,
                is CertificateException,
                is UnrecoverableKeyException,
                is IOException,
                is NoSuchAlgorithmException,
                is InvalidKeyException -> throw RuntimeException("Failed to init Cipher", e)
                else -> throw e
            }
        }
    }

    /**
     * Proceed with the purchase operation
     *
     * @param withFingerprint `true` if the purchase was made by using a fingerprint
     * @param crypto the Crypto object
     */
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onPurchased(withFingerprint: Boolean, crypto: FingerprintManager.CryptoObject?) {
        if (withFingerprint) {
            // If the user authenticated with fingerprint, verify using cryptography and then show
            // the confirmation message.
            if (crypto != null) {
                tryEncrypt(crypto.cipher)
//                Log.d(TAG,"berhasil "+crypto.cipher.toString())
            }
        } else {
            // Authentication happened with backup password. Just show the confirmation message.
            showConfirmation()
        }
    }

    // Show confirmation message. Also show crypto information if fingerprint was used.
    private fun showConfirmation(encrypted: ByteArray? = null) {
        if (encrypted != null) {
//            Log.d(TAG,"berhasil 1 "+encrypted.toString())
//                Base64.encodeToString(encrypted, 0 /* flags */)
            sendSign()
        }
    }

    private fun sendSign(){
        onLoading()
        postSignDoc()

//        disposable = serviceBSRE.sendSign(body)
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .doOnSubscribe {
//                Log.e("body doing ", body.toString())
//
//            }
//            .subscribe(
//                { result ->
//                    onComplete()
//
//                    val obj = JSONObject(result.string())
//
//                    try {
//                        val response = obj.getInt("response")?:0
//                        if(response==1)
//                            dialogSuccess("Sign Success, the document has been success to signed",3)
//                        else
//                            dialogFailed(response.toString())
//                    }
//                    catch (e : Exception) {
//                        val message = obj.getString("message")?:""
//                        dialogFailed(message)
//                    }
//                },
//                { error ->
//                    dialogFailed(error.message.toString())
//                }
//            )
    }

    private fun postSignDoc() {
        val body = HashMap<String, String>()
        body["id_user"] = mUserId.toString()
        body["id_contract"] = mContractId.toString()
        body["passphrase"] = passPhrase
////        if (mUserRole == 5) body["penandatangan"] =  "112233455667788"
////        else body["penandatangan"] =  "112234455667788"
        body["contract_title"] = mDocTitle
        body["contract_path"] = mDocPath
        Log.e("Body", body.toString());

        val gson = GsonBuilder().setLenient().create()
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(100, TimeUnit.SECONDS)
            .writeTimeout(100, TimeUnit.SECONDS)
            .readTimeout(100, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(RetrofitApi.SIGN_DOC_JSON_SENDER2)
            .addConverterFactory(
                GsonConverterFactory.create(gson)
            )
            .client(okHttpClient)
        val api = retrofit.build().create(RetrofitApi::class.java)

//        val url = "Json_sender2/get_token?id_user=" + mUserId.toString() + "&id_contract=" + mContractId.toString() + "&passphrase=" + passPhrase + "&contract_title=" + mDocTitle + "&contract_path=" + mDocPath

        val call = api.signDoc(body,
            "application/json")

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val result = response.body()?.string()

                if (result == null){
                    dialogFailed("Error occured, the document has been failed to signed")
                }
                else{
                    if (result.contains("Passphrase anda salah")){
                        dialogFailed("Passphrase anda salah")
                    }
                    else if(result.contains("response")){
                        if (result.contains("1")){
                            dialogSuccess("Sign Success, the document has been success to signed",3)
                        }
                        else{
                            dialogFailed(result)
                        }
                    }
                    else{
                        dialogFailed(result)
                    }
                }
                onComplete()
                Log.e("Result", result);

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                onComplete()
                dialogFailed(t.message.toString())
            }
        })
    }

    /**
     * Tries to encrypt some data with the generated key from [createKey]. This only works if the
     * user just authenticated via fingerprint.
     */
    private fun tryEncrypt(cipher: Cipher) {
        try {
            Log.e("Finger print", cipher.toString());
            showConfirmation(cipher.doFinal(SECRET_MESSAGE.toByteArray()))
        } catch (e: Exception) {
            when (e) {
                is BadPaddingException,
                is IllegalBlockSizeException -> {
                    Toast.makeText(this, "Failed to encrypt the data with the generated key. "
                            + "Retry the purchase", Toast.LENGTH_LONG).show()
                    Log.e(TAG, "Failed to encrypt the data with the generated key. ${e.message}")
                }
                else -> throw e
            }
        }
    }

    /**
     * Creates a symmetric key in the Android Key Store which can only be used after the user has
     * authenticated with a fingerprint.
     *
     * @param keyName the name of the key to be created
     * @param invalidatedByBiometricEnrollment if `false` is passed, the created key will not be
     * invalidated even if a new fingerprint is enrolled. The default value is `true` - the key will
     * be invalidated if a new fingerprint is enrolled.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    override fun createKey(keyName: String, invalidatedByBiometricEnrollment: Boolean) {
        // The enrolling flow for fingerprint. This is where you ask the user to set up fingerprint
        // for your flow. Use of keys is necessary if you need to know if the set of enrolled
        // fingerprints has changed.
        try {
            keyStore.load(null)

            val keyProperties = KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            val builder = KeyGenParameterSpec.Builder(keyName, keyProperties)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
//                .setInvalidatedByBiometricEnrollment(invalidatedByBiometricEnrollment)

            keyGenerator.run {
                init(builder.build())
                generateKey()
            }
        } catch (e: Exception) {
            when (e) {
                is NoSuchAlgorithmException,
                is InvalidAlgorithmParameterException,
                is CertificateException,
                is IOException -> throw RuntimeException(e)
                else -> throw e
            }
        }
    }

    override fun errorFingerprint() {
        Log.d("balao", "fingerprint auth error")
    }

    override fun noFingerprint() {
        sendSign()
    }
}
