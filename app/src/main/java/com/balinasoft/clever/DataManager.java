package com.balinasoft.clever;

import android.content.Context;
import android.provider.Settings;

import com.balinasoft.clever.network.RestService;
import com.balinasoft.clever.network.model.LastUpdateModel;
import com.balinasoft.clever.network.model.QuestionApiModel;
import com.balinasoft.clever.network.model.QuestionsStatsModel;
import com.balinasoft.clever.network.model.StatsResponseModel;
import com.balinasoft.clever.storage.model.Question;
import com.balinasoft.clever.util.ConstantsManager;
import com.balinasoft.clever.util.NetworkStateChecker;
import com.balinasoft.clever.util.TimeFormatter;
import com.balinasoft.clever.util.UrlFormatter;
import com.google.gson.Gson;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.res.StringRes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Response;
import rx.Observable;

@EBean
public class DataManager {

    @RootContext
    Context mContext;

    @Bean
    RestService mRestService;

    @Bean
    NetworkStateChecker mNetworkStateChecker;

    @Bean
    TimeFormatter mTimeFormatter;

    @StringRes(R.string.network_unavailbale_message)
    String mNetworkUnavailableMessage;

    @StringRes(R.string.no_questions)
    String mNoQuestionsMessage;

    private List<String> mCachedQuestionsWithoutMedia;

    private List<Question> mFreshAnsweredQuestions;

    public Observable<Question> getQuestions() {
        return isNetworkAvailable() ? checkLastUpdate() : getCachedQuestions();
    }

    public Observable<Question> sendQuestionsStats() {
        return Question.getFreshAnsweredQuestions()
                .flatMap(questions -> questions.isEmpty() ? Observable.error(new RuntimeException("EMPTY QUESTIONS")) : Observable.just(questions))
                .doOnNext(this::setFreshAnsweredQuestions)
                .flatMap(Observable::from)
                .flatMap(this::getStatModel)
                .toList()
                .flatMap(this::getJsonString)
                .flatMap(json -> mRestService.sendQuestionsStats(json))
                .flatMap(statsResponse -> statsResponse.body().getSuccess() == 1 ? setQuestionsSent() : Observable.empty());
    }

    public Observable<Response<StatsResponseModel>> sendUserStats() {
        return GameApplication.isUserCheckedIn() ? sendUserData() : checkInAndSendUserStats();
    }

    private Observable<Response<StatsResponseModel>> sendUserData() {
        return mRestService.sendUserStats(getDeviceToken(),
                GameApplication.getSessionLength(),
                GameApplication.getUserCoins(),
                GameApplication.getUserScore(),
                mTimeFormatter.formatTime(GameApplication.getLaunchTime()));
    }

    private Observable<Response<StatsResponseModel>> checkInAndSendUserStats() {
        return mRestService.checkIn(getDeviceToken())
                .flatMap(statsResponse ->
                        statsResponse.body().getSuccess() == 1
                                ? setUserCheckedIn(statsResponse).flatMap(response -> sendUserData())
                                : Observable.empty());
    }

    private Observable<Response<StatsResponseModel>> setUserCheckedIn(Response<StatsResponseModel> responseModel) {
        GameApplication.setUserCheckedIn(true);
        return Observable.just(responseModel);
    }

    private Observable<Question> checkLastUpdate() {
        return getLastUpdate()
                .onErrorResumeNext(throwable -> Observable.just(new LastUpdateModel("")))
                .flatMap(lastUpdateModel ->
                        GameApplication.getLastUpdate().equals(lastUpdateModel.getLastUpdate()) || lastUpdateModel.getLastUpdate().isEmpty()
                                ? getCachedQuestions()
                                : loadFreshQuestions(lastUpdateModel.getLastUpdate()));
    }

    private Observable<Question> getCachedQuestions() {
        return isNetworkAvailable() ? getQuestionsWithPossibleMedia() : getQuestionsWithoutMedia();
    }

    private Observable<Question> loadFreshQuestions(String lastUpdate) {
        return isNetworkAvailable()
                ? downloadQuestionsAndGet(lastUpdate)
                : Observable.error(new RuntimeException(mNetworkUnavailableMessage));
    }

    private Observable<Question> downloadQuestionsAndGet(String lastUpdate) {
        return downloadQuestions()
                .doOnNext(Collections::shuffle)
                .flatMap(this::saveQuestions)
                .doOnNext(questions -> GameApplication.setLastUpdate(lastUpdate))
                .flatMap(questions -> getCachedQuestions());
    }

