package com.beenet.theo_androidtv.service;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitInstance {

    private static Retrofit retrofit = null;

    public static RestApiService getApiService() {
        if (retrofit == null) {

            //https://play.instel.site:4433/
            //https://mago.beenet.com.sv:4433/

            retrofit = new Retrofit
                    .Builder()
                    .baseUrl("https://cms.beenet.com.sv:4433/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

        }

        return retrofit.create(RestApiService.class);

    }


}
