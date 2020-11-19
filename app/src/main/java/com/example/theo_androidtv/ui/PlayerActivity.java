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

    /* ---- */
    DrawerLayout drawerLayout; //Permite el despliegue de menu lateral en conjunto con mainLayout y menuLateral
    ConstraintLayout mainLayout, menuLateral;

    /* Categories */
    Spinner sp_categorias;
    List<Category> cat = null;  //Lista de Objetos Categoria

    RecyclerView mRecyclerView;
    SwipeRefreshLayout swipeRefresh;
    public PlayerViewModel playerViewModel;

    ChannelAdapter mChannelAdapter;

    public String stream = " ";
    String auth, id_category = "0";

    THEOplayerView theoplayerView;
    Player player;
    /* ---- */


    /*
    private static final String TAG = PlayerActivity.class.getSimpleName();
    private Typeface fontAwesome;
    private ActivityMainBinding viewBinding;
    Handler timeout = new Handler();
    final double SKIP_FORWARD_SECS = 10;
    final int INACTIVITY_SECONDS = 10;
    final double VOLUME_DELTA = 0.05;
    final double SKIP_BACKWARD_SECS = 10;
    final double[] playbackRates = {-2.0, -1.50, -1.25, -1, -.5, .5, 1.0, 1.25, 1.50, 2.00};
    int currentPlaybackRateIndex = 6;
    double currentTime;
    Player player;
    int currentTextTrackIndex = 0;
    MutableLiveData<Boolean> trickPlayVisible = new MutableLiveData<>();
    final Runnable r = () -> trickPlayVisible.setValue(false);

    Map<String, TextTrack> textTracks = new HashMap<>();
    Map<String, VideoQuality> qualities = new HashMap<>();
    */

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


        /* --- */

        /*
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        fontAwesome = Typeface.createFromAsset(this.getAssets(), "fa.otf");


        //ocultando controles de player
        trickPlayVisible.setValue(false);

        // Getting all buttons within trickbar control
        for (int index = 0; index <= viewBinding.trickbar.getChildCount(); index++) {
            View innerControl = viewBinding.trickbar.getChildAt(index);
            if (innerControl instanceof Button) {
                Button b = (Button) innerControl;

                // applying font awesome
                b.setTypeface(fontAwesome);

                // adding focus change listener and changing background depending on the focus state
                b.setOnFocusChangeListener((view, bl) -> {
                    int color = bl ? ContextCompat.getColor(PlayerActivity.this, R.color.THEOBlue) :
                            ContextCompat.getColor(PlayerActivity.this, R.color.THEOYellow);
                    view.setBackgroundTintList(ColorStateList.valueOf(color));
                });

                // hiding trickbar upon pressing key down
                /*
                b.setOnKeyListener((view, i, keyEvent) -> {
                    if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                        timeout.removeCallbacks(r);
                        trickPlayVisible.setValue(false);
                    }
                    // return false to allow upper components to handle they keystroke
                    return false;
                });

                 */
            /*
            }
        }
        */

        // Setting the player reference for future use
        player = theoplayerView.getPlayer();


        //Configure the player
        configureTheoPlayer(stream);

        /*
        // Hiding the trickbar to disable the keys when the hiding animation is complete
        viewBinding.trickbar.animate().setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (!trickPlayVisible.getValue()) {
                    viewBinding.trickbar.setVisibility(View.GONE);
                }
            }
        });

        // Observing trickPlayVisible livedata and showing/hiding the trickbar accordingly
        trickPlayVisible.observeForever(visible -> {
            if (visible) {
                viewBinding.trickbar.setVisibility(View.VISIBLE);
                viewBinding.trickbar.animate().translationY(0).alpha(1);
            } else {
                viewBinding.trickbar.animate().translationY(64).alpha(0);
            }
        });

        // Play/pause action
        viewBinding.playPauseBtn.setOnClickListener(v -> {
            if (player.isPaused()) {
                player.play();
            } else {
                player.pause();
            }
        });

        // Volume up
        viewBinding.volumeUpBtn.setOnClickListener(view -> {
            double currentVolume = player.getVolume();
            if (currentVolume < 1) {
                player.setVolume(currentVolume + VOLUME_DELTA);
            }
        });

        // Volume down
        viewBinding.volumeDownBtn.setOnClickListener(view -> {
            double currentVolume = player.getVolume();
            if (currentVolume > 0) {
                player.setVolume(currentVolume - VOLUME_DELTA);
            }
        });

        // Skipping forward by SKIP_FORWARD_SECS seconds
        viewBinding.forwardBtn.setOnClickListener(view -> player.setCurrentTime(currentTime + SKIP_FORWARD_SECS));

        // Skipping backward by SKIP_BACKWARD_SECS seconds
        viewBinding.backwardBtn.setOnClickListener(view -> player.setCurrentTime(currentTime - SKIP_BACKWARD_SECS));


        // Setting faster playback rate based on the playbackrates array
        viewBinding.ffwdBtn.setOnClickListener(view -> {
            if (currentPlaybackRateIndex < playbackRates.length - 1) {
                currentPlaybackRateIndex++;
            }
            player.setPlaybackRate(playbackRates[currentPlaybackRateIndex]);
        });

        // Setting slower playback rate based on the playbackrates array
        viewBinding.fbckBtn.setOnClickListener(view -> {
            if (currentPlaybackRateIndex > 0) {
                currentPlaybackRateIndex--;
            }
            player.setPlaybackRate(playbackRates[currentPlaybackRateIndex]);
        });

        // Showing dialog to chose subtitles/cc track

        viewBinding.ccBtn.setOnClickListener(view -> {
            final ArrayList<String> items = new ArrayList<>();
            items.add(getString(R.string.none));

            for (Map.Entry<String, TextTrack> entry : textTracks.entrySet()) {
                items.add(entry.getKey());
            }

            final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(PlayerActivity.this, R.style.THEO_Dialog));
            LayoutInflater inflater = getLayoutInflater();
            View convertView = inflater.inflate(R.layout.list, null);
            builder.setView(convertView);

            builder.setIcon(R.drawable.cc);
            builder.setTitle(getString(R.string.selectSubtitles));
            final AlertDialog dialog = builder.create();
            ListView lv = convertView.findViewById(R.id.lv);
            final ArrayAdapter<String> adapter = new ArrayAdapter(PlayerActivity.this, R.layout.list_item, items);
            lv.setAdapter(adapter);
            lv.setOnItemClickListener((adapterView, view1, i, l) -> {
                currentTextTrackIndex = i;
                String strName = items.get(i);

                // First disable other tracks
                for (Map.Entry<String, TextTrack> entry : textTracks.entrySet()) {
                    entry.getValue().setMode(TextTrackMode.DISABLED);
                }

                // If a track different than 'none' was selected, set its mode to SHOWING
                if (!strName.equals(getString(R.string.none))) {
                    TextTrack textTrack = textTracks.get(strName);
                    textTrack.setMode(TextTrackMode.SHOWING);
                }
                dialog.dismiss();
            });
            // Try to focus back on CC button on cancel dialog
            dialog.setOnCancelListener(dialogInterface -> {
                viewBinding.trickbar.requestFocus();
                viewBinding.ccBtn.requestFocus();
            });
            // Try to focus back on CC button dismiss dialog
            dialog.setOnDismissListener(dialogInterface -> {
                viewBinding.trickbar.requestFocus();
                viewBinding.ccBtn.requestFocus();
            });
            dialog.show();

            // Stylize the CC button to appear "pressed" when a CC track is chosen
            if (currentTextTrackIndex != 0) {
                int color = ContextCompat.getColor(PlayerActivity.this, R.color.THEOYellowAlt);
                viewBinding.ccBtn.setBackgroundTintList(ColorStateList.valueOf(color));
                viewBinding.ccBtn.setBackgroundTintMode(PorterDuff.Mode.DARKEN);
            } else {
                int color = ContextCompat.getColor(PlayerActivity.this, R.color.THEOYellow);
                viewBinding.ccBtn.setBackgroundTintList(ColorStateList.valueOf(color));
                viewBinding.ccBtn.setBackgroundTintMode(PorterDuff.Mode.DARKEN);
            }
        });

        // Showing dialog to chose subtitles/cc track
        viewBinding.qualityBtn.setOnClickListener(view -> {
            final ArrayList<String> items = new ArrayList<>();

            for (Map.Entry<String, VideoQuality> entry : qualities.entrySet()) {
                items.add(entry.getKey());
            }

            final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(PlayerActivity.this, R.style.THEO_Dialog));
            LayoutInflater inflater = getLayoutInflater();
            View convertView = inflater.inflate(R.layout.list, null);
            builder.setView(convertView);

            builder.setIcon(R.drawable.cc);
            builder.setTitle(getString(R.string.selectSubtitles));
            final AlertDialog dialog = builder.create();
            ListView lv = convertView.findViewById(R.id.lv);
            final ArrayAdapter<String> adapter = new ArrayAdapter(PlayerActivity.this, R.layout.list_item, items);
            lv.setAdapter(adapter);
            lv.setOnItemClickListener((adapterView, view1, i, l) -> {
                currentTextTrackIndex = i;
                String strName = items.get(i);

                // If a track different than 'none' was selected, set its mode to SHOWING
                if (!strName.equals(getString(R.string.none))) {
                    VideoQuality targetQuality = qualities.get(strName);
                    Log.i(TAG, "targetQuality: " + targetQuality.getWidth() + "x" + targetQuality.getHeight());
                    player.getVideoTracks().getItem(0).setTargetQuality(targetQuality);
                }

                dialog.dismiss();
            });
            // Try to focus back on CC button on cancel dialog
            dialog.setOnCancelListener(dialogInterface -> {
                viewBinding.trickbar.requestFocus();
                viewBinding.qualityBtn.requestFocus();
            });
            // Try to focus back on CC button dismiss dialog
            dialog.setOnDismissListener(dialogInterface -> {
                viewBinding.trickbar.requestFocus();
                viewBinding.qualityBtn.requestFocus();
            });
            dialog.show();

            // Stylize the CC button to appear "pressed" when a CC track is chosen
            if (currentTextTrackIndex != 0) {
                int color = ContextCompat.getColor(PlayerActivity.this, R.color.THEOYellowAlt);
                viewBinding.qualityBtn.setBackgroundTintList(ColorStateList.valueOf(color));
                viewBinding.qualityBtn.setBackgroundTintMode(PorterDuff.Mode.DARKEN);
            } else {
                int color = ContextCompat.getColor(PlayerActivity.this, R.color.THEOYellow);
                viewBinding.qualityBtn.setBackgroundTintList(ColorStateList.valueOf(color));
                viewBinding.qualityBtn.setBackgroundTintMode(PorterDuff.Mode.DARKEN);
            }
        });

        // In INACTIVITY_SECONDS seconds of inactivity hide the trickbar
        timeout.postDelayed(r, INACTIVITY_SECONDS * 1000);

         */
    }


    private void configureTheoPlayer(String url) {
        // Creating a TypedSource builder that defines the location of a single stream source
        // and has Widevine DRM parameters applied.
        // TypedSource.Builder typedSource = typedSource(getString(R.string.defaultSourceUrl));
        //   .drm(drmConfiguration.build());

        if(url == " "){
            url = "https://xcdrsbsv-b.beenet.com.sv/abr_france24/abr_france24_out/playlist.m3u8";
        }

        Toast.makeText(getApplicationContext(), "URL: "+url, Toast.LENGTH_LONG).show();

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

        // Build text track list
        /*
        player.getTextTracks().addEventListener(TextTrackListEventTypes.ADDTRACK, addTrackEvent -> {
            String id = addTrackEvent.getTrack().getLabel() + ": " + addTrackEvent.getTrack().getLanguage();
            textTracks.put(id, addTrackEvent.getTrack());
        });
         */

        // Update play button icons on PLAY, PAUSE and ERROR events
        /*
        player.addEventListener(PlayerEventTypes.PLAY, playEvent -> {
            viewBinding.playPauseBtn.setText(getString(R.string.pauseIcon));
        });
        player.addEventListener(PlayerEventTypes.PAUSE, pauseEvent -> viewBinding.playPauseBtn.setText(getString(R.string.playIcon)));
        player.addEventListener(PlayerEventTypes.ERROR, pauseEvent -> viewBinding.playPauseBtn.setText(getString(R.string.playIcon)));
         */

        // Adding listeners to THEOplayer content protection events.
        /*
        player.addEventListener(PlayerEventTypes.CONTENTPROTECTIONSUCCESS, event -> Log.i(TAG, "Event: CONTENT_PROTECTION_SUCCESS, mediaTrackType=" + event.getMediaTrackType()));
        player.addEventListener(PlayerEventTypes.CONTENTPROTECTIONERROR, event -> Log.i(TAG, "Event: CONTENT_PROTECTION_ERROR, error=" + event.getError()));
        */

        //Adding the Video Quality tracks in the List
        /*
        player.getVideoTracks().addEventListener(VideoTrackListEventTypes.ADDTRACK, event -> {
            QualityList<VideoQuality> qualitiesArray = player.getVideoTracks().getItem(0).getQualities();
            for (int i = 0; i < qualitiesArray.length(); i++) {
                String id = qualitiesArray.getItem(i).getWidth() + "x" + qualitiesArray.getItem(i).getHeight();
                qualities.put(id, qualitiesArray.getItem(i));
            }
        });
         */

        // Update time and progress bar
        /*
        player.addEventListener(PlayerEventTypes.TIMEUPDATE, timeUpdateEvent -> {
            currentTime = timeUpdateEvent.getCurrentTime();
            boolean isLive = Double.isNaN(player.getDuration());
            String text;
            if (isLive) {
                // If live stream, only show current time
                text = getString(R.string.live) + " " + formatTime((int) currentTime);
                viewBinding.progress.setProgress(100);
            } else {
                double duration = player.getDuration();
                int progress = (int) Math.round(currentTime / duration * 100);
                text = formatTime((int) currentTime) + getString(R.string.timeProgressSeparator) + formatTime((int) duration);
                viewBinding.progress.setProgress(progress);
            }
            viewBinding.time.setText(text);

            if (player.getVideoTracks().length() > 0) {
                VideoQuality activeQuality = player.getVideoTracks().getItem(0).getActiveQuality();
                if (activeQuality != null) {
                    Log.i(TAG, "Event: activeQuality:" + activeQuality.getWidth() + "x" + activeQuality.getHeight());

                }
            }
        });
        */

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

    // Make seconds look nice
    public String formatTime(long secs) {
        return String.format(getString(R.string.timeFormat), secs / 3600, (secs % 3600) / 60, secs % 60);
    }


        /* --- */

    private void initializationViews() {

        theoplayerView = findViewById(R.id.theoPlayerView);
        //iniciarPlayer(stream);

        //Selector de Categorias
        sp_categorias = (Spinner) findViewById(R.id.sp_canales);

        //Lista de Canales
        //listaCanales = (ListView) findViewById(R.id.lista_canales) ;
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        mRecyclerView = (RecyclerView) findViewById(R.id.channelRecyclerView);
        //mRecyclerView.setSelected(true);
        //Contenedor de drawer
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);

        //Contenedor MainLayout y MenuLateral
        mainLayout = (ConstraintLayout) findViewById(R.id.mainLayout);
        menuLateral = (ConstraintLayout) findViewById(R.id.menuLateral);

        //Poblando Spinner de Categorias con arraylist
        //loadCategories(auth);

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
        //Agregando Categoria 'Todos'
        //categoryList.add(new Category(0,"Todos"));
        categoryList.set(0,new Category(0,"Todos"));
        ArrayAdapter<Category> arrayAdapter = new ArrayAdapter<>(getApplicationContext(),
                R.layout.category_item, categoryList);

        sp_categorias.setAdapter(arrayAdapter);

        sp_categorias.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                id_category = Integer.toString(categoryList.get(position).getId());

                //Toast.makeText(getApplicationContext(),"ID CATEGORIA: "+id_category,Toast.LENGTH_LONG).show();

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
                        postDataLogout(auth);

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
    public void postDataLogout(final String auth){

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
