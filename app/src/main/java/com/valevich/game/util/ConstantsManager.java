package com.valevich.game.util;


public interface ConstantsManager {
    String BASE_URL = "http://91.107.105.245:3000";
    int CONNECTION_TIME_OUT = 30;
    int READ_TIME_OUT = 50;

    int [] ENEMY_ONE = new int[]{2}; //center image
    int [] ENEMY_TWO = new int[]{1,3}; //right/left to the center
    int [] ENEMY_THREE = new int[]{1,2,3}; //3 center
    int [] ENEMY_FOUR = new int[]{0,1,3,4}; //4 from the start
    int [] ENEMY_FIVE = new int[]{0,1,2,3,4}; // all

    int ROUND_LENGTH = 25000;//25 seconds
    int COUNTDOWN_INTERVAL_NORMAL = 50;//millis
    int COUNTDOWN_INTERVAL_BOOST = 20;
    int SPEED_BOOST = 15;

    int MAX_QUESTIONS_COUNT = 21;
    int ROUND_QUESTIONS_COUNT = 7;

    String DEFAULT_USER_NAME = "No_Name";

    String UPDATE_QUESTIONS_JOB_TAG = "job_demo_tag";
    int DEFAULT_ENEMIES_COUNT = 3;
    int DEFAULT_BET = 1;
}
