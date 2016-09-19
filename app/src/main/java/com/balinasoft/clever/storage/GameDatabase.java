package com.balinasoft.clever.storage;

import com.raizlabs.android.dbflow.annotation.Database;

@Database(name = GameDatabase.NAME, version = GameDatabase.VERSION)
public class GameDatabase {
    public static final String NAME = "game";

    public static final int VERSION = 1;
}

