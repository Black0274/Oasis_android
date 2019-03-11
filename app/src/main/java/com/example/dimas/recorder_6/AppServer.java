package com.example.dimas.recorder_6;

import android.app.Application;
import android.app.Service;
import android.widget.Toast;

import com.example.dimas.recorder_6.api.OasisApi;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Dimas on 09.03.2018.
 */

public class AppServer extends Application{
    private static OasisApi oasisApi;
    private Retrofit retrofit;
    public static final String BASE_URL = "http://oasis-project.org:8192/";

    @Override
    public void onCreate() {
        super.onCreate();

        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(20, TimeUnit.SECONDS)
                .connectTimeout(20, TimeUnit.SECONDS)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();

        oasisApi = retrofit.create(OasisApi.class);
    }

    public static OasisApi getApi() {
        return oasisApi;
    }
}
