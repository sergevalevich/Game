package com.valevich.game.network.model;

import com.google.gson.annotations.SerializedName;

public class LastUpdateModel {
    @SerializedName("_id")
    private String mId;

    @SerializedName("last_update")
    private String mLastUpdate;

    public String getId() {
        return mId;
    }

    public LastUpdateModel(String id) {
        mId = id;
    }

//    public String getLastUpdate() {
//        return mLastUpdate;
//    }
}
