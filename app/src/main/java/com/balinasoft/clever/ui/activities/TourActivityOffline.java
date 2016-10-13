package com.balinasoft.clever.ui.activities;


import android.view.View;
import android.widget.TextView;

import com.balinasoft.clever.R;
import com.balinasoft.clever.model.Player;
import com.balinasoft.clever.storage.model.Question;
import com.balinasoft.clever.util.ConstantsManager;

import org.androidannotations.annotations.EActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

import static com.balinasoft.clever.GameApplication.getUserCoins;
import static com.balinasoft.clever.GameApplication.setUserCoins;
import static com.balinasoft.clever.util.ConstantsManager.COUNTDOWN_INTERVAL_NORMAL;


@EActivity(R.layout.activity_tour)
public class TourActivityOffline extends TourActivityBase {

    List<List<Integer>> mAnswersRatios = new ArrayList<>();

    @Override
    void takeCoins() {
        setUserCoins(getUserCoins() - bet);
    }

    @Override
    Player createUser() {////////////////////////
        return Player.getUser(bet,false);
    }

    @Override
    String getMediaLocation() {
        return getFilesDir() + File.separator + mCurrentQuestion.getMediaPath();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCurrentQuestion == null) bindData();
        else if(mCurrentMillisecond.get() > 0) startTimer();
    }

    @Override
    protected void onPause() {
        if (mSubscription != null && !mSubscription.isUnsubscribed()) mSubscription.unsubscribe();
        super.onPause();
    }

    @Override
    void onTick(long lap) {
        super.onTick(lap);
        if (!hasEnemiesAnswered()) checkIfEnemiesAnswered();
    }

    @Override
    void bindData() {
        setAnswersRatioCombinations();
        super.bindData();
        setEnemiesAnswers();
    }

    @Override
    boolean isBoost() {
        return hasUserAnswered();
    }

    @Override
    void acceptAnswer(TextView option) {/////
        super.acceptAnswer(option);
        boostTimer();
    }

    @Override
    void onRoundReset() {

        List<Player> players = new ArrayList<>();
        players.add(new Player(mUser));
        for (Player enemy : mEnemies) {
            Player player = new Player(enemy);
            players.add(player);
        }

        Collections.sort(players, (player1, player2) -> player2.getTotalScore() - player1.getTotalScore());

        if (mCurrentTour == 4) {
            Player.countCoins(players);
        }

        Player[] pls = new Player[players.size()];
        pls = players.toArray(pls);

        ResultsActivity_.intent(this).parcelablePlayers(pls).tourNumber(mCurrentTour - 1).start();
    }

    void resetQuestion() {
        saveQuestionInfo();
        clearAnswerRatios();
        super.resetQuestion();
    }

    private void setEnemiesAnswers() {
        Timber.d("SETTING ANSWERS");
        for (Player enemy : mEnemies) {
            enemy.setAnswerBy(mRightAnswerPosition);
        }
    }

    private void clearAnswerRatios() {
        mAnswersRatios.clear();
    }


    private void saveQuestionInfo() {
        Question currentQuestion = (Question) mCurrentQuestion;
        if (currentQuestion != null) {
            currentQuestion.setIsPlayed(1);
            currentQuestion.setIsRightAnswered(!hasUserAnswered() || !isUserAnswerCorrect() ? 0 : 1);
            double answerTime = mUser.getAnswerTime();
            currentQuestion.setAnswerTime(answerTime / 1000);
            currentQuestion.update();
            mCurrentQuestion = null;
        }
    }

    private void checkIfEnemiesAnswered() {
        int interval = isBoost()
                ? ConstantsManager.COUNTDOWN_INTERVAL_BOOST * ConstantsManager.SPEED_BOOST
                : COUNTDOWN_INTERVAL_NORMAL;
        for (int i = 0; i < mEnemies.size(); i++) {
            Player enemy = mEnemies.get(i);
            if (enemy.getAnswerTime() / interval == mCurrentMillisecond.get() / interval) {
                TextView indicator = mEnemyAnswerIndicators.get(mEnemiesPositions[i]);
                indicator.setText(String.valueOf(++mPlayersAnsweredCount));
                indicator.setVisibility(View.VISIBLE);

                updateAnswersRatio();
            }
        }
    }

    private void updateAnswersRatio() {
        int size = mAnswersRatios.size();
        if (size != 0) {
            List<Integer> ratios = mAnswersRatios.remove(0);
            for (int i = 0; i < mOptionPercents.size(); i++) {
                TextView percent = mOptionPercents.get(i);
                percent.setText(String.format(Locale.getDefault(), "%d%s", ratios.get(i), "%"));
            }
        }
    }

    private void setAnswersRatioCombinations() {
        List<Integer> startRatio = new ArrayList<>(Arrays.asList(0, 0, 0, 0));
        List<Player> copyEnemies = new ArrayList<>(mEnemies);
        Collections.sort(copyEnemies);
        for (int k = 0; k < copyEnemies.size(); k++) {
            Player enemy = copyEnemies.get(k);
            int enemyOption = enemy.getAnswerOption();
            startRatio.set(enemyOption, startRatio.get(enemyOption) + 1);

            List<Integer> outRatio = new ArrayList<>();
            for (int i = 0; i < startRatio.size(); i++) {
                outRatio.add((startRatio.get(i) * 100 / (k + 1)));
            }

            mAnswersRatios.add(outRatio);

        }
    }

}
