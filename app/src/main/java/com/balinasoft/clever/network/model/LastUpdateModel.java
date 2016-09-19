package com.balinasoft.clever.network.model;

import com.google.gson.annotations.SerializedName;

public class LastUpdateModel {
    @SerializedName("_id")
    private String mId;

    @SerializedName("last_update")
    private String mLastUpdate;

    public LastUpdateModel(String lastUpdate) {
        mLastUpdate = lastUpdate;
    }

    public String getLastUpdate() {
        return mLastUpdate;
    }
}
