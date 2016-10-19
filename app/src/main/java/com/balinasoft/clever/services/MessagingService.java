package com.balinasoft.clever.services;


import android.content.Intent;

import com.balinasoft.clever.ui.activities.MainActivity_;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.androidannotations.annotations.EService;

import java.util.Map;

import timber.log.Timber;

@EService
public class MessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Timber.d("Message received %s",remoteMessage.toString());

        Timber.d("body %s", remoteMessage.getNotification().getBody());

        Map<String,String> data = remoteMessage.getData();

        if (data.size() > 0) {
            Timber.d("Message data payload: %s",remoteMessage.getData());
        }

        RemoteMessage.Notification notification = remoteMessage.getNotification();

        if (notification != null) {
            String clickAction = notification.getClickAction();
            Timber.d("Message Notification Body: %s Click action: %s",
                    remoteMessage.getNotification().getBody(),clickAction);
            MainActivity_.intent(this)
                    .action(clickAction)
                    .fromNotification(true)
                    .flags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .start();
        }

    }

}
