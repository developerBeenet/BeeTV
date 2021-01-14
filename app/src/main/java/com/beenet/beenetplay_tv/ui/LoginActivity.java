package com.beenet.beenetplay_tv.ui;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import com.beenet.beenetplay_tv.R;
import com.beenet.beenetplay_tv.model.LoginResponse;
import com.beenet.beenetplay_tv.service.RestApiService;
import com.beenet.beenetplay_tv.service.RetrofitInstance;
import com.muddzdev.styleabletoast.StyleableToast;

import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/*
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;
*/

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends Activity {

    EditText user,pass;
    CheckBox remember;
    public String username, password,data,auth,timestamp;
    String encriptar = null;
    long x;
    public boolean isChecked, isRemember = false;
    Button btnLogin;

    SharedPreferences sharedPreferences;

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
        remember = findViewById(R.id.cb_Remember);
        btnLogin = findViewById(R.id.btnLogin);

        //SharedPreferences
        sharedPreferences = getSharedPreferences("Preferencias", Context.MODE_PRIVATE);

        //Preparando para encriptar Datos de SharedPreferences
        /*
        try {
            //MasterKeys de Android para crear varios Strings encriptado con el formato AES256_GCM_SPEC, luego lo coloco dentro la variable sharedPreferences
            encriptar = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


         try {
            sharedPreferences = EncryptedSharedPreferences.create(
                    "SHARED_PREF",
                    encriptar,
                    this,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
         */

        //Shared Preference para guardar Credenciales de inicio de sesion; MODE PRIVATE solo la app tiene acceso a los dato
        //Obtener valor de Checkbox de 'Recordar'
        isRemember = sharedPreferences.getBoolean("CHECKBOX",false);

        //Si Checkbox es true; setea los campos de formulario
        if(isRemember){
            user.setText(sharedPreferences.getString("USERNAME",""));
            pass.setText(sharedPreferences.getString("PASSWORD",""));
            remember.setChecked(isRemember);
        }


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {

                //Recuperando Valores de Formulario
                username = user.getText().toString();
                password = pass.getText().toString();
                isChecked = remember.isChecked();
                //isChecked = false;

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
                postDataLogin(auth,isChecked);
            }
        });
    }

    /**
     * Funcion que envia datos a la API, recibe como paramentro valor encriptado
     * */
    public void postDataLogin(final String auth,boolean isChecked){

        //Call<LoginResponse> call = RetrofitClient.getInstance().getApi()
        //      .userLogin(COMPANY_ID,APP_ID,APP_NAME,API_VERSION,APPVERSION,auth);

        RestApiService apiService = RetrofitInstance.getApiService();

        Call<LoginResponse> call = apiService.userLogin(COMPANY_ID,APP_ID,APP_NAME,API_VERSION,APPVERSION,auth);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                LoginResponse loginResponse = response.body();
                if (loginResponse.getStatus_code() == 200){

                    if (isChecked){

                        // Guardando los valores en Archivo SharedPreference
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("USERNAME",username);
                        editor.putString("PASSWORD",password);
                        editor.putBoolean("CHECKBOX",isChecked);
                        editor.apply();

                        //Toast.makeText(getApplicationContext(),"DATOS GUARDADOS EN MEMORIA",Toast.LENGTH_LONG).show();

                    }else{
                        //Toast.makeText(getApplicationContext(),"Se eliminara Credenciales.",Toast.LENGTH_LONG).show();
                        //Borrar archivo de sharedPreferences
                        SharedPreferences.Editor editor = getSharedPreferences("SHARED_PREF",MODE_PRIVATE).edit();
                        editor.clear();
                        editor.apply();

                    }

                    //Mensaje de Exito de inicio de Sesion
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
                    myBundle.putBoolean("check",isRemember);
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
