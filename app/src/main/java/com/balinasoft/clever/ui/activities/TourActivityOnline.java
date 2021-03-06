package com.balinasoft.clever.ui.activities;

import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

import com.balinasoft.clever.R;
import com.balinasoft.clever.model.Player;
import com.balinasoft.clever.util.ConstantsManager;
import com.crashlytics.android.Crashlytics;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.res.StringRes;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import io.socket.client.Socket;
import timber.log.Timber;

import static com.balinasoft.clever.GameApplication.getOnlineCoins;
import static com.balinasoft.clever.GameApplication.getOnlineScore;
import static com.balinasoft.clever.GameApplication.getSocket;
import static com.balinasoft.clever.GameApplication.getUserId;
import static com.balinasoft.clever.GameApplication.setOnlineCoins;
import static com.balinasoft.clever.GameApplication.setOnlineScore;

@EActivity(R.layout.activity_tour)
public class TourActivityOnline extends TourActivityBase {

    @StringRes(R.string.socket_error)
    String mSocketErrorMessage;

    @StringRes(R.string.player_left)
    String mUserLeftMessage;

    private Socket mSocket = getSocket();

    private List<Integer> mAnswers = new ArrayList<>(Arrays.asList(0,0,0,0));

    private Player[] mPlayersWithResults;

    private int mAnswersSum = 0;

    private String mLastLeftId = "";

    private boolean mHasResultsReceived = false;

    @Extra
    int room;

    @Extra
    String idInRoom;

    @Override
    void takeCoins() {
        setOnlineCoins(getOnlineCoins() - bet);
    }

