package com.valevich.game;

import android.app.Application;

import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.valevich.game.util.Preferences_;

import org.androidannotations.annotations.EApplication;
import org.androidannotations.annotations.sharedpreferences.Pref;

import timber.log.Timber;

@EApplication
public class GameApplication extends Application {

    @Pref
    static Preferences_ mPreferences;

    @Override
    public void onCreate() {
        super.onCreate();

        //DbFlow
        FlowManager.init(new FlowConfig.Builder(this)
                .openDatabasesOnInit(true).build());

        //Timber
        if(BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree() {
                @Override
                protected String createStackElementTag(StackTraceElement element) {
                    return super.createStackElementTag(element) + ":" + element.getLineNumber();
                }
            });
        }
    }

    public static String getUserName() {
        return mPreferences.userName().get();
    }

    public static void setUserName(String userName) {
        mPreferences.userName().put(userName);
    }

    public static int getUserScore() {
        return mPreferences.userScore().get();
    }

    public static int getUserCoins() {
        return mPreferences.userCoins().get();
    }

    public static void setUserScore(int score) {
        mPreferences.userScore().put(score);
    }

    public static void setUserCoins(int coins) {
        mPreferences.userCoins().put(coins);
    }

    public static int getUserImage() {
        return mPreferences.userImage().get();
    }

    public static void setUserImage(int imageResId) {
        mPreferences.userImage().put(imageResId);
    }

    public static String getLastUpdateId() {
        return mPreferences.lastUpdateId().get();
    }

    public static void setLastUpdateId(String id) {
        mPreferences.lastUpdateId().put(id);
    }

}
