package com.balinasoft.clever.network.model;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RatingModel extends DefaultResponseModel{

    @SerializedName("users")
    private List<UserModel> mUsers;

    public List<UserModel> getUsers() {
        return mUsers;
    }

    public class UserModel {
        @SerializedName("username")
        private String mUserName;

        @SerializedName("_id")
        private String mId;

        @SerializedName("coins")
        private int mCoins;

        @SerializedName("score")
        private int mScore;

        @SerializedName("avatar")
        private int mAvatar;

        public int getCoins() {
            return mCoins;
        }

        public int getScore() {
            return mScore;
        }

        public String getUserName() {
            return mUserName;
        }

        public String getId() {
            return mId;
        }

        public int getAvatar() {
            return mAvatar;
        }
    }

}
