package com.balinasoft.clever.network.model;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NewsModel extends DefaultResponseModel{

    @SerializedName("news")
    private List<NewsItemModel> mNews;

    public List<NewsItemModel> getNews() {
        return mNews;
    }
}
