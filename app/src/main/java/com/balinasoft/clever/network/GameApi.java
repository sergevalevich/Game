package com.balinasoft.clever.network;


import com.balinasoft.clever.network.model.LastUpdateModel;
import com.balinasoft.clever.network.model.QuestionApiModel;
import com.balinasoft.clever.network.model.StatsResponseModel;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

public interface GameApi {
    @GET("quests/get")
    Observable<List<QuestionApiModel>> loadQuestions();

    @GET("media/{type}")
    Observable<ResponseBody> loadQuestionMedia(
            @Path("type") String mediaType,
            @Query("filename") String filename);

    @GET("quests/get_date")
    Observable<LastUpdateModel> getLastUpdate();

    @FormUrlEncoded
    @POST("quests/stat")
    Observable<Response<StatsResponseModel>> sendQuestionsStats(@Field("quests") String stats);

    @FormUrlEncoded
    @POST("users/stat")
    Observable<Response<StatsResponseModel>> sendUserStats(@Field("device_token") String token,
                                                           @Field("time_session") long sessionTime,
                                                           @Field("coins") int userCoins,
                                                           @Field("score") int userScore,
                                                           @Field("entry_time") String launchTime);

    @FormUrlEncoded
    @POST("users/check_in")
    Observable<Response<StatsResponseModel>> checkIn(@Field("device_token") String token);
}
