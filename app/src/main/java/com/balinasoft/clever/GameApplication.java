package com.balinasoft.clever;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.support.multidex.MultiDex;

import com.balinasoft.clever.util.ConstantsManager;
import com.balinasoft.clever.util.NetworkStateChecker;
import com.balinasoft.clever.util.Preferences_;
import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKAccessTokenTracker;
import com.vk.sdk.VKSdk;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EApplication;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.net.URISyntaxException;

import io.fabric.sdk.android.Fabric;
import io.socket.client.IO;
import io.socket.client.Socket;
import timber.log.Timber;

@EApplication
public class GameApplication extends Application {

    @Pref
    static Preferences_ mPreferences;

    @Bean
    NetworkStateChecker mNetworkStateChecker;

    private static Socket mSocket;

    static {
        try {
            mSocket = IO.socket(ConstantsManager.BASE_URL);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    VKAccessTokenTracker mVKAccessTokenTracker = new VKAccessTokenTracker() {
        @Override
        public void onVKAccessTokenChanged(VKAccessToken oldToken, VKAccessToken newToken) {
            if (newToken == null) {
                //login
            }
        }
    };


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            MultiDex.install(base);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //Fabric
        Fabric.with(this, new Crashlytics());

        //DbFlow
        FlowManager.init(new FlowConfig.Builder(this)
                .openDatabasesOnInit(true).build());

        //VK
        mVKAccessTokenTracker.startTracking();
        VKSdk.initialize(this);

        //Facebook
        FacebookSdk.sdkInitialize(this);
        AppEventsLogger.activateApp(this);

        //Timber
        if(BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree() {
                @Override
                protected String createStackElementTag(StackTraceElement element) {
                    return super.createStackElementTag(element) + ":" + element.getLineNumber();
                }
            });
        }

        saveDeviceToken();

        if(isFirstLaunch()) setUserCoins(ConstantsManager.INIT_COINS);
    }

    private void saveDeviceToken() {
        mPreferences.deviceToken().put(Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID));
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

    public static void setUserEmail(String email) {
        mPreferences.userEmail().put(email);
    }

    public static String getUserEmail() {
        return mPreferences.userEmail().get();
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

    public static long getCurrentTime() {
        return System.currentTimeMillis();
    }

    public static long getSessionLength() {
        return mPreferences.sessionLength().get();
    }

    public static void saveVkToken(String vkToken) {
        mPreferences.vkToken().put(vkToken);
    }

    public static void saveFacebookToken(String token) {
        mPreferences.facebookToken().put(token);
    }

    public static String getFacebookToken() {
        return mPreferences.facebookToken().get();
    }

    public static String getVKToken() {
        return mPreferences.vkToken().get();
    }

    public static String getDeviceToken() {
        return mPreferences.deviceToken().get();
    }

    public static boolean isAuthTokenExists() {
        return !mPreferences.cleverToken().get().equals("");
    }

    public static void saveCleverToken(String token) {
        mPreferences.cleverToken().put(token);
    }

    public static String getAuthToken() {
        return mPreferences.cleverToken().get();
    }

    public static String getUserId() {
        return mPreferences.userId().get();
    }

    public static void saveUserId(String id) {
        mPreferences.userId().put(id);
    }

    public static Socket getSocket() {
        return mSocket;
    }

    public static boolean isFirstLaunch() {
        return getLastAppLaunchDay() == 0;
    }

    public static boolean isNewAccount() {
        return mPreferences.isNewAccount().get();
    }

    public static void setNewAccount(boolean isNewAccount) {
        mPreferences.isNewAccount().put(isNewAccount);
    }

    public static void setOnlineName(String name) {
        mPreferences.userNameOnline().put(name);
    }

    public static void setOnlineImage(int imageResId) {
        mPreferences.userImageOnline().put(imageResId);
    }

    public static void setOnlineScore(int score) {
        mPreferences.userScoreOnline().put(score);
    }

    public static void setOnlineCoins(int coins) {
        mPreferences.userCoinsOnline().put(coins);
    }

    public static String getOnlineName() {
        return mPreferences.userNameOnline().get();
    }

    public static int getOnlineImage() {
        return mPreferences.userImageOnline().get();
    }

    public static int getOnlineScore() {
        return mPreferences.userScoreOnline().get();
    }

    public static int getOnlineCoins() {
        return mPreferences.userCoinsOnline().get();
    }
}
