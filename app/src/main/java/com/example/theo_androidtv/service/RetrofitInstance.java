package com.example.theo_androidtv.service;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.CertificateException;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitInstance {

    private static Retrofit retrofit = null;

    public static RestApiService getApiService() {
        if (retrofit == null) {

            //https://play.instel.site:4433/

            retrofit = new Retrofit
                    .Builder()
                    .baseUrl("https://mago.beenet.com.sv:4433/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

        }

        return retrofit.create(RestApiService.class);

    }


}
