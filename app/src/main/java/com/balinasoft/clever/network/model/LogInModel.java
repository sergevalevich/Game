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

    @SerializedName("name")
    private String mName;

    @SerializedName("id")
    private String mId;

    public int getSuccess() {
        return mSuccess;
    }

    public String getMessage() {
        return mMessage;
    }

    public int getError() {
        return mError;
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
