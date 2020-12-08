package com.beenet.beenetplay_tv.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import com.beenet.beenetplay_tv.R;

import com.beenet.beenetplay_tv.model.Category;
import com.beenet.beenetplay_tv.model.Channel;
import com.beenet.beenetplay_tv.model.LoginResponse;
import com.beenet.beenetplay_tv.service.RestApiService;
import com.beenet.beenetplay_tv.service.RetrofitInstance;
import com.beenet.beenetplay_tv.viewmodel.PlayerViewModel;
import com.muddzdev.styleabletoast.StyleableToast;
import com.theoplayer.android.api.THEOplayerView;
import com.theoplayer.android.api.abr.AbrStrategyConfiguration;
import com.theoplayer.android.api.abr.AbrStrategyType;
import com.theoplayer.android.api.player.Player;
import com.theoplayer.android.api.source.SourceDescription;
import com.theoplayer.android.api.source.SourceType;
import com.theoplayer.android.api.source.TypedSource;
import com.theoplayer.android.api.source.addescription.THEOplayerAdDescription;

import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.theoplayer.android.api.source.SourceDescription.Builder.sourceDescription;

public class PlayerActivity extends AppCompatActivity {


    DrawerLayout drawerLayout; //Permite el despliegue de menu lateral en conjunto con mainLayout y menuLateral
    ConstraintLayout mainLayout, menuLateral;

    /* Clock */
    TextView tHora;
    int hora=0, minuto =0, segundo = 0, am_pm;
    Intent i;
    Thread iniReloj = null;
    Runnable r;
    public boolean isUpdate = false, check = false;

    String sec, min, hor, marca;
    String curTime;
    /* */

    /* ProgressBar */
    /*
    private ProgressBar progressBar;
    private int progressStatus = 0;
    private int duration; //Suponiendo en seg
    private Handler handler = new Handler();
     */

    /* Categories */
    Spinner sp_categorias;
    List<Category> cat = null;  //Lista de Objetos Categoria

    /* Channels */
    RecyclerView mRecyclerView;
    SwipeRefreshLayout swipeRefresh;
    ChannelAdapter mChannelAdapter;

    public PlayerViewModel playerViewModel;
    THEOplayerView theoplayerView;
    Player player;

    public String stream = " ";
    String auth, id_category = "1";
    public boolean ads = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        //Recuperando valor enviado proveniente de LoginActivityTV
        Bundle myBundle = this.getIntent().getExtras();
        if (myBundle != null){
            auth = myBundle.getString("auth");
            check = myBundle.getBoolean("check");
        }

        /** Inicializando de elementos Visuales **/
        initializationViews();
        playerViewModel = ViewModelProviders.of(this).get(PlayerViewModel.class);

        /** Metodos que controlan la hora**/
        r = new RefreshClock();
        iniReloj= new Thread(r);
        iniReloj.start();

        /** Progress Bar animation**/
        /*
        duration = 200;
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(duration);
        // Start long running operation in a background thread
        new Thread(new Runnable() {
            public void run() {
                while (progressStatus < duration) {
                    progressStatus += 1;
                    // Update the progress bar and display the
                    //current value in the text view
                    handler.post(new Runnable() {
                        public void run() {
                            progressBar.setProgress(progressStatus);
                        }
                    });
                    try {
                        // Sleep for 200 milliseconds.
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        /* */

        /** Poblando Spinner **/
        getCategory();

        /** Poblando RecyclerView **/
        getPopularChannel(id_category);

        // lambda expression
        swipeRefresh.setOnRefreshListener(() -> {
            getPopularChannel(id_category);
        });

        // Setting the player reference for future use
        player = theoplayerView.getPlayer();

