package com.pertamina.digitalcontract.TesRetrofit

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.POST

interface API{
    @POST("get_token")
    fun sign(isi : String) : Observable<ResponseBody>
}