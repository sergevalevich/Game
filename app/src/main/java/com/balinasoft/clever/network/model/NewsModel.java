package com.balinasoft.clever.network.model;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NewsModel {

    @SerializedName("success")
    private int mSuccess;

    @SerializedName("error")
    private int mError;

    @SerializedName("message")
    private String mMessage;

    @SerializedName("news")
    private List<NewsItemModel> mNews;

    public int getSuccess() {
        return mSuccess;
    }

    public String getMessage() {
        return mMessage;
    }

    public List<NewsItemModel> getNews() {
        return mNews;
    }
}
