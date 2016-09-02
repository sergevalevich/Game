package com.valevich.game.util;

import org.androidannotations.annotations.sharedpreferences.DefaultBoolean;
import org.androidannotations.annotations.sharedpreferences.SharedPref;

@SharedPref(value=SharedPref.Scope.UNIQUE)
public interface Preferences {

    @DefaultBoolean(value = true)
    boolean isSyncNeeded();

}
