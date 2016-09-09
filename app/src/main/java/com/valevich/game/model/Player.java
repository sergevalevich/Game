package com.valevich.game.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.valevich.game.GameApplication;
import com.valevich.game.R;
import com.valevich.game.util.ConstantsManager;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class Player implements Comparable<Player> , Parcelable{

    private String mName;

    private int mAnswerTime;

    private int mAnswerOption;

    private int mImageResId;

    private int mTotalScore;

    private int mRightAnswersCount;

    private int mTotalCoins;

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
        mTotalCoins = player.getTotalCoins();
    }

    public Player() {}

    protected Player(Parcel in) {
        mName = in.readString();
        mAnswerTime = in.readInt();
        mAnswerOption = in.readInt();
        mImageResId = in.readInt();
        mTotalScore = in.readInt();
        mRightAnswersCount = in.readInt();
        mTotalCoins = in.readInt();
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

    public String getName() {
        return mName;
    }

    public int getAnswerTime() {
        return mAnswerTime;
    }

    private void setAnswerOption(int answerOption) {
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

    public int getTotalCoins() {// TODO: 06.09.2016
        return mTotalCoins;
    }

    public void setTotalCoins(int coins) {
        mTotalCoins = coins;
    }

    public void setAnswer(int rightAnswerPosition) {
        setRandomAnswer(rightAnswerPosition);
        setAnswerTime(getNormalAnswerTime());
        if(getAnswerOption() == rightAnswerPosition) {
            addPoints((ConstantsManager.ROUND_LENGTH - getAnswerTime())/100);
            addRightAnswer();
        }
    }

    public int getCoinsPortion(Player[] players) {
        int totalRightAnswers = 0;
        int totalBet = 0;
        for(Player player:players) {
            if (player != null) {
                totalRightAnswers += player.getTotalRightAnswersCount();
                totalBet += player.getTotalCoins();
            }
        }
        return (getTotalRightAnswersCount()*totalBet)/totalRightAnswers;
    }

    private void setRandomAnswer(int rightAnswerPosition) {
        List<Integer> options = new ArrayList<>(Arrays.asList(
                0,rightAnswerPosition,
                1,rightAnswerPosition,
                2,rightAnswerPosition,
                3,rightAnswerPosition));

        setAnswerOption(options.get(generateAnswerOption(options.size())));
    }

    private static int getNormalAnswerTime() {
        return mAnswerTimeRandom.nextInt(4000) + 4000;
    }

    private static int generateAnswerOption(int max) {
        return mAnswerOptionRandom.nextInt(max);
    }

    public static List<Player> get(int count,int bet) {
        List<Player> enemies = new ArrayList<>(count);
        Collections.shuffle(IMAGES_BY_NAME);
        for (int i = 0; i < count; i++) {
            Player player = new Player();
            player.setName(IMAGES_BY_NAME.get(i).getKey());
            player.setImageResId(IMAGES_BY_NAME.get(i).getValue());
            player.setTotalCoins(bet);
            enemies.add(player);
        }
        return enemies;
    }

    public static Player getUser(int bet) {
        Player user = new Player();
        user.setName(GameApplication.getUserName());
        user.setImageResId(IMAGES_BY_NAME.get(new Random().nextInt(3)).getValue());
        user.setTotalCoins(bet);
        return user;
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
        parcel.writeInt(mTotalCoins);
    }
}