        //Configure the player
        configureTheoPlayer(stream);

    }

    private void configureTheoPlayer(String url) {
        // Creating a TypedSource builder that defines the location of a single stream source
        // and has Widevine DRM parameters applied.
        // TypedSource.Builder typedSource = typedSource(getString(R.string.defaultSourceUrl));
        //   .drm(drmConfiguration.build());

        if(url == " "){
            ads = true;
            url = "https://xcdrsbsv-b.beenet.com.sv/abr_local2/abr_local2_out/playlist.m3u8";
        }

        /* CARGA CANAL */
        TypedSource typedSource = TypedSource.Builder
                .typedSource()
                .src(url)
                .type(SourceType.HLS)
                .build();

        /* ABR */
        AbrStrategyConfiguration abrConfig = AbrStrategyConfiguration.Builder
                .abrStrategyConfiguration()
                .setType(AbrStrategyType.PERFORMANCE)
                .build();

        player.getAbr().setAbrStrategy(abrConfig);
        player.getAbr().setTargetBuffer(4);

        
        // Creating a SourceDescription builder that contains the settings to be applied as a new
        // THEOplayer source.
        SourceDescription.Builder sourceDescription = sourceDescription(typedSource);

        //Agrega anuncio una vez al iniciar sesion
        if(ads == true){
            sourceDescription
                    .poster(getString(R.string.poster))
                    .ads(
                        // Inserting linear pre-roll ad defined with VAST standard.
                        THEOplayerAdDescription.Builder
                            .adDescription(getString(R.string.ads_video))
                            .timeOffset("start")
                            .build()
                    );
            ads = false;
        }

        //Setting the source to the player
        player.setSource(sourceDescription.build());

        //Setting the Autoplay to true
        player.setAutoplay(true);

        //Playing the source in the FullScreen Landscape mode
        theoplayerView.getSettings().setFullScreenOrientationCoupled(true);
    }

    // Overriding default events
    @Override
    protected void onPause() {
        super.onPause();
        theoplayerView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        theoplayerView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        theoplayerView.onDestroy();
    }

    private void initializationViews() {

        theoplayerView = findViewById(R.id.theoPlayerView);

        //Selector de Categorias
        sp_categorias = (Spinner) findViewById(R.id.sp_canales);

        //Lista de Canales
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        mRecyclerView = (RecyclerView) findViewById(R.id.channelRecyclerView);

        //Contenedor de drawer
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);

        //Contenedor MainLayout y MenuLateral
        mainLayout = (ConstraintLayout) findViewById(R.id.mainLayout);
        menuLateral = (ConstraintLayout) findViewById(R.id.menuLateral);

        //Textview Reloj
        tHora = findViewById(R.id.txt_clock);

    }

    public void getPopularChannel(String filter) {
        swipeRefresh.setRefreshing(true);
        playerViewModel.getAllChannel(auth,filter).observe(this, new Observer<List<Channel>>() {
            @Override
            public void onChanged(List<Channel> channels) {
                swipeRefresh.setRefreshing(false);
                prepareRecyclerView(channels);
            }
        });

    }

    private void prepareRecyclerView(List<Channel> channelList) {

        mChannelAdapter = new ChannelAdapter(channelList,cat);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        } else {
            mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        }
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        /* seleccionar elemento de recyclerview */
        mChannelAdapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stream = channelList.get(mRecyclerView.getChildAdapterPosition(v)).getStream_url();
                configureTheoPlayer(channelList.get(mRecyclerView.getChildAdapterPosition(v)).getStream_url());

            }
        });

        mRecyclerView.setAdapter(mChannelAdapter);
        mChannelAdapter.notifyDataSetChanged();

    }

    public void getCategory(){
        playerViewModel.getAllCategory(auth).observe(this,new Observer<List<Category>>(){

            @Override
            public void onChanged(List<Category> categories) {
                prepareSpinnerCategories(categories);
            }
        });
    }

    private void prepareSpinnerCategories(List<Category> categoryList){

        cat = categoryList;
        categoryList.set(cat.size()-1,new Category(0,"Todos"));

        ArrayAdapter<Category> arrayAdapter = new ArrayAdapter<>(getApplicationContext(),
                R.layout.category_item, categoryList);

        sp_categorias.setAdapter(arrayAdapter);

        sp_categorias.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                id_category = Integer.toString(categoryList.get(position).getId());
                getPopularChannel(id_category);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /** Metodo que captura acciones del D-PAD **/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        boolean handled = false;
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                handled = true;
                openList();
                break;

            case KeyEvent.KEYCODE_DPAD_RIGHT:
                handled = true;
                closeList();
                break;

            case KeyEvent.KEYCODE_BACK:
                handled = true;
                //Llamar a la funcion cerrar sesion
                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogCustom);
                builder.setMessage("Quieres salir de la Aplicacion?");
                builder.setTitle("Salir");
                builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Logout(auth);

                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

                break;
        }

        return handled || super.onKeyDown(keyCode, event);
    }

    /** Funcion Despliega la vista Lateral **/
    public void openList(){
        if (!drawerLayout.isDrawerOpen(menuLateral)){
            drawerLayout.openDrawer(menuLateral);
        }
    }

    /** Funcion Oculta lista lateral **/
    public void closeList(){
        if (drawerLayout.isDrawerOpen(menuLateral)){
            drawerLayout.closeDrawer(menuLateral);
        }
    }

    /**
     *  Cerrar Sesion en API
     *  parametro: Auth contiene la clave utilizada para transacciones con la API
     * */
    public void Logout(final String auth){

        RestApiService apiService = RetrofitInstance.getApiService();

        Call<LoginResponse> call = apiService.userLogout(auth);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                LoginResponse loginResponse = response.body();
                if (loginResponse.getStatus_code() == 200){

                    new StyleableToast
                            .Builder(getApplicationContext())
                            .text("SESION FINALIZADA")
                            .textSize(16)
                            .textColor(Color.WHITE)
                            .iconStart(R.drawable.ic_salir)
                            .backgroundColor(Color.rgb(255,112,0))
                            .show();

                    finish();

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


    /** ---------  Metodos para Reloj  ---------- **/
    /**
     Esta clase es la encargada de estar de actualizar cada 1000 milisegundos es decir, un segudo. **/
    class RefreshClock implements Runnable{
        // @Override
        @SuppressWarnings("unused")
        public void run() {
            while(!Thread.currentThread().isInterrupted()){
                try {
                    initClock();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }catch(Exception e){
                }
            }
        }
    }

    /**
     Metodo limpia el layout cada segundo, un if es el que identifica si se ha actualizado la hora
     o seguir  mostrando la hora actual
     */
    private void initClock() {
        runOnUiThread(new Runnable() {
            public void run() {
                try{
                    if(isUpdate){
                        settingNewClock();
                    } else {
                        updateTime();
                    }
                    //curtime = 0 + hora + 0 + minuto + 0 + segundo + " " + AM/PM
                    curTime = hor + hora + min + minuto + sec + segundo + " "+marca;
                    tHora.setText(curTime);

                }catch (Exception e) {}
            }
        });
    }

    /**
     Este metodo es el encargado de parsear la hora de una
     manera que al llegar a 24:59:59 esta retome los valores de 00:00:00.
     */
    private void settingNewClock(){
        segundo +=1;

        setZeroClock();

        if(segundo >=0 & segundo <=59){

        }else {
            segundo = 0;
            minuto +=1;
        }
        if(minuto >=0 & minuto <=59){

        }else{
            minuto = 0;
            hora +=1;
        }
        /*
        if(hora >= 0 & hora <= 12){
            System.out.println("******************************* ******************* SettingNewClock Hora: "+ hora);
        }else{
            hora = 12;
        }
         */

    }

    /**
     Este es el metodo inicial del reloj, a partir de el es que se muestra la hora
     cada segundo es la encargada Java.Util.Calendar
     */
    private void updateTime(){

        Calendar c = Calendar.getInstance();
        //hora = c.get(Calendar.HOUR_OF_DAY);
        hora = c.get(Calendar.HOUR);
        minuto = c.get(Calendar.MINUTE);
        segundo = c.get(Calendar.SECOND);
        am_pm = c.get(Calendar.AM_PM);

        /**
         * si la marca es PM y hora es 0 que lo cambie a 12;
         * funcional para la manana 11:59:59 AM -> 00:00:00 PM lo pasa a 12:00:00 PM
         */
        if(am_pm == 1 & hora == 0){
            hora = 12;
        }

        setZeroClock();


    }

    /**
     setZeroClock es para que se nos ponga el primer 0 a la par.
     */
    private void setZeroClock(){

        if(hora >=0 & hora <=9){
            hor = "0";
        }else{
            hor = "";
        }


        if(minuto >=0 & minuto <=9){
            min = ":0";
        }else{
            min = ":";
        }

        if(segundo >=0 & segundo <=9){
            sec = ":0";

        }else{
            sec = ":";
        }

        if(am_pm == 0){
            marca = "AM";
        }else{
            marca = "PM";
        }


    }

    /** ----- Fin Metodos Reloj ----- **/

}
