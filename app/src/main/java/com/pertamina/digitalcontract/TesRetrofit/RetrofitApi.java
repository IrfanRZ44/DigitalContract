package com.pertamina.digitalcontract.TesRetrofit;


import com.pertamina.digitalcontract.model.ModelLogin;
import com.pertamina.digitalcontract.model.ModelSign;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Url;

/**
 * Created by IrfanRZ on 02/08/2019.
 */

public interface RetrofitApi {
//    String BASE_URL = "http://digitalcontract.mor7.com/";
    String BASE_URL = "http://digitalcontractv3.kabirland.technology/";
    String SIGN_DOC_URL = BASE_URL + "api/v1/";
//    String SIGN_IN_URL = BASE_URL + "api/v1/";
    String SIGN_IN_URL = "http://digitalcontract.mor7.com/api/v1/";


    @Headers("Accept:application/json")
    @POST
    Call<ResponseBody> signDoc(@Url String url, @Header("Content-Type") String contentType);

    @Headers("Content-Type:application/json")
    @POST
    Call<ModelLogin> login(@Url String url);
}
