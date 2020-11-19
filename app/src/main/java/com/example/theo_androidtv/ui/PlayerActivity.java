package com.example.theo_androidtv.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;


import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import com.example.theo_androidtv.R;
import com.example.theo_androidtv.databinding.ActivityMainBinding;
import com.example.theo_androidtv.model.Category;
import com.example.theo_androidtv.model.Channel;
import com.example.theo_androidtv.model.LoginResponse;
import com.example.theo_androidtv.service.RestApiService;
import com.example.theo_androidtv.service.RetrofitInstance;
import com.example.theo_androidtv.viewmodel.PlayerViewModel;
import com.theoplayer.android.api.THEOplayerView;
import com.theoplayer.android.api.abr.AbrStrategyConfiguration;
import com.theoplayer.android.api.abr.AbrStrategyType;
import com.theoplayer.android.api.event.player.PlayerEventTypes;
import com.theoplayer.android.api.event.track.mediatrack.video.list.VideoTrackListEventTypes;
import com.theoplayer.android.api.event.track.texttrack.list.TextTrackListEventTypes;
import com.theoplayer.android.api.player.Player;
import com.theoplayer.android.api.player.track.mediatrack.quality.QualityList;
import com.theoplayer.android.api.player.track.mediatrack.quality.VideoQuality;
import com.theoplayer.android.api.player.track.texttrack.TextTrack;
import com.theoplayer.android.api.player.track.texttrack.TextTrackMode;
import com.theoplayer.android.api.source.SourceDescription;
import com.theoplayer.android.api.source.SourceType;
import com.theoplayer.android.api.source.TypedSource;
import com.example.theo_androidtv.databinding.ActivityMainBindingImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.theoplayer.android.api.source.SourceDescription.Builder.sourceDescription;

public class PlayerActivity extends AppCompatActivity {


    DrawerLayout drawerLayout; //Permite el despliegue de menu lateral en conjunto con mainLayout y menuLateral
    ConstraintLayout mainLayout, menuLateral;

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
    String auth, id_category = "0";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        /* --- */
        //Recuperando valor enviado proveniente de LoginActivityTV
        Bundle myBundle = this.getIntent().getExtras();
        if (myBundle != null){
            auth = myBundle.getString("auth");
        }

        /** Inicializando de elementos Visuales **/
        initializationViews();
        playerViewModel = ViewModelProviders.of(this).get(PlayerViewModel.class);

        /** Poblando RecyclerView **/
        getPopularChannel(id_category);

        /** Poblando Spinner **/
        getCategory();
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
            url = "https://xcdrsbsv-a.beenet.com.sv/foxnews_720/foxnews_720_out/playlist.m3u8";
        }

        TypedSource typedSource = TypedSource.Builder
                .typedSource()
                .src(url)
                .type(SourceType.HLS)
                .build();

        AbrStrategyConfiguration abrConfig = AbrStrategyConfiguration.Builder
                .abrStrategyConfiguration()
                .setType(AbrStrategyType.PERFORMANCE)
                .build();

        player.getAbr().setAbrStrategy(abrConfig);
        player.getAbr().setTargetBuffer(4);



        // Creating a SourceDescription builder that contains the settings to be applied as a new
        // THEOplayer source.
        SourceDescription.Builder sourceDescription = sourceDescription(typedSource)
                .poster(getString(R.string.defaultPosterUrl));

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
        //viewBinding.theoPlayerView.onPause();
        theoplayerView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //viewBinding.theoPlayerView.onResume();
        theoplayerView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //viewBinding.theoPlayerView.onDestroy();
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

        mChannelAdapter = new ChannelAdapter(channelList);
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

        categoryList.set(0,new Category(0,"Todos"));

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

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

                    Toast.makeText(getApplicationContext(),"Sesion Finalizada",Toast.LENGTH_LONG).show();
                    //StyleableToast.makeText(getApplicationContext(),"Sesion Finalizada",R.style.msgToast).show();
                    //Volver a Inicio de Sesion
                    //Intent intent = new Intent (getApplicationContext(), LoginActivityTV.class);
                    //startActivityForResult(intent, 0);

                    finish();

                }else{
                    Toast.makeText(getApplicationContext(), loginResponse.getError_description(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

}
