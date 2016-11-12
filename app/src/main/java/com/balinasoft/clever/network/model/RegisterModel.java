package com.balinasoft.clever.network.model;


import com.google.gson.annotations.SerializedName;

public class RegisterModel extends DefaultResponseModel{

    @SerializedName("name")
    private String mName;

    @SerializedName("id")
    private String mId;

    public String getName() {
        return mName;
    }

    public String getId() {
        return mId;
    }
}