    private Observable<Question> getQuestionsIfEnough(List<Question> questions) {
        return areQuestionsEnough(questions)
                ? Observable.just(questions).doOnNext(this::setCachedQuestionsWithoutMedia).flatMap(Observable::from)
                : Observable.error(new RuntimeException(mNoQuestionsMessage));
    }

    private void setCachedQuestionsWithoutMedia(List<Question> questions) {
        mCachedQuestionsWithoutMedia = new ArrayList<>(questions.size());
        for(Question question : questions) {
            if (question.getMediaPath() == null) {
                mCachedQuestionsWithoutMedia.add(question.getTextQuest());
            }
        }
    }

    private boolean areQuestionsEnough(List<Question> questions) {
        return questions.size() == ConstantsManager.MAX_QUESTIONS_COUNT;
    }

    private Observable<LastUpdateModel> getLastUpdate() {
        return mRestService.getLastUpdate();
    }

    private Observable<Question> getQuestionsWithoutMedia() {
        return getQuestions(ConstantsManager.MAX_QUESTIONS_COUNT, false)
                .flatMap(this::getQuestionsIfEnough);
    }

    private Observable<Question> getQuestionsWithPossibleMedia() {
        return getQuestions(ConstantsManager.MAX_QUESTIONS_COUNT, true)
                .flatMap(this::getQuestionsIfEnough)
                .flatMap(question ->
                        question.getMediaPath() != null
                                ? loadQuestionMedia(question)
                                : Observable.just(question));
    }

    private Observable<Question> loadQuestionMedia(Question question) {
        return downloadQuestionsMedia(question).onErrorResumeNext(replaceWithNonMedia());
    }

    private Observable<Question> replaceWithNonMedia() {
        return Question.replaceWithNonMedia(mCachedQuestionsWithoutMedia).flatMap(newQuestion -> newQuestion == null
                ? Observable.error(new RuntimeException(mNoQuestionsMessage))
                : Observable.just(newQuestion))
                .doOnNext(question -> mCachedQuestionsWithoutMedia.add(question.getTextQuest()));
    }

    private Observable<List<Question>> getQuestions(int limit,boolean isMediaAllowed) {
        return Question.getQuestions(limit,isMediaAllowed);
    }

    private Observable<List<QuestionApiModel>> downloadQuestions() {
        return mRestService.loadQuestions();
    }

    private Observable<List<QuestionApiModel>> saveQuestions(List<QuestionApiModel> apiQuestions) {
        return Question.insertQuestions(apiQuestions);
    }

    private Observable<Question> downloadQuestionsMedia(Question question) {
        return getQuestionMedia(question).flatMap(responseBody -> writeResponseBodyToDisk(responseBody,question));
    }

    private Observable<ResponseBody> getQuestionMedia(Question question) {
        return mRestService.loadQuestionMedia(
                question.getMediaType(),
                UrlFormatter.getFileNameFrom(question.getMediaPath())
        );
    }

    private Observable<Question> writeResponseBodyToDisk(ResponseBody body, Question question) {
        try {
            File file = new File(mContext.getFilesDir() + File.separator + UrlFormatter.getFileNameFrom(question.getMediaPath()));

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(file);

                while (true) {
                    int read = inputStream.read(fileReader);
                    if (read == -1) {
                        break;
                    }
                    outputStream.write(fileReader, 0, read);
                }

                outputStream.flush();

                return Observable.just(question);

            } catch (IOException e) {
                return Observable.empty();
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return Observable.empty();
        }
    }

    ///////////////////STATS//////////////////////

    private Observable<Question> setQuestionsSent() {
        return Observable.from(mFreshAnsweredQuestions).flatMap(question -> {
            question.setHasSent(1);
            question.update();
            return Observable.just(question);
        });
    }

    private Observable<QuestionsStatsModel> getStatModel(Question question) {
        QuestionsStatsModel questionsStatsModel = new QuestionsStatsModel();
        questionsStatsModel.setAnswersCount(1);
        questionsStatsModel.setRightAnswersCount(question.getIsRightAnswered());
        questionsStatsModel.setId(question.getServerId());
        questionsStatsModel.setAnswerTime(question.getAnswerTime());
        return Observable.just(questionsStatsModel);
    }

    private Observable<String> getJsonString(List<QuestionsStatsModel> stats) {// FIXME: 20.09.2016
        Gson gson = new Gson();
        String json = gson.toJson(stats);
        return Observable.just(json);
    }

    private void setFreshAnsweredQuestions(List<Question> questions) {
        mFreshAnsweredQuestions = questions;
    }

    private String getDeviceToken() {
        return Settings.Secure.getString(mContext.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    private boolean isNetworkAvailable() {
        return mNetworkStateChecker.isNetworkAvailable();
    }
}
