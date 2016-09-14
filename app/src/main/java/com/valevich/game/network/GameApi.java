package com.valevich.game.network;


import com.valevich.game.network.model.LastUpdateModel;
import com.valevich.game.network.model.QuestionApiModel;
import com.valevich.game.network.model.QuestionMediaApiModel;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.http.GET;
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
}
