package com.balinasoft.clever.model;


import java.util.List;

public class Room {

    private List<Player> mPlayers;

    private int mBet;

    private int mNumber;

    public Room(List<Player> players, int bet,int number) {
        mPlayers = players;
        mBet = bet;
        mNumber = number;
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
}
