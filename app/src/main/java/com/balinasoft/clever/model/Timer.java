package com.balinasoft.clever.model;

import java.util.concurrent.atomic.AtomicInteger;

public class Timer {

    private TickHandler mTickHandler;

    private int mInterval;

    private int mLength;

    private AtomicInteger mCurrentMillis = new AtomicInteger();

    public Timer(TickHandler tickHandler, int interval, int length) {
        mTickHandler = tickHandler;
        mInterval = interval;
        mLength = length;
    }

    public void start() {
        long startTime = System.currentTimeMillis();
        long currentTime;
        long prevMillis = 0;
        do {
            currentTime = System.currentTimeMillis();
            if (currentTime != prevMillis && currentTime % mInterval == 0) {
                onTick();
            }
            prevMillis = currentTime;
        } while (currentTime - startTime < mLength);
    }

    public int getCurrentMillis() {
        return mCurrentMillis.get();
    }

    public void setInterval(int interval) {
        mInterval = interval;
    }

    public void setLength(int length) {
        mLength = length;
    }

    private void onTick() {
        if(mTickHandler != null) {
            mTickHandler.onTick(mCurrentMillis.addAndGet(mInterval));
        }
    }
}
