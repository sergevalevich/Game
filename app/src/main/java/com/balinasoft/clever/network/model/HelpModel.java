package com.balinasoft.clever.network.model;

import com.google.gson.annotations.SerializedName;

public class HelpModel extends DefaultResponseModel {

    @SerializedName("help")
    private Help mHelp;

    private class Help {
        @SerializedName("text")
        private String mText;

        @SerializedName("title")
        private String mTitle;

        @SerializedName("image")
        private String mImage;

        @SerializedName("id")
        private int mId;

        public String getText() {
            return mText;
        }

        public String getTitle() {
            return mTitle;
        }

        public String getImage() {
            return mImage;
        }

        public int getId() {
            return mId;
        }
    }

    public Help getHelp() {
        return mHelp;
    }
}
