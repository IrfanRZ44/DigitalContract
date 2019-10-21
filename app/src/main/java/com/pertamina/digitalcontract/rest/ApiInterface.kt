package com.pertamina.digitalcontract.rest

import android.util.Log
import com.pertamina.digitalcontract.Login
import com.pertamina.digitalcontract.ResultContract
import com.pertamina.digitalcontract.Config
import io.reactivex.Observable
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit
import retrofit2.http.Url
import okhttp3.ResponseBody
import retrofit2.http.GET



interface ApiInterface {

    @POST("first_access")
    fun login(@Body isi : Map<String,String>) : Observable<Login>

    @POST("check_imei")
    fun checkImei(@Body isi : Map<String,String>) : Observable<ResponseBody>

    @POST("set_imei")
    fun setImei(@Body isi : Map<String,String>) : Observable<ResponseBody>

    @POST("set_token")
    fun setToken(@Body isi : Map<String,String>) : Observable<ResponseBody>

    @POST("get_contract")
    fun getContract(@Body isi : Map<String,String>) : Observable<ResultContract>

    @POST("set_status")
    fun setReadStatus(@Body isi : Map<String,String>) : Observable<ResponseBody>

    @POST("get_document")
    fun getDocument(@Body isi : Map<String,String>) : Observable<ResponseBody>

    @GET
    fun downloadFIle(@Url fileUrl: String): Observable<ResponseBody>

    /*@POST("set_compile")
    fun sendSign(@Body isi : Map<String,String>) : Observable<ResponseBody>*/

    @POST("get_token")
    fun sendSign(@Body isi : Map<String,String>) : Observable<ResponseBody>

    @FormUrlEncoded
    @POST("oauth/token")
    fun getToken(@Query("client_id") client_id : String,
                 @Query("client_secret") client_secret : String,
                 @Query("grant_type") grant_type : String,
                 @Field("terserah") terserah: String) : Observable<ResponseBody>

    @Multipart
    @POST("api/account/ktp")
    fun uploadFileSigned(@Header("X-Kioser-Token") token : String, @Part image: MultipartBody.Part,
                  @Query("penandatangan") penandatangan: String,
                  @Query("tampilan") tampilan: String,
                  @Query("image") imagePath: String,
                  @Query("linkQR") linkQR: String,
                  @Query("halaman") halaman: String,
                  @Query("yAxis") yAxis: String,
                  @Query("xAxis") xAxis: String,
                  @Query("width") width: String,
                  @Query("height") height: String): Observable<ResponseBody>


    /*@FormUrlEncoded
    @POST("api/v2/entity/sign/request")
    fun getToken(@Header("Authorization") token : String,
                 @Query("penandatangan") penandatangan : String,
                 @Query("client_secret") client_secret : String,
                 @Query("grant_type") grant_type : String) : Observable<ResponseBody>*/

    /*@FormUrlEncoded
    @POST("api/v1/json_sender/first_access")
    fun login(@Field("bos") isi : String) : Observable<Login>*/

    companion object {
        fun create(timeOut : Long = 60): ApiInterface {

            var httpClient : OkHttpClient.Builder = OkHttpClient.Builder()

            httpClient.readTimeout(timeOut,TimeUnit.SECONDS)
            httpClient.connectTimeout(timeOut, TimeUnit.SECONDS)
            httpClient.addInterceptor {
                chain ->
                val original : Request = chain.request()
                val request : Request = original.newBuilder()
//                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .header("Content-Type", "application/json")
                        .method(original.method(),original.body())
                        .build()
                chain.proceed(request)
            }


            val client = httpClient.build()

            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(Config.BASE_URL_API)
                    .client(client)
                    .build()
            Log.e("Retro yg pake", retrofit.baseUrl().toString());
            return retrofit.create(ApiInterface::class.java)
        }

        fun createBSRE(timeOut : Long = 180): ApiInterface {
//            val okHttpClient = UnsafeOkHttpClient.getUnsafeOkHttpClient()

            var okHttpClient : OkHttpClient.Builder = OkHttpClient.Builder()

            val client = okHttpClient.build()

            okHttpClient.readTimeout(timeOut,TimeUnit.SECONDS)
            okHttpClient.connectTimeout(timeOut, TimeUnit.SECONDS)
            okHttpClient.addInterceptor {
                    chain ->
                val original : Request = chain.request()
                val request : Request = original.newBuilder()
//                        .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .method(original.method(),original.body())
                    .build()
                chain.proceed(request)
            }

            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(Config.BASE_URL_BSRE)
                    .client(client)
                    .build()

            return retrofit.create(ApiInterface::class.java)
        }
    }
}