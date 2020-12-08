package com.beenet.beenetplay_tv.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.beenet.beenetplay_tv.model.Category;
import com.beenet.beenetplay_tv.model.CategoryRepository;
import com.beenet.beenetplay_tv.model.Channel;
import com.beenet.beenetplay_tv.model.ChannelRepository;

import java.util.List;

public class PlayerViewModel extends AndroidViewModel {

    private CategoryRepository categoryRepository;
    private ChannelRepository channelRepository;
    //private LoginRepository loginRepository;

    public PlayerViewModel(@NonNull Application application) {
        super(application);
        categoryRepository = new CategoryRepository(application);
        channelRepository = new ChannelRepository(application);

    }

    public LiveData<List<Category>> getAllCategory(String auth){
        return categoryRepository.getMutableLiveData(auth);
    }

    public LiveData<List<Channel>> getAllChannel(String auth, String filter){
        return channelRepository.getMutableLiveData(auth,filter);
    }
}
