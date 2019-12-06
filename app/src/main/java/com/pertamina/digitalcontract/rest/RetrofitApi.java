package com.pertamina.digitalcontract.rest;


import com.pertamina.digitalcontract.model.ModelLogin;
import com.pertamina.digitalcontract.model.ModelReviewer;
import com.pertamina.digitalcontract.model.ModelSign;

import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Url;

/**
 * Created by IrfanRZ on 02/08/2019.
 */

public interface RetrofitApi {
    String BASE_URL = "https://digitalcontractv3.mor7.com/";
    String JSON_SENDER1 = BASE_URL + "api/v1/Json_sender/";
    String JSON_SENDER2 = BASE_URL + "api/v1/Json_sender2/";

    @Headers("Accept:application/json")
    @POST("get_token")
    Call<ResponseBody> signDoc(@Body Map<String,String> input, @Header("Content-Type") String contentType);

    @Headers("Accept:application/json")
    @POST("getReviewer")
    Call<ArrayList<ModelReviewer>> getReviewer(@Body Map<String,String> input, @Header("Content-Type") String contentType);

    @Headers("Accept:application/json")
    @POST("setReviewer")
    Call<ResponseBody> setReviewer(@Body Map<String,String> input, @Header("Content-Type") String contentType);

    @Headers("Accept:application/json")
    @POST("set_status")
    Call<ResponseBody> set_status(@Body Map<String,String> input, @Header("Content-Type") String contentType);

    @Headers("Accept:application/json")
    @POST("cekContractReviewer")
    Call<ResponseBody> cekContractReviewer(@Body Map<String,String> input, @Header("Content-Type") String contentType);

    @Headers("Accept:application/json")
    @POST("publishContract")
    Call<ResponseBody> publishContract(@Body Map<String,String> input, @Header("Content-Type") String contentType);

    @Headers("Accept:application/json")
    @POST("getLogContract")
    Call<ResponseBody> getLogContract(@Body Map<String,String> input, @Header("Content-Type") String contentType);
}
