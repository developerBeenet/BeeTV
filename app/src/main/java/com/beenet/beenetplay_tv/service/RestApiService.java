package com.beenet.beenetplay_tv.service;

import com.beenet.beenetplay_tv.model.CategoryResponse;
import com.beenet.beenetplay_tv.model.ChannelResponse;
import com.beenet.beenetplay_tv.model.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface RestApiService {

    /**
     * Llamadas a los metodos API.
     * El cliente para hacer Peticiones es Retrofit y se encuentra inicializado en api/RetrofitClient
     */

    @FormUrlEncoded
    @POST("apiv2/credentials/login")
    Call<LoginResponse> userLogin(
            @Field("company_id") String company_id,
            @Field("appid") String appid,
            @Field("app_name") String app_name,
            @Field("api_version") String api_version,
            @Field("appversion") String appversion,
            @Field("auth") String auth
    );

    @FormUrlEncoded
    @POST("apiv2/credentials/logout")
    Call<LoginResponse> userLogout(
            @Field("auth") String auth
    );

    @FormUrlEncoded
    @POST("apiv2/channels/genre")
    Call<CategoryResponse> allCategories(
            @Field("auth") String auth
    );

    @FormUrlEncoded
    @POST("apiv2/channels/list")
    Call<ChannelResponse> allChannels(
            @Field("auth") String auth
    );


}
