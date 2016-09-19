package com.balinasoft.clever.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.balinasoft.clever.network.RestService;
import com.balinasoft.clever.network.model.StatsModel;
import com.balinasoft.clever.storage.model.Question;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;

@EService
public class StatsService extends Service {

    @Bean
    RestService mRestService;

    private Subscription mSubscription;

    private List<Question> mFreshAnsweredQuestions = new ArrayList<>();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mSubscription = sendStats()
                .subscribeOn(Schedulers.io())
                .doAfterTerminate(() -> stopSelf(startId))
                .subscribe(quote -> {}, throwable -> {});

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSubscription != null && !mSubscription.isUnsubscribed()) mSubscription.unsubscribe();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Observable<Question> sendStats() {
        return Question.getFreshAnsweredQuestions()
                .filter(questions -> !questions.isEmpty())
                .doOnNext(this::setFreshAnsweredQuestions)
                .flatMap(Observable::from)
                .flatMap(this::getStatModel)
                .toList()
                .flatMap(this::getJsonString)
                .flatMap(json -> mRestService.sendStats(json))
                .flatMap(statsResponse -> statsResponse.body().getSuccess() == 1 ? setQuestionsSent() : Observable.empty());
    }

    private Observable<Question> setQuestionsSent() {
        return Observable.from(mFreshAnsweredQuestions).flatMap(question -> {
            question.setHasSent(1);
            question.update();
            return Observable.just(question);
        });
    }

    private Observable<StatsModel> getStatModel(Question question) {
        StatsModel statsModel = new StatsModel();
        statsModel.setAnswersCount(1);
        statsModel.setRightAnswersCount(question.getIsRightAnswered());
        statsModel.setId(question.getServerId());
        statsModel.setAnswerTime(question.getAnswerTime());
        return Observable.just(statsModel);
    }

    private Observable<String> getJsonString(List<StatsModel> stats) {
        Gson gson = new Gson();
        String json = gson.toJson(stats);
        return Observable.just(json);
    }

    private void setFreshAnsweredQuestions(List<Question> questions) {
        mFreshAnsweredQuestions = questions;
    }
}
