package com.balinasoft.clever;

import android.app.Application;

import com.balinasoft.clever.scheduling.GameJobCreator;
import com.balinasoft.clever.scheduling.jobs.UsersStatsJob;
import com.balinasoft.clever.util.Preferences_;
import com.crashlytics.android.Crashlytics;
import com.evernote.android.job.JobManager;
import com.jenzz.appstate.RxAppState;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EApplication;
import org.androidannotations.annotations.sharedpreferences.Pref;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

@EApplication
public class GameApplication extends Application {

    @Pref
    static Preferences_ mPreferences;

    @Bean
    GameJobCreator mGameJobCreator;

    @Bean
    UsersStatsJob mUsersStatsJob;

    @Override
    public void onCreate() {
        super.onCreate();

        JobManager.create(this).addJobCreator(mGameJobCreator);

        //Fabric
        Fabric.with(this, new Crashlytics());

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

        //App state
        RxAppState.monitor(this).subscribe(appState -> {
            switch (appState) {
                case FOREGROUND:
                    JobManager.instance().cancel(mUsersStatsJob.getId());
                    setLaunchTime(getCurrentTime());
                    break;
                case BACKGROUND:
                    setSessionTime(getCurrentTime() - getLaunchTime());
                    Timber.d("%d azaz %d", getCurrentTime(), getLaunchTime());
                    mUsersStatsJob.schedule();
                    break;
            }
        });
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

    public static String getLastUpdate() {
        return mPreferences.lastUpdate().get();
    }

    public static void setLastUpdate(String lastUpdate) {
        mPreferences.lastUpdate().put(lastUpdate);
    }

    public static int getLastAppLaunchDay() {
        return mPreferences.lastAppLaunchDay().get();
    }

    public static void setLastAppLaunchDay(int currentDay) {
        mPreferences.lastAppLaunchDay().put(currentDay);
    }

    public static int getBonus() {
        return mPreferences.bonus().get();
    }

    public static void setBonus(int bonus) {
        mPreferences.bonus().put(bonus);
    }

    public static boolean isFirstLaunch() {
        return getLastAppLaunchDay() == 0;
    }

    public static boolean isUserCheckedIn() {
        return mPreferences.isUserCheckedIn().get();
    }

    public static void setUserCheckedIn(boolean isCheckedIn) {
        mPreferences.isUserCheckedIn().put(isCheckedIn);
    }

    public static void setSessionTime(long sessionTime) {
        mPreferences.sessionLength().put(sessionTime/1000);
    }

    public static void setLaunchTime(long launchTime) {
        mPreferences.launchTime().put(launchTime);
    }

    public static long getLaunchTime() {
        return mPreferences.launchTime().get();
    }

    public static long getSessionLength() {
        return mPreferences.sessionLength().get();
    }

    private long getCurrentTime() {
        return System.currentTimeMillis();
    }



}
