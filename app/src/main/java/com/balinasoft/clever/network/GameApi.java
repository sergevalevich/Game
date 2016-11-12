package com.balinasoft.clever.network;


import com.balinasoft.clever.network.model.HelpModel;
import com.balinasoft.clever.network.model.LastUpdateModel;
import com.balinasoft.clever.network.model.LogInModel;
import com.balinasoft.clever.network.model.NewsModel;
import com.balinasoft.clever.network.model.QuestionApiModel;
import com.balinasoft.clever.network.model.DefaultResponseModel;
import com.balinasoft.clever.network.model.RatingModel;
import com.balinasoft.clever.network.model.RegisterModel;

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

interface GameApi {
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
    Observable<Response<DefaultResponseModel>> sendQuestionsStats(@Field("quests") String stats);

    @FormUrlEncoded
    @POST("users/stat")
    Observable<Response<DefaultResponseModel>> sendUserStats(@Field("email") String email,
                                                             @Field("username") String userName,
                                                             @Field("time_session") long sessionTime,
                                                             @Field("coins") int userCoins,
                                                             @Field("score") int userScore,
                                                             @Field("entry_time") String launchTime,
                                                             @Field("avatar") int avatar);

    @FormUrlEncoded
    @POST("users/mobile_login")
    Observable<LogInModel> logIn(@Field("os") String os,
                                 @Field("device_token") String token,
                                 @Field("email") String email,
                                 @Field("password") String password);

    @FormUrlEncoded
    @POST("users/mobile_login")
    Observable<LogInModel> logInWithFb(@Field("os") String os,
                                       @Field("device_token") String deviceToken,
                                       @Field("fb_token") String fbToken,
                                       @Field("email") String email,
                                       @Field("username") String username);

    @FormUrlEncoded
    @POST("users/mobile_login")
    Observable<LogInModel> logInWithVk(@Field("os") String os,
                                       @Field("device_token") String deviceToken,
                                       @Field("vk_token") String vkToken,
                                       @Field("email") String email,
                                       @Field("username") String username);

    @FormUrlEncoded
    @POST("users/register")
    Observable<RegisterModel> register(@Field("os") String os,
                                       @Field("device_token") String token,
                                       @Field("email") String email,
                                       @Field("password") String password);

    @FormUrlEncoded
    @POST("users/restore")
    Observable<DefaultResponseModel> restore(@Field("email") String email);

    @GET("users/rating")
    Observable<RatingModel> getRating(@Query("filter") String filter);

    @GET("news")
    Observable<NewsModel> getNews();

    @GET("help/one")
    Observable<HelpModel> getHelp(@Query("id") int id);
}
