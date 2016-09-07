package com.valevich.game;

import android.content.Context;

import com.valevich.game.network.RestService;
import com.valevich.game.network.model.QuestionApiModel;
import com.valevich.game.storage.model.Question;
import com.valevich.game.util.UrlFormatter;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import okhttp3.ResponseBody;
import rx.Observable;

@EBean
public class DataManager {

    @RootContext
    Context mContext;

    @Bean
    RestService mRestService;

    public Observable<Object> loadQuestions() {

        return getQuestions().flatMap(apiQuestions -> Observable.merge(
                saveQuestions(apiQuestions),
                saveQuestionsMedia(apiQuestions))).cache();

    }

    private Observable<List<QuestionApiModel>> getQuestions() {
        return mRestService.loadQuestions();
    }

    private Observable<QuestionApiModel> saveQuestions(List<QuestionApiModel> apiQuestions) {
        return Observable.from(apiQuestions).flatMap(Question::insertQuestion);
    }

    private Observable<Boolean> saveQuestionsMedia(List<QuestionApiModel> apiQuestions) {
        return Observable
                .from(apiQuestions)
                .filter(question -> question.getMediaType() != null)
                .flatMap(apiQuestion -> Observable.zip(getQuestionMedia(apiQuestion), getFileName(apiQuestion), this::writeResponseBodyToDisk));
    }

    private Observable<ResponseBody> getQuestionMedia(QuestionApiModel apiQuestion) {
        return mRestService.loadQuestionMedia(UrlFormatter.getFileNameFrom(
                apiQuestion.getMediaType()),
                UrlFormatter.getFileNameFrom(apiQuestion.getMediaUrl()));
    }

    private Observable<String> getFileName(QuestionApiModel questionApiModel) {
        return Observable.just(UrlFormatter.getFileNameFrom(questionApiModel.getMediaUrl()));
    }

    private boolean writeResponseBodyToDisk(ResponseBody body, String filename) {
        try {
            File file = new File(mContext.getFilesDir() + File.separator + filename);

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

                return true;

            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }
}
