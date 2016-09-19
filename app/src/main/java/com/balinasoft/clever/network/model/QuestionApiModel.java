package com.balinasoft.clever.network.model;


import com.google.gson.annotations.SerializedName;

public class QuestionApiModel {

    @SerializedName("theme_quest")
    private String mThemeQuest;

    @SerializedName("text_quest")
    private String mTextQuest;

    @SerializedName("media_type")
    private String mMediaType;

    @SerializedName("media")
    private String mMedia;

    @SerializedName("answers")
    private String mAnswers;

    @SerializedName("right_answer")
    private String mRightAnswer;

    @SerializedName("_id")
    private String mServerId;

    public String getServerId() {
        return mServerId;
    }

    public String getThemeQuest() {
        return mThemeQuest;
    }

    public String getTextQuest() {
        return mTextQuest;
    }

    public String getMediaType() {
        return mMediaType;
    }

    public String getMediaUrl() {
        return mMedia;
    }

    public String getAnswers() {
        return mAnswers;
    }

    public String getRightAnswer() {
        return mRightAnswer;
    }

}
