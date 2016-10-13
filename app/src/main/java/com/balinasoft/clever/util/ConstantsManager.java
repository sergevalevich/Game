package com.balinasoft.clever.util;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface ConstantsManager {
    //String BASE_URL = "https://clever.balinasoft.com/";
    String BASE_URL = "http://91.107.105.245:3000/";
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

    String DEFAULT_USER_NAME = "OFFLINE";
    String DEFAULT_USER_NAME_ONLINE = "ONLINE";
    String CONGRATS_START = "Поздравляем с";
    String CONGRATS_END = "местом!";
    String CONGRATS_ENDING = "о";

    int DEFAULT_ENEMIES_COUNT = 3;
    int DEFAULT_BET = 1;
    int INIT_COINS = 100;
    int INIT_SCORE = 0;
    int MAX_BONUS = 30;

    String AVATAR_DIALOG_TAG = "choose_image";

    String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    String DEFAULT_USER_ID = "user_id";

    List<String> FACEBOOK_PERMISSIONS = new ArrayList<>(Arrays.asList("public_profile","email"));

    String[] VK_PERMISSIONS = new String[]{"email"};

    String JOIN_ROOM_EVENT = "joinRoom";
    String ROOM_MESSAGE_EVENT = "roomMessage";
    String ROOM_INFO_EVENT = "roomInfo";
    String AUTH_EVENT = "auth";
    String CREATE_ROOM_EVENT = "createRoom";
    String ROOMS_LIST_GET_EVENT = "listRooms";
    String ROOM_LEAVING_EVENT = "leaveRoom";
    String START_GAME_EVENT = "startGame";
    String QUEST_ANSWER_EVENT = "answerTheQuest";
    String ROUND_FINISHED_EVENT = "roundResult";
    String GAME_FINISHED_EVENT = "gameResult";

    String SOCIAL_PASS = "socialpasswordforcheckingifuserisregisteredwithsocialnetworks";

    String OS_NAME = "android";

    String DEFAULT_PLAYER_ID = "player_id";
    String COINS_FILTER = "coins";
    String SCORE_FILTER = "score";
    int QUESTION_MAX_LENGTH = 240;
    int QUESTION_MAX_LENGTH_BIG = 280;
    int QUESTION_MAX_LENGTH_LARGE = 320;
    int OPTION_MAX_LENGTH = 20;
    int OPTION_MAX_LENGTH_BIG = 30;
    int OPTION_MAX_LENGTH_LARGE = 40;

    String LOREM = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.";
    String CAT = "http://www.unionsnobs.com/assets/images/cat-image2.png";

    String GOOGLE_PLAY_LINK = "https://play.google.com/store/apps/details?id=com.balinasoft.clever";
}
