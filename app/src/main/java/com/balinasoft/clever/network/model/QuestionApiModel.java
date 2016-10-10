package com.balinasoft.clever.network.model;


import android.os.Parcel;
import android.os.Parcelable;

import com.balinasoft.clever.model.IQuestion;
import com.google.gson.annotations.SerializedName;

public class QuestionApiModel implements Parcelable, IQuestion{

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

    private int mRound;

    private int mNumber;

    private QuestionApiModel(Parcel in) {
        mThemeQuest = in.readString();
        mTextQuest = in.readString();
        mMediaType = in.readString();
        mMedia = in.readString();
        mAnswers = in.readString();
        mRightAnswer = in.readString();
        mServerId = in.readString();
        mRound = in.readInt();
        mNumber = in.readInt();
    }

    public QuestionApiModel() {}

    public static final Creator<QuestionApiModel> CREATOR = new Creator<QuestionApiModel>() {
        @Override
        public QuestionApiModel createFromParcel(Parcel in) {
            return new QuestionApiModel(in);
        }

        @Override
        public QuestionApiModel[] newArray(int size) {
            return new QuestionApiModel[size];
        }
    };
    
    @Override
    public String getMediaPath() {
        return mMedia;
    }

    public String getMedia() {
        return mMedia;
    }

    public int getRound() {
        return mRound;
    }

    public void setRound(int round) {
        mRound = round;
    }

    public int getNumber() {
        return mNumber;
    }

    public void setNumber(int number) {
        mNumber = number;
    }

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

    public void setThemeQuest(String themeQuest) {
        mThemeQuest = themeQuest;
    }

    public void setTextQuest(String textQuest) {
        mTextQuest = textQuest;
    }

    public void setMediaType(String mediaType) {
        mMediaType = mediaType;
    }

    public void setMedia(String media) {
        mMedia = media;
    }

    public void setAnswers(String answers) {
        mAnswers = answers;
    }

    public void setRightAnswer(String rightAnswer) {
        mRightAnswer = rightAnswer;
    }

    public void setServerId(String serverId) {
        mServerId = serverId;
    }

    public String[] getOptions() {
        return mAnswers.split(",");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mThemeQuest);
        parcel.writeString(mTextQuest);
        parcel.writeString(mMediaType);
        parcel.writeString(mMedia);
        parcel.writeString(mAnswers);
        parcel.writeString(mRightAnswer);
        parcel.writeString(mServerId);
        parcel.writeInt(mRound);
        parcel.writeInt(mNumber);
    }
}
