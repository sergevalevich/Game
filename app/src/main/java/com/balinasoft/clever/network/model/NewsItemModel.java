package com.balinasoft.clever.network.model;


import com.google.gson.annotations.SerializedName;

public class NewsItemModel {

    @SerializedName("title")
    private String mTitle;

    @SerializedName("text")
    private String mText;

    @SerializedName("image")
    private String mImage;

    @SerializedName("_id")
    private String mId;

    @SerializedName("date")
    private String mDate;

    public String getTitle() {
        return mTitle;
    }

    public String getText() {
        return mText;
    }

    public String getImage() {
        return mImage;
    }

    public String getId() {
        return mId;
    }

    public String getDate() {
        return mDate;
    }
}
