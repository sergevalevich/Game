package com.balinasoft.clever.services;


import com.balinasoft.clever.DataManager;
import com.balinasoft.clever.storage.model.News;
import com.balinasoft.clever.util.TimeFormatter;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;
import org.json.JSONException;
import org.json.JSONObject;

import rx.schedulers.Schedulers;
import timber.log.Timber;

@EService
public class MessagingService extends FirebaseMessagingService {

    @Bean
    DataManager mDataManager;

    @Bean
    TimeFormatter mTimeFormatter;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        RemoteMessage.Notification notification = remoteMessage.getNotification();

        if (notification != null) {
            String body = notification.getBody();
            Timber.d("MESSAGE_RECEIVED %s", body);
            //String clickAction = notification.getClickAction();
            try {
                saveNews(createNews(body));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private News createNews(String newsBody) throws JSONException {
        News news = new News();
        JSONObject apiNews = new JSONObject(newsBody);
        news.setDate(apiNews.getString("date"));
        news.setDescription(apiNews.getString("text"));
        news.setTopic(apiNews.getString("title"));
        news.setImageUrl(apiNews.getString("image"));
        return news;
    }

    private void saveNews(News news) {
        mDataManager.insertNews(news)
                .subscribeOn(Schedulers.io())
                .subscribe((n) -> {
                }, throwable ->
                {
                    Timber.d("error saving news %s", throwable.getLocalizedMessage());
                });
    }
}
