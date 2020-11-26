package com.example.theo_androidtv.ui;


import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import com.example.theo_androidtv.R;
import com.example.theo_androidtv.model.LoginResponse;
import com.example.theo_androidtv.service.RestApiService;
import com.example.theo_androidtv.service.RetrofitInstance;
import com.muddzdev.styleabletoast.StyleableToast;

import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends Activity {

    EditText user,pass;
    public String username, password,data,auth,timestamp;
    long x;
    Button btnLogin;

    static final String APP_ID = "1";
    static final String BOX_ID = "123456789";
    static final String ENCRYPTION_KEY = "1234567890123456";
    static final String COMPANY_ID = "1";
    static final String APP_NAME = "BeenetPlay";
    static final String API_VERSION = "";
    static final String APPVERSION = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Referenciando variables con Formulario
        user = findViewById(R.id.txtUser);
        pass = findViewById(R.id.txtPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {

                //Recuperando Valores de Formulario
                username = user.getText().toString();
                password = pass.getText().toString();

                x = System.currentTimeMillis();
                timestamp = Long.toString(x);

                //Concatenando Variable a encriptar
                data ="username=" + username +
                        ";password=" + password +
                        ";appid=1" +
                        ";boxid=123456789" +
                        ";timestamp=" + timestamp;

                //Encriptar data
                try {
                    auth = encrypt(data,ENCRYPTION_KEY);

                } catch (UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException e) {
                    e.printStackTrace();
                }

                //Llamando a la API para Login
                postDataLogin(auth);

            }
        });
    }

    /**
     * Funcion que envia datos a la API, recibe como paramentro valor encriptado
     * */
    public void postDataLogin(final String auth){

        //Call<LoginResponse> call = RetrofitClient.getInstance().getApi()
        //      .userLogin(COMPANY_ID,APP_ID,APP_NAME,API_VERSION,APPVERSION,auth);

        RestApiService apiService = RetrofitInstance.getApiService();

        Call<LoginResponse> call = apiService.userLogin(COMPANY_ID,APP_ID,APP_NAME,API_VERSION,APPVERSION,auth);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                LoginResponse loginResponse = response.body();
                if (loginResponse.getStatus_code() == 200){

                    //System.out.println("ERROR: " + loginResponse.getError_description());

                   new StyleableToast
                            .Builder(getApplicationContext())
                            .text("SESION INICIADA")
                            .textSize(16)
                            .textColor(Color.WHITE)
                            .iconStart(R.drawable.ic_login_user)
                            .backgroundColor(Color.rgb(255,112,0))
                            .show();

                    //Llamada siguiente ventana PlayerActivity.class y Enviando parametro auth
                    Intent intent = new Intent (getApplicationContext(), PlayerActivity.class);
                    Bundle myBundle = new Bundle();
                    myBundle.putString("auth",auth);
                    intent.putExtras(myBundle);
                    startActivityForResult(intent, 0);

                }else{
                   new StyleableToast
                            .Builder(getApplicationContext())
                            .text(loginResponse.getError_description())
                            .textColor(Color.WHITE)
                            .iconStart(R.drawable.ic_error)
                            .backgroundColor(Color.rgb(255,112,0))
                            .show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
               // Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
                new StyleableToast
                        .Builder(getApplicationContext())
                        .text(t.getMessage())
                        .textColor(Color.WHITE)
                        .iconStart(R.drawable.ic_error)
                        .backgroundColor(Color.rgb(255,112,0))
                        .show();
            }
        });
    }

    /**
     * Funcion de Encriptamiento AES/CBS, recibe como parametro: Cadena y Llave de Encriptamiento
     * */
    public String encrypt(String dato, String key) throws UnsupportedEncodingException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] raw = key.getBytes();
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        IvParameterSpec iv = new IvParameterSpec(key.getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
        byte[] encrypted = cipher.doFinal(dato.getBytes("utf-8"));

        /**
         *  java.util.Base64 solo esta disponible en Android a partir de API 26
         * */
        //return Base64.getEncoder().encodeToString(encrypted);

        /**
         *  Android.util.Base64 es compatible con la mayoria de niveles de API
         **/
        return Base64.encodeToString(encrypted,Base64.DEFAULT);
    }

}
