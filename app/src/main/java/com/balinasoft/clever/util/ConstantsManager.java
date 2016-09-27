package com.balinasoft.clever.util;


import com.balinasoft.clever.model.Player;
import com.balinasoft.clever.model.Room;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public interface ConstantsManager {
    String BASE_URL = "https://clever.balinasoft.com/";
    int CONNECTION_TIME_OUT = 30;
    int READ_TIME_OUT = 50;

    int [] ENEMY_ONE = new int[]{2}; //center image
    int [] ENEMY_TWO = new int[]{1,3}; //right/left to the center
    int [] ENEMY_THREE = new int[]{1,2,3}; //3 center
    int [] ENEMY_FOUR = new int[]{0,1,3,4}; //4 from the start
    int [] ENEMY_FIVE = new int[]{0,1,2,3,4}; // all

    int ROUND_LENGTH = 25000;//25 seconds
    int COUNTDOWN_INTERVAL_NORMAL = 50;//millis
    int COUNTDOWN_INTERVAL_BOOST = 25;
    int SPEED_BOOST = 10;

    int MAX_QUESTIONS_COUNT = 21;
    int ROUND_QUESTIONS_COUNT = 7;

    String DEFAULT_USER_NAME = "No_Name";
    String CONGRATS_START = "Поздравляем с";
    String CONGRATS_END = "местом!";
    String CONGRATS_ENDING = "о";

    int DEFAULT_ENEMIES_COUNT = 3;
    int DEFAULT_BET = 1;
    int INIT_SCORE = 100;
    int MAX_BONUS = 30;

    String AVATAR_DIALOG_TAG = "choose_image";

    String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    String DEFAULT_USER_ID = "user_id";

    String DEVICE_ALREADY_CHECKED_IN_MSG = "Это устройство уже отмечено на сервере.";

    List<String> FACEBOOK_PERMISSIONS = new ArrayList<>(Collections.singletonList("public_profile"));

    List<Room> STUB_ROOMS = new ArrayList<>(Arrays.asList(
            new Room(Player.get(4,10),10,Player.getUser(10)),
            new Room(Player.get(2,30),30,Player.getUser(30)),
            new Room(Player.get(4,10),10,Player.getUser(10)),
            new Room(Player.get(2,30),30,Player.getUser(30)),
            new Room(Player.get(4,10),10,Player.getUser(10)),
            new Room(Player.get(2,30),30,Player.getUser(30)),
            new Room(Player.get(2,30),30,Player.getUser(30)),
            new Room(Player.get(4,10),10,Player.getUser(10)),
            new Room(Player.get(3,20),20,Player.getUser(20))));
}
