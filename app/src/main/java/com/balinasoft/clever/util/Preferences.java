package com.balinasoft.clever.util;

import com.balinasoft.clever.R;

import org.androidannotations.annotations.sharedpreferences.DefaultBoolean;
import org.androidannotations.annotations.sharedpreferences.DefaultInt;
import org.androidannotations.annotations.sharedpreferences.DefaultLong;
import org.androidannotations.annotations.sharedpreferences.DefaultString;
import org.androidannotations.annotations.sharedpreferences.SharedPref;

@SharedPref(value=SharedPref.Scope.UNIQUE)
public interface Preferences {

    @DefaultString(value = ConstantsManager.DEFAULT_USER_NAME)
    String userName();

    @DefaultInt(value = R.drawable.first_man_profile_flat_icon_med)
    int userImage();

    @DefaultInt(value = 0)
    int userScore();

    @DefaultInt(value = 0)
    int userCoins();

    @DefaultString(value = "")
    String lastUpdate();

    @DefaultInt(value = 0)
    int lastAppLaunchDay();

    @DefaultInt(value = 0)
    int bonus();

    //stats//remove//
    @DefaultBoolean(value = false)
    boolean isUserCheckedIn();

    @DefaultLong(value = 0)
    long sessionLength();

    @DefaultLong(value = 0)
    long launchTime();
}