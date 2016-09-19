package com.balinasoft.clever.eventbus.events;

public class UserNameSelectedEvent {
    private String mUserName;

    public UserNameSelectedEvent(String userName) {
        mUserName = userName;
    }

    public String getUserName() {
        return mUserName;
    }
}
