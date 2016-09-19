package com.balinasoft.clever.eventbus.events;


public class AvatarSelectedEvent {
    private int mAvatarResId;

    public AvatarSelectedEvent(int avatarResId) {
        mAvatarResId = avatarResId;
    }

    public int getAvatarResId() {
        return mAvatarResId;
    }
}
