package com.balinasoft.clever.network.model;

import com.google.gson.annotations.SerializedName;

public class QuestionsStatsModel {

    @SerializedName("num_of_answ")
    private int mAnswersCount;

    @SerializedName("num_of_ransw")
    private int mRightAnswersCount;

    @SerializedName("time_for_answ")
    private double mAnswerTime;

    @SerializedName("_id")
    private String mId;

    public void setAnswersCount(int answersCount) {
        mAnswersCount = answersCount;
    }

    public void setRightAnswersCount(int rightAnswersCount) {
        mRightAnswersCount = rightAnswersCount;
    }

    public void setId(String id) {
        mId = id;
    }

    public void setAnswerTime(double answerTime) {
        mAnswerTime = answerTime;
    }
}
