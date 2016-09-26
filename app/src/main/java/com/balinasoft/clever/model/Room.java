package com.balinasoft.clever.model;


import java.util.List;

public class Room {
    private List<Player> mPlayers;

    private int mBet;

    private Player mHost;

    public Room(List<Player> players, int bet, Player host) {
        mPlayers = players;
        mBet = bet;
        mHost = host;
    }

    public List<Player> getPlayers() {
        return mPlayers;
    }

    public int getBet() {
        return mBet;
    }

    public Player getHost() {
        return mHost;
    }
}