    @UiThread
    void onDisconnect() {
        Timber.e("TourActivity DISCONNECT");
        if (RESUMED_ACTIVITIES_COUNT > 0 && mRootView != null) {
            Snackbar.make(mRootView, mSocketErrorMessage, Snackbar.LENGTH_SHORT)
                    .setCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            finish();
                        }
                    })
                    .show();
        }
    }

    @UiThread
    public void onEnemyAnswered(Object... args) {
        JSONObject data = (JSONObject) args[0];
        Timber.d("enemy answered %s",data.toString());
        try {
            String id = data.getString("id");
            int answer = data.getInt("answer");
            String textAnswer  = data.getString("text_answer");
            if(!textAnswer.isEmpty()) {
                showPlayerAnswered(id, answer,textAnswer);
                updateAnswersRatio(textAnswer);
                if(haveAllAnswered()) boostTimer();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @UiThread
    public void onRoundResultReceived(Object... args) {
        if (!mHasResultsReceived && mCurrentTour < 3) {
            mHasResultsReceived = true;
            JSONObject data = (JSONObject) args[0];
            Timber.d("Round results --- %s", data.toString());
            try {
                setResults(data, false);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    @UiThread
    public void onGameResultReceived(Object... args) {
        Timber.d("game results/should be false, actually %s", String.valueOf(mHasResultsReceived));
        if (!mHasResultsReceived) {
            mHasResultsReceived = true;
            JSONObject data = (JSONObject) args[0];
            Timber.d("Game results --- %s", data.toString());
            try {
                setResults(data, true);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @UiThread
    public void onUserAnswered(Object... args) {
        JSONObject data = (JSONObject) args[0];
        Timber.d("Message in tour %s",data.toString());
        try {
            String message = data.getString("message");
            String id = data.getString("id");
            if(message.equals(mUserLeftMessage) && !id.equals(mLastLeftId)) {
                mLastLeftId = id;
                Player player = new Player();
                player.setId(mLastLeftId);
                mEnemies.remove(player);
            }
        } catch (JSONException ignored) {

        }
        if(haveAllAnswered()) boostTimer();
    }

    @Override
    Player createUser() {////////////////////////
        return Player.getUser(bet,true);
    }

    @Override
    String getMediaLocation() {//////////////////
        return mCurrentQuestion.getMediaPath();
    }

    @Override
    void acceptAnswer(TextView option) {
        super.acceptAnswer(option);
        answer(option.getText().toString());
    }

    @Override
    boolean isBoost() {
        return haveAllAnswered();
    }

    @Override
    void onPlayersAnswered() {
        if(!hasUserAnswered()) answer("");
        super.onPlayersAnswered();
    }

    @Override
    void onRoundReset() {
        Timber.d("round resetting start/should be true, actually %s", String.valueOf(mHasResultsReceived));
        navigateToResults();
        mHasResultsReceived = false;
        Timber.d("round resetting finished");
    }

    void resetQuestion() {
        mCurrentQuestion = null;
        clearAnswers();
        super.resetQuestion();
    }

    @Override
    protected void onResume() {////////
        super.onResume();
        Timber.d("Starting listening");
        startSocketListening();
        if (mCurrentQuestion == null) bindData();
    }

    @Override
    protected void onDestroy() {
        stopSocketListening();
        super.onDestroy();
    }

    private void answer(String answer) {
        JSONObject answerData = new JSONObject();

        try{
            boolean hasAnsweredCorrect = hasUserAnswered() && isUserAnswerCorrect();
            answerData.put("idInRoom",idInRoom);
            answerData.put("id",getUserId());
            answerData.put("round",mCurrentTour);
            answerData.put("quest",mCurrentQuestionNumber);
            answerData.put("score",hasAnsweredCorrect ? (ConstantsManager.ROUND_LENGTH - mUser.getAnswerTime())/100 : 0);
            answerData.put("answer",hasAnsweredCorrect ? 1 : 0);
            answerData.put("room",room);
            answerData.put("text_answer",answer);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Timber.e("answering %s",answerData.toString());
        mSocket.emit(ConstantsManager.QUEST_ANSWER_EVENT,answerData);
    }

    private void startSocketListening() {
        mSocket.on(ConstantsManager.QUEST_ANSWER_EVENT, this::onEnemyAnswered);
        mSocket.on(ConstantsManager.ROUND_FINISHED_EVENT,this::onRoundResultReceived);
        mSocket.on(ConstantsManager.GAME_FINISHED_EVENT,this::onGameResultReceived);
        mSocket.on(ConstantsManager.ROOM_MESSAGE_EVENT,this::onUserAnswered);
        mSocket.on(Socket.EVENT_DISCONNECT, args -> onDisconnect());
    }

    private void stopSocketListening() {
        mSocket.off(ConstantsManager.QUEST_ANSWER_EVENT, this::onEnemyAnswered);
        mSocket.off(ConstantsManager.ROUND_FINISHED_EVENT,this::onRoundResultReceived);
        mSocket.off(ConstantsManager.GAME_FINISHED_EVENT,this::onGameResultReceived);
        mSocket.off(ConstantsManager.ROOM_MESSAGE_EVENT,this::onUserAnswered);
        mSocket.off(Socket.EVENT_DISCONNECT, args -> onDisconnect());
    }

    private void leave() {
        JSONObject roomInfo = new JSONObject();
        try {
            roomInfo.put("room", room);
            Timber.d("leaving.........%s", roomInfo.toString());
            mSocket.emit(ConstantsManager.ROOM_LEAVING_EVENT, roomInfo);
            finish();
        } catch (JSONException ignored) {

        }
    }

    private void updateAnswersRatio(String answer) {
        for(int i = 0; i < mOptionLabels.size(); i++) {
            TextView option = mOptionLabels.get(i);
            Timber.d("ratio %s %s",option.getText().toString(),answer);
            Crashlytics.log(option.getText().toString() + answer);
            if(answer.equals(option.getText().toString())) {
                mAnswers.set(i, mAnswers.get(i) + 1);
                mAnswersSum++;
                break;
            }
        }

        for(int i = 0; i < mOptionPercents.size(); i++) {
            if (mAnswersSum != 0) {
                TextView percentLabel = mOptionPercents.get(i);
                percentLabel.setText(String.format(Locale.getDefault(), "%d%s", (mAnswers.get(i) * 100) / mAnswersSum, "%"));
            } else {
                Timber.e("answer was %s", answer);
            }
        }

    }

    private void showPlayerAnswered(String id, int answer, String textAnswer) {
        for (int i = 0; i < mEnemies.size(); i++) {
            Player enemy = mEnemies.get(i);
            if (enemy.getId().equals(id) && !enemy.getTextAnswer().equals(textAnswer)) {
                Timber.d("answerPassed %s %s",enemy.getTextAnswer(), textAnswer);
                TextView indicator = mEnemyAnswerIndicators.get(mEnemiesPositions[i]);
                indicator.setText(String.valueOf(++mPlayersAnsweredCount));
                indicator.setVisibility(View.VISIBLE);
                enemy.setTextAnswer(textAnswer);
                if (answer == 1) {
                    enemy.setAnswerOption(mRightAnswerPosition);
                }
                else enemy.setAnswerOption(new Random().nextInt(1) + 5);
                break;
            }
        }
    }

    private void clearAnswers() {
        mAnswersSum = 0;
        for(int i = 0; i < mAnswers.size(); i++) {
            mAnswers.set(i,0);
        }
    }

    private List<Player> applyResults(JSONObject data, boolean hasGameFinished) throws JSONException {
        List<Player> players = new ArrayList<>(mEnemies);
        players.add(mUser);
        JSONArray results = data.getJSONArray("result");
        for(int i = 0; i<results.length(); i++) {
            JSONObject playerResult = results.getJSONObject(i);
            for(Player player : players) {
                if(player.getId().equals(playerResult.getString("id"))) {
                    int score = playerResult.getInt("score");
                    player.setTotalScore(score);
                    player.setRightAnswersCount(playerResult.getInt("answers"));
                    if(hasGameFinished) {
                        int coins = playerResult.getInt("coins");
                        player.setCoinsPortion(coins);
                        if(player.getId().equals(getUserId())) {
                            setOnlineCoins(getOnlineCoins() + coins);
                            setOnlineScore(getOnlineScore() + score);
                        }
                    }
                    break;
                }
            }
        }
        sortPlayersByScore(players);
        return players;
    }

    private void sortPlayersByScore(List<Player> players) {
        Collections.sort(players, (player1, player2) -> player2.getTotalScore() - player1.getTotalScore());
    }

    private Player[] getPlayersArray(List<Player> players) {
        Player[] pls = new Player[players.size()];
        return players.toArray(pls);
    }

    private void navigateToResults() {
        ResultsActivity_.intent(this)
                .parcelablePlayers(mPlayersWithResults)
                .tourNumber(mCurrentTour - 1)
                .isOnline(true)
                .start();
        if (mCurrentTour == 4) leave();
    }

    private void setResults(JSONObject data, boolean hasGameFinished) throws JSONException {
        mPlayersWithResults = getPlayersArray(applyResults(data,hasGameFinished));
    }

}
