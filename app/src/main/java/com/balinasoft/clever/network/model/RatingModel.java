package com.balinasoft.clever.network.model;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RatingModel {

    @SerializedName("success")
    private int mSuccess;

    @SerializedName("error")
    private int mError;

    @SerializedName("message")
    private String mMessage;

    @SerializedName("users")
    private List<UserModel> mUsers;

    public int getSuccess() {
        return mSuccess;
    }

    public String getMessage() {
        return mMessage;
    }

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

    }

//    private class UserModelScore extends UserModel {
//        @SerializedName("coins")
//        private int mCoins;
//
//        public int getCoins() {
//            return mCoins;
//        }
//    }
//
//    private class UserModelScore extends UserModel {
//        @SerializedName("coins")
//        private int mCoins;
//
//        public int getCoins() {
//            return mCoins;
//        }
//    }
}
