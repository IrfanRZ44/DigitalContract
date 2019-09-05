package com.pertamina.digitalcontract.activity

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.AnimationDrawable
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.pertamina.digitalcontract.Config
import com.pertamina.digitalcontract.GlideApp
import com.pertamina.digitalcontract.R
import com.pertamina.digitalcontract.util.FileDownloader
import kotlinx.android.synthetic.main.content_attachment.*
import kotlinx.android.synthetic.main.content_loading.*
import java.io.File
import java.io.IOException

class ActAttachment : ActBase(){

    private var mDocTitle = ""//, mRole = "";
    private var webviewLoad = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_attachment)

        progress.setBackgroundResource(R.drawable.animation_loading)
        anim = progress.background as AnimationDrawable
        mDocTitle           = intent.getStringExtra("DOC_TITLE")
        title               = mDocTitle
        var content = Config.BASE_URL+"upload/"+mDocTitle
        content = "https://drive.google.com/viewerng/viewer?embedded=true&url=$content"

        onLoading()

        //init web viewer
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient(){
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                webviewLoad = true
                onLoading()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                onComplete()

                if(!webviewLoad) {
                    webviewLoad = false
                    webView.reload()
                    swipeRefresh.isEnabled = true
                    fabDownload.visibility = View.GONE
                }
                else {
                    swipeRefresh.isEnabled = false
                    fabDownload.visibility = View.VISIBLE
                }
                //tampilkan aksi hanya setelah web load
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                onError()

                GlideApp.with(this@ActAttachment).load(R.drawable.no_internet).into(ivRefresh)
                tvRefreshTitle.text = resources.getString(R.string.errorKoneksi)
                tvRefreshDesc.text = resources.getString(R.string.errorKoneksiDetail)
            }
        }

        webView.loadUrl(content)

        btRefresh.setOnClickListener {
            webView.loadUrl(content)
        }

        swipeRefresh.setOnRefreshListener {
            swipeRefresh.isRefreshing = false
            webView.loadUrl(content)
        }

        fabDownload.setOnClickListener {
            checkPermission()
        }
    }

    private fun checkPermission(){
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                KEY_PERMISSION_WRITE_EX)
        }
        else downloadPDF()
    }

    private fun downloadPDF(){
        var content = Config.BASE_URL+"upload/"+mDocTitle
//        content = "https://drive.google.com/viewerng/viewer?embedded=true&url=$content"
        DownloadFile().execute(content, mDocTitle)
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
            val folder = File(extStorageDirectory,"Digital Contract")
            folder.mkdir()

            val pdfFile = File(folder,fileName)
            try {
                pdfFile.createNewFile()
            }
            catch (e: IOException){
                e.printStackTrace()
            }
            FileDownloader.downloadFile(fileUrl,pdfFile)
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            onComplete()
            viewPDF()
        }
    }

    open fun viewPDF(){
        val pdfFile = File(Environment.getExternalStorageDirectory().absolutePath+"/Digital Contract/"
                +mDocTitle)

        if(pdfFile.exists()){
            val pdfIntent = Intent(Intent.ACTION_VIEW)
            pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            val apkURI = FileProvider.getUriForFile(this,
                this.applicationContext
                    .packageName+".provider",pdfFile)
            pdfIntent.setDataAndType(apkURI,"application/pdf")
            pdfIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            try {
                startActivity(pdfIntent)
            }
            catch (e: ActivityNotFoundException){
                showSnackbar("No Application available to view PDF")
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            KEY_PERMISSION_WRITE_EX -> {
                if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    downloadPDF()
                else{
                    showSnackbar("Write External Storage Permission isn't granted")
                }
            }
        }
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    companion object {
        val TAG = "ActAttachment"
        private val KEY_PERMISSION_WRITE_EX = 100
    }
}
