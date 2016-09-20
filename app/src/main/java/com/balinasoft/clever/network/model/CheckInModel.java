package com.balinasoft.clever.network.model;

import com.google.gson.annotations.SerializedName;

public class CheckInModel {
    @SerializedName("device_token")
    private String mDeviceToken;

    public void setDeviceToken(String deviceToken) {
        mDeviceToken = deviceToken;
    }
}
