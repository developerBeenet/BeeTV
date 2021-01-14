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

public class CategoryRepository {

    private ArrayList<Category> categories = new ArrayList<>();
    private MutableLiveData<List<Category>> mutableLiveData = new MutableLiveData<>();
    private Application application;
    private String auth;

    /*Construct*/
    public CategoryRepository(Application application) {
        this.application = application;
    }

    public MutableLiveData<List<Category>> getMutableLiveData(String auth) {

        RestApiService apiService = RetrofitInstance.getApiService();

        Call<CategoryResponse> call = apiService.allCategories(auth);

        call.enqueue(new Callback<CategoryResponse>() {
            @Override
            public void onResponse(Call<CategoryResponse> call, Response<CategoryResponse> response) {
                CategoryResponse mCategoryResponse = response.body();
                if (mCategoryResponse != null && mCategoryResponse.getResponse_object() != null) {

                    categories = (ArrayList<Category>) mCategoryResponse.getResponse_object();
                    categories.remove(categories.size()-1);
                    mutableLiveData.setValue(categories);
                }
            }

            @Override
            public void onFailure(Call<CategoryResponse> call, Throwable t) {

            }

        });

        return mutableLiveData;
    }


}
