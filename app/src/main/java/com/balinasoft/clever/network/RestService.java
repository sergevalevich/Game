package com.balinasoft.clever.network;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.balinasoft.clever.network.model.LastUpdateModel;
import com.balinasoft.clever.network.model.QuestionApiModel;
import com.balinasoft.clever.network.model.StatsResponseModel;
import com.balinasoft.clever.util.ConstantsManager;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import rx.Observable;

@EBean
public class RestService {

    private GameApi mGameApi;

    @AfterInject
    void setUpClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConstantsManager.BASE_URL)
                .client(getOkHttpClient())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(getGson()))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        mGameApi = retrofit.create(GameApi.class);
    }

    private OkHttpClient getOkHttpClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(getInterceptor())
                .connectTimeout(ConstantsManager.CONNECTION_TIME_OUT, TimeUnit.SECONDS)
                .readTimeout(ConstantsManager.READ_TIME_OUT, TimeUnit.SECONDS)
                .build();
    }

    private HttpLoggingInterceptor getInterceptor() {
        return new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
    }

    private Gson getGson() {
        return new GsonBuilder().create();
    }

    public Observable<List<QuestionApiModel>> loadQuestions() {
        return mGameApi.loadQuestions();
    }

    public Observable<ResponseBody> loadQuestionMedia(String mediaType,String fileName) {
        return mGameApi.loadQuestionMedia(mediaType,fileName);
    }

    public Observable<LastUpdateModel> getLastUpdate() {
        return mGameApi.getLastUpdate();
    }

    public Observable<Response<StatsResponseModel>> sendStats(String json) {
        return mGameApi.sendStats(json);
    }

}
