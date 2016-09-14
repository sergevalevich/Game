package com.valevich.game;

import android.content.Context;

import com.valevich.game.network.RestService;
import com.valevich.game.network.model.LastUpdateModel;
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

    public Observable<List<QuestionApiModel>> downloadQuestions() {
        return mRestService.loadQuestions();
    }

    public Observable<List<QuestionApiModel>> saveQuestions(List<QuestionApiModel> apiQuestions) {
        return Question.insertQuestions(apiQuestions);
    }

    public Observable<List<Question>> getQuestions(int limit,boolean isMediaAllowed) {
        return Question.getQuestions(limit,isMediaAllowed);
    }

    public Observable<Question> downloadQuestionsMedia(Question question) {
        return getQuestionMedia(question).flatMap(responseBody -> writeResponseBodyToDisk(responseBody,question));
    }

    private Observable<ResponseBody> getQuestionMedia(Question question) {
        return mRestService.loadQuestionMedia(UrlFormatter.getFileNameFrom(
                question.getMediaType()),
                UrlFormatter.getFileNameFrom(question.getMediaPath()));
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

    public Observable<Question> replaceWithNonMedia(List<String> questions) {
        return Question.replaceWithNonMedia(questions);
    }

    public Observable<LastUpdateModel> getLastUpdate() {
        return mRestService.getLastUpdate();
    }
}
