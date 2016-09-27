package com.balinasoft.clever.ui.activities;

import android.support.v7.app.AppCompatActivity;

import com.balinasoft.clever.services.UserStatsService_;
import com.balinasoft.clever.util.NetworkStateChecker;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;

import timber.log.Timber;

import static com.balinasoft.clever.GameApplication.getCurrentTime;
import static com.balinasoft.clever.GameApplication.getLaunchTime;
import static com.balinasoft.clever.GameApplication.setLaunchTime;
import static com.balinasoft.clever.GameApplication.setSessionTime;

@EActivity
public class BaseActivity extends AppCompatActivity {

    public static int RESUMED_ACTIVITIES_COUNT = 0;

    @Bean
    NetworkStateChecker mNetworkStateChecker;

    boolean mIsInteractionAllowed;

    @Override
    protected void onResume() {
        super.onResume();
        Timber.d("onResume %s",String.valueOf(RESUMED_ACTIVITIES_COUNT));
        if(RESUMED_ACTIVITIES_COUNT++ == 0) setLaunchTime(getCurrentTime());
    }

    @Override
    protected void onStop() {
        super.onStop();
        Timber.d("onStop %s",String.valueOf(RESUMED_ACTIVITIES_COUNT));
        if(--RESUMED_ACTIVITIES_COUNT == 0) {
            setSessionTime(getCurrentTime() - getLaunchTime());
            sendUserStats();
        }
        if(RESUMED_ACTIVITIES_COUNT < 0) RESUMED_ACTIVITIES_COUNT = 0;
    }

    private void sendUserStats() {
        if(mNetworkStateChecker.isNetworkAvailable()) UserStatsService_.intent(this).start();
    }

    void disableInteraction() {
        mIsInteractionAllowed = false;
    }

    void enableInteraction() {
        mIsInteractionAllowed = true;
    }

}
