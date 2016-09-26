package com.balinasoft.clever.network.model;


import com.google.gson.annotations.SerializedName;

public class RegisterModel {
    @SerializedName("success")
    private int mSuccess;

    @SerializedName("error")
    private int mError;

    @SerializedName("message")
    private String mMessage;

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

    public String getName() {
        return mName;
    }

    public String getId() {
        return mId;
    }
}
