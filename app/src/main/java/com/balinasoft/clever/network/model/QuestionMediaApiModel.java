package com.balinasoft.clever.network.model;

import com.google.gson.annotations.SerializedName;

public class QuestionMediaApiModel {

    @SerializedName("success")
    private Integer mSuccess;

    @SerializedName("error")
    private String mError;

    public Integer getSuccess() {
        return mSuccess;
    }

    public String getError() {
        return mError;
    }

}
