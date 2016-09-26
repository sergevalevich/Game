package com.balinasoft.clever.network.model;

import com.google.gson.annotations.SerializedName;

public class DefaultResponseModel {
    @SerializedName("success")
    private int mSuccess;

    @SerializedName("error")
    private int mError;

    @SerializedName("message")
    private String mMessage;

    public int getSuccess() {
        return mSuccess;
    }

    public String getMessage() {
        return mMessage;
    }
}
