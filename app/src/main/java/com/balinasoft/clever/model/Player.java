package com.balinasoft.clever.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.balinasoft.clever.R;
import com.balinasoft.clever.util.ConstantsManager;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.balinasoft.clever.GameApplication.getOnlineName;
import static com.balinasoft.clever.GameApplication.getUserCoins;
import static com.balinasoft.clever.GameApplication.getUserId;
import static com.balinasoft.clever.GameApplication.getUserImage;
import static com.balinasoft.clever.GameApplication.getUserName;
import static com.balinasoft.clever.GameApplication.getUserScore;
import static com.balinasoft.clever.GameApplication.setUserCoins;
import static com.balinasoft.clever.GameApplication.setUserScore;


public class Player implements Comparable<Player> , Parcelable{

    private String mName;

    private int mAnswerTime;

    private int mAnswerOption;

    private int mImageResId;

    private int mTotalScore;

    private int mRightAnswersCount;

    private int mBet;

    private int mCoinsPortion;

    private String mId;

    private String mTextAnswer;

    private static Random mAnswerOptionRandom = new Random();
    private static Random mAnswerTimeRandom = new Random();

    private static final List<Map.Entry<String,Integer>> IMAGES_BY_NAME = new ArrayList<>(Arrays.asList(
            new AbstractMap.SimpleEntry<>("Плуто", R.drawable.first_man_profile_flat_icon_game),
            new AbstractMap.SimpleEntry<>("GreenSnake", R.drawable.second_man_profile_flat_icon_game),
            new AbstractMap.SimpleEntry<>("Братишка", R.drawable.third_man_profile_flat_icon_game),
            new AbstractMap.SimpleEntry<>("Староста", R.drawable.second_girl_profile_flat_icon_game),
            new AbstractMap.SimpleEntry<>("Маша", R.drawable.first_girl_profile_flat_icon_game)));

    public Player(Player player) {
        mName = player.getName();
        mAnswerTime = player.getAnswerTime();
        mAnswerOption = player.getAnswerOption();
        mImageResId = player.getImageResId();
        mTotalScore = player.getTotalScore();
        mRightAnswersCount = player.getTotalRightAnswersCount();
        mBet = player.getBet();
        mCoinsPortion = player.getCoinsPortion();
        mId = player.getId();
        mTextAnswer = player.getTextAnswer();
    }

    public Player() {}

    private Player(Parcel in) {
        mName = in.readString();
        mAnswerTime = in.readInt();
        mAnswerOption = in.readInt();
        mImageResId = in.readInt();
        mTotalScore = in.readInt();
        mRightAnswersCount = in.readInt();
        mBet = in.readInt();
        mCoinsPortion = in.readInt();
        mId = in.readString();
        mTextAnswer = in.readString();
    }

    public static List<Player> get(int count,int bet) {
        List<Player> enemies = new ArrayList<>(count);
        Collections.shuffle(IMAGES_BY_NAME);
        for (int i = 0; i < count; i++) {
            Player player = new Player();
            player.setName(IMAGES_BY_NAME.get(i).getKey());
            player.setImageResId(IMAGES_BY_NAME.get(i).getValue());
            player.setBet(bet);
            enemies.add(player);
        }
        return enemies;
    }

    public static Player getUser(int bet, boolean isOnline) {
        Player user = new Player();
        user.setName(isOnline ? getOnlineName() : getUserName());
        user.setImageResId(getUserImage());
        user.setBet(bet);
        user.setId(getUserId());
        return user;
    }

    public static final Creator<Player> CREATOR = new Creator<Player>() {
        @Override
        public Player createFromParcel(Parcel in) {
            return new Player(in);
        }

        @Override
        public Player[] newArray(int size) {
            return new Player[size];
        }
    };

    public String getTextAnswer() {
        return mTextAnswer == null ? "" : mTextAnswer;
    }

    public void setTextAnswer(String textAnswer) {
        mTextAnswer = textAnswer;
    }

    public String getId() {
        return mId == null ? ConstantsManager.DEFAULT_PLAYER_ID : mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public int getAnswerTime() {
        return mAnswerTime;
    }

    public void setAnswerOption(int answerOption) {
        mAnswerOption = answerOption;
    }

    public void setAnswerTime(int answerTime) {
        mAnswerTime = answerTime;
    }

    public int getAnswerOption() {
        return mAnswerOption;
    }

    public int getImageResId() {
        return mImageResId;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setImageResId(int imageResId) {
        mImageResId = imageResId;
    }

    public int getTotalScore() {
        return mTotalScore;
    }

    public void addPoints(int points) {
        mTotalScore += points;
    }

    public void addRightAnswer() {
        mRightAnswersCount++;
    }

    public int getTotalRightAnswersCount() {
        return mRightAnswersCount;
    }

    private int getBet() {
        return mBet;
    }

    private void setBet(int coins) {
        mBet = coins;
    }

    public void setAnswerBy(int rightAnswerPosition) {
        setRandomAnswer(rightAnswerPosition);
        setAnswerTime(getNormalAnswerTime());
        if(getAnswerOption() == rightAnswerPosition) {
            addPoints((ConstantsManager.ROUND_LENGTH - getAnswerTime())/100);
            addRightAnswer();
        }
    }

    public int getCoinsPortion() {
        return mCoinsPortion;
    }

    public void setTotalScore(int totalScore) {
        mTotalScore = totalScore;
    }

    public void setCoinsPortion(int coinsPortion) {
        mCoinsPortion = coinsPortion;
    }

    public void setRightAnswersCount(int rightAnswersCount) {
        mRightAnswersCount = rightAnswersCount;
    }

    public static void countCoins(List<Player> players) {
        float totalRightAnswers = 0;
        int totalBet = 0;
        for (Player player : players) {
            if (player != null) {
                totalRightAnswers += player.getTotalRightAnswersCount();
                totalBet += player.getBet();
            }
        }
        int portionSum = 0;
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            int portion;
            if (i == players.size() - 1) portion = totalBet - portionSum;
            else {
                portion = Math.round((player.getTotalRightAnswersCount() * totalBet) / totalRightAnswers);
                portionSum += portion;
            }
            if(player.getId().equals(getUserId())) {
                setUserCoins(getUserCoins() + portion);
                setUserScore(getUserScore() + player.getTotalScore());
            }
            player.setCoinsPortion(portion);
        }
    }

    @Override
    public int compareTo(@NonNull Player player) {
        return mAnswerTime - player.getAnswerTime();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mName);
        parcel.writeInt(mAnswerTime);
        parcel.writeInt(mAnswerOption);
        parcel.writeInt(mImageResId);
        parcel.writeInt(mTotalScore);
        parcel.writeInt(mRightAnswersCount);
        parcel.writeInt(mBet);
        parcel.writeInt(mCoinsPortion);
        parcel.writeString(mId);
        parcel.writeString(mTextAnswer);
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null) {
            return false;
        }
        if (!Player.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final Player other = (Player) obj;

        return mId.equals(other.getId());
    }

    private void setRandomAnswer(int rightAnswerPosition) {
        List<Integer> options = new ArrayList<>(Arrays.asList(rightAnswerPosition,
                0,rightAnswerPosition,
                1,rightAnswerPosition,
                2,rightAnswerPosition,
                3,rightAnswerPosition));

        setAnswerOption(options.get(generateAnswerOption(options.size())));
    }

    private static int getNormalAnswerTime() {
        return mAnswerTimeRandom.nextInt(4000) + 3000;
    }

    private static int generateAnswerOption(int max) {
        return mAnswerOptionRandom.nextInt(max);
    }

}
