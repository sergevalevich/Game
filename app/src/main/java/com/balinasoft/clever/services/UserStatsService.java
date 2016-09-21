package com.balinasoft.clever.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.balinasoft.clever.DataManager;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;

import rx.Subscription;
import rx.schedulers.Schedulers;
import timber.log.Timber;

@EService
public class UserStatsService extends Service {

    @Bean
    DataManager mDataManager;

    private Subscription mSubscription;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.d("On start command");
        mSubscription = mDataManager.sendUserStats()
                .subscribeOn(Schedulers.io())
                .doAfterTerminate(this::stopSelf)
                .subscribe(quote -> {}, throwable -> {});

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.d("on service destroy");
        if (mSubscription != null && !mSubscription.isUnsubscribed()) mSubscription.unsubscribe();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
