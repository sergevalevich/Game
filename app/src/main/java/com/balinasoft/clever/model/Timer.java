package com.balinasoft.clever.model;


public class Timer {

    private TickHandler mTickHandler;

    private long mCurrentMillis = 0;

    public void start(long interval, long length) {
        long startTime = System.currentTimeMillis();
        long prevMillis = 0;
        while (mCurrentMillis - startTime < length) {
            mCurrentMillis = System.currentTimeMillis();
            if (mCurrentMillis != prevMillis && mCurrentMillis % interval == 0) {
                mTickHandler.onTick(mCurrentMillis);
            }
            prevMillis = mCurrentMillis;
        }
    }

    public long getCurrentMillis() {
        return mCurrentMillis;
    }
}
