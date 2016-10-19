package com.balinasoft.clever.model;


import java.util.List;

public class Room {

    private List<Player> mPlayers;

    private int mBet;

    private int mNumber;

    private int mMaxPlayers;

    public Room(List<Player> players, int bet,int number, int maxPlayers) {
        mPlayers = players;
        mBet = bet;
        mNumber = number;
        mMaxPlayers = maxPlayers;
    }

    public List<Player> getPlayers() {
        return mPlayers;
    }

    public int getBet() {
        return mBet;
    }

    public int getNumber() {
        return mNumber;
    }

    public int getMaxPlayers() {
        return mMaxPlayers;
    }
}
