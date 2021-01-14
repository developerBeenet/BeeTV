package com.beenet.beenetplay_tv.model;

import android.app.Application;

import androidx.lifecycle.MutableLiveData;

import com.beenet.beenetplay_tv.service.RestApiService;
import com.beenet.beenetplay_tv.service.RetrofitInstance;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChannelRepository {

    private ArrayList<Channel> channels = new ArrayList<>();
    private MutableLiveData<List<Channel>> mutableLiveData = new MutableLiveData<>();
    private Application application;

    public ChannelRepository(Application application) {
        this.application = application;
    }

    public MutableLiveData<List<Channel>> getMutableLiveData(String auth, String filter) {

        //Borrando Elementos de Arraylist
        channels.clear();

        RestApiService apiService = RetrofitInstance.getApiService();

        Call<ChannelResponse> call = apiService.allChannels(auth);

        call.enqueue(new Callback<ChannelResponse>() {
            @Override
            public void onResponse(Call<ChannelResponse> call, Response<ChannelResponse> response) {
                ChannelResponse mchannelResponse = response.body();

                if (mchannelResponse != null && mchannelResponse.getResponse_object() != null) {
                    if(filter.equals("0")) {
                        //Todos
                        channels = (ArrayList<Channel>) mchannelResponse.getResponse_object();
                        //mutableLiveData.setValue(channels);
                    }

                    //Agregar Canales a lista segun categoria o Todos
                    for (Channel channel : mchannelResponse.getResponse_object()){
                        if (channel.getGenre_id().equals(filter)){
                            channels.add(new Channel(
                                    channel.getId(),
                                    channel.getGenre_id(),
                                    channel.getTitle(),
                                    channel.getIcon_url(),
                                    channel.getStream_url()
                            ));
                        }
                    }

                    mutableLiveData.setValue(channels);
                }
            }

            @Override
            public void onFailure(Call<ChannelResponse> call, Throwable t) {

            }
        });

        return mutableLiveData;
    }


}
