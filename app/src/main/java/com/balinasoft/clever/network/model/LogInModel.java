package com.balinasoft.clever.network.model;

import com.google.gson.annotations.SerializedName;

public class LogInModel {

    @SerializedName("success")
    private int mSuccess;

    @SerializedName("error")
    private int mError;

    @SerializedName("message")
    private String mMessage;

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

    public int getSuccess() {
        return mSuccess;
    }

    public String getMessage() {
        return mMessage;
    }

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
