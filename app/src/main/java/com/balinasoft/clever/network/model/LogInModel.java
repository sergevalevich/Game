package com.balinasoft.clever.network.model;

import com.google.gson.annotations.SerializedName;

public class LogInModel extends DefaultResponseModel {

    @SerializedName("token")
    private String mToken;

    @SerializedName("username")
    private String mName;

    @SerializedName("id")
    private String mId;

    @SerializedName("coins")
    private int mCoins;

    @SerializedName("score")
    private int mScore;

    public int getCoins() {
        return mCoins;
    }

    public int getScore() {
        return mScore;
    }

    public String getToken() {
        return mToken;
    }

    public String getName() {
        return mName;
    }

    public String getId() {
        return mId;
    }
}
