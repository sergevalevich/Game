package com.valevich.game.model;

import android.support.annotation.NonNull;

import com.valevich.game.R;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Player implements Comparable<Player>{

    private String mName;

    private int mAnswerTime;

    private int mAnswerOption;

    private int mImageResId;

    private static final List<Map.Entry<String,Integer>> IMAGES_BY_NAME = new ArrayList<>(Arrays.asList(
            new AbstractMap.SimpleEntry<>("Плуто", R.drawable.first_man_profile_flat_icon_game),
            new AbstractMap.SimpleEntry<>("GreenSnake", R.drawable.second_man_profile_flat_icon_game),
            new AbstractMap.SimpleEntry<>("Староста", R.drawable.second_girl_profile_flat_icon_game),
            new AbstractMap.SimpleEntry<>("Братишка", R.drawable.third_man_profile_flat_icon_game),
            new AbstractMap.SimpleEntry<>("Маша", R.drawable.first_girl_profile_flat_icon_game)));

    public String getName() {
        return mName;
    }

    public int getAnswerTime() {
        return mAnswerTime;
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

    public void setAnswerTime(int answerTime) {
        mAnswerTime = answerTime;
    }

    public void setAnswerOption(int answerOption) {
        mAnswerOption = answerOption;
    }

    public void setImageResId(int imageResId) {
        mImageResId = imageResId;
    }

    public static List<Player> get(int count) {
        List<Player> enemies = new ArrayList<>(count);
        Collections.shuffle(IMAGES_BY_NAME);
        Random answerOptionRandom = new Random();
        Random answerTimeRandom = new Random();
        for (int i = 0; i < count; i++) {
            Player player = new Player();
            player.setName(IMAGES_BY_NAME.get(i).getKey());
            player.setImageResId(IMAGES_BY_NAME.get(i).getValue());
            player.setAnswerTime(answerOptionRandom.nextInt(9) + 5);
            player.setAnswerOption(answerTimeRandom.nextInt(4));
            enemies.add(player);
        }
        return enemies;
    }


    @Override
    public int compareTo(@NonNull Player player) {
        return getAnswerTime() - player.getAnswerTime();
    }
}
