package com.balinasoft.clever.network.model;

import com.google.gson.annotations.SerializedName;

public class UserStatsModel {

    @SerializedName("device_token")
    private String mDeviceToken;

    @SerializedName("time_session")
    private int mSessionTime;

    @SerializedName("coins")
    private int mUserCoins;

    @SerializedName("score")
    private int mUserScore;

    @SerializedName("entry_time")
    private String mLaunchTime;

    public void setDeviceToken(String deviceToken) {
        mDeviceToken = deviceToken;
    }

    public void setSessionTime(int sessionTime) {
        mSessionTime = sessionTime;
    }

    public void setUserCoins(int userCoins) {
        mUserCoins = userCoins;
    }

    public void setUserScore(int userScore) {
        mUserScore = userScore;
    }

    public void setLaunchTime(String launchTime) {
        mLaunchTime = launchTime;
    }
}
