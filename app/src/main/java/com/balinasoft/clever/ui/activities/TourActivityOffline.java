package com.balinasoft.clever.ui.activities;


import android.content.res.Resources;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.balinasoft.clever.R;
import com.balinasoft.clever.model.Player;
import com.balinasoft.clever.util.ConstantsManager;
import com.bumptech.glide.Glide;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

import static com.balinasoft.clever.util.ConstantsManager.COUNTDOWN_INTERVAL_NORMAL;


@EActivity(R.layout.activity_tour)
public class TourActivityOffline extends TourActivityBase {

    @Override
    protected void onResume() {
        super.onResume();
        if (mCurrentQuestion == null) bindData();
        else startTimer(hasUserAnswered() ? ConstantsManager.COUNTDOWN_INTERVAL_BOOST : ConstantsManager.COUNTDOWN_INTERVAL_NORMAL);
    }

    @Override
    protected void onStop() {
        if (mSubscription != null && !mSubscription.isUnsubscribed()) mSubscription.unsubscribe();
        super.onStop();
    }

    @Click(R.id.question_image)
    void onImageClicked() {
        if (mQuestionImage.getVisibility() == View.VISIBLE) showScaledImage();
    }

    @Click(R.id.scaled_image_area)
    void onScaledAreaClicked() {
        hideScaledImage();
    }

    @Override
    public void onBackPressed() {
    }

    private void setUpDefaultAnswersRatio() {
        for (TextView percent : mOptionPercents) {
            percent.setText("0%");
        }
    }

    private void hideScaledImage() {
        mScaledImageArea.setVisibility(View.GONE);
    }

    private void showScaledImage() {
        mScaledImageArea.setVisibility(View.VISIBLE);
    }

    private void bindData() {
        setUpSupportingLabels();
        setUpTimerViews();
        setQuestion();
        setQuestionCategory();
        setOptions();
        setEnemiesAnswers();
        setAnswersRatioCombinations();
        setQuestionContent();
        showQuestion();
    }

    private void setQuestionContent() {
        if (isMediaQuestion()) setUpMediaQuestion();
        else setUpTextQuestion();
    }

    private boolean isMediaQuestion() {
        return mCurrentQuestion.getMediaPath() != null;
    }

    private void setUpMediaQuestion() {
        String path = getFilesDir() + File.separator + mCurrentQuestion.getMediaPath();
        int cornerRadius = (int) (16 / Resources.getSystem().getDisplayMetrics().density);
        Glide.with(this).load(path).crossFade()
                .bitmapTransform(new RoundedCornersTransformation(this, cornerRadius, 0))
                .into(mQuestionImage);
        Glide.with(this).load(path).crossFade().into(mScaledImage);
        mQuestionImage.setVisibility(View.VISIBLE);
        mQuestionLabelBottom.setText(mCurrentQuestion.getTextQuest());
        mQuestionLabelBottom.setVisibility(View.VISIBLE);
    }

    private void setUpTextQuestion() {
        mQuestionLabel.setText(mCurrentQuestion.getTextQuest());
        mQuestionLabel.setVisibility(View.VISIBLE);
    }

    private void setQuestionCategory() {
        mCategoryLabel.setText(mCurrentQuestion.getThemeQuest());
    }

    private void setEnemiesAnswers() {
        for (Player enemy : mEnemies) {
            enemy.setAnswerBy(mRightAnswerPosition);
        }
    }

    private void setOptions() {
        List<Integer> positions = new ArrayList<>(Arrays.asList(0, 1, 2, 3));
        Collections.shuffle(positions);
        String[] answers = mCurrentQuestion.getFormattedAnswers();
        for (int i = 0; i < mOptionLabels.size() - 1; i++) {
            TextView option = mOptionLabels.get(positions.remove(0));
            option.setText(answers[i]);
        }

        mRightAnswerPosition = positions.remove(0);
        TextView rightOption = mOptionLabels.get(mRightAnswerPosition);
        rightOption.setText(mCurrentQuestion.getRightAnswer());
    }

    private void setUpHints() {
        if (!mUsedHints.get(0)) mHints.get(0).setOnClickListener(view -> showRightAnswerHint());
        if (!mUsedHints.get(1)) mHints.get(1).setOnClickListener(view -> showAudienceHelpHint());
        if (!mUsedHints.get(2)) mHints.get(2).setOnClickListener(view -> showFiftyFiftyHint());
    }

    private void setUpTimerViews() {
        mTimerLabel.setText(String.valueOf(ConstantsManager.ROUND_LENGTH / 1000));
        mTimerView.setProgress(0);
    }

    private void showQuestion() {
        Animation slideDownAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        slideDownAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                showOptions();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        toggleQuestion(slideDownAnimation, View.VISIBLE);
    }

    private void hideQuestion() {
        Timber.d("hide question");
        Animation slideUpQuestionAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_up_fast);
        slideUpQuestionAnimation.setStartOffset(2000);

        List<Integer> visibleOptions = getVisibleOptions(mOptionLabels);
        List<Animation> slideUps = new ArrayList<>(visibleOptions.size());
        for (int i = 0; i < visibleOptions.size(); i++) {
            slideUps.add(AnimationUtils.loadAnimation(this, R.anim.slide_up_fast));
        }

        setOptionSlideUpActions(slideUps,visibleOptions);
        setQuestionSlideUpAction(slideUpQuestionAnimation,visibleOptions.get(0),slideUps.get(0));
        toggleQuestion(slideUpQuestionAnimation, View.INVISIBLE);
    }

    private void setQuestionSlideUpAction(Animation slideUpQuestionAnimation, int firstOptionPosition, Animation optionAnimation) {
        slideUpQuestionAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Timber.d("On Question slideUp Finished");
                mQuestionImage.setVisibility(View.GONE);
                mQuestionLabelBottom.setVisibility(View.GONE);
                mQuestionLabel.setVisibility(View.GONE);
                toggleOption(firstOptionPosition, optionAnimation, View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void setOptionSlideUpActions(List<Animation> slideUps, List<Integer> visibleOptions) {
        for (int i = 0; i < slideUps.size(); i++) {
            Animation slideUp = slideUps.get(i);
            final int nextVisibleOption = i + 1;
            slideUp.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if(nextVisibleOption == slideUps.size()) {
                        resetQuestion();
                    }
                    else toggleOption(visibleOptions.get(
                            nextVisibleOption),
                            slideUps.get(nextVisibleOption),
                            View.INVISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
    }

    private void setOptionSlideDownActions(List<Animation> slideDowns) {
        for(int i = 0; i < slideDowns.size(); i++) {
            Animation slideDown = slideDowns.get(i);
            int nextOptionPosition = i+1;
            slideDown.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if(nextOptionPosition == slideDowns.size()) {
                        startTimer(COUNTDOWN_INTERVAL_NORMAL);
                        unBlockOptions();
                        unBlockImage();
                        setUpHints();
                    } else {
                        toggleOption(nextOptionPosition, slideDowns.get(nextOptionPosition), View.VISIBLE);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
    }

    private void showOptions() {
        List<Animation> slideDowns = new ArrayList<>();
        for (int i = 0; i < mOptionLabels.size(); i++) {
            slideDowns.add(AnimationUtils.loadAnimation(this,R.anim.slide_down_fast));
        }
        setOptionSlideDownActions(slideDowns);
        toggleOption(0, slideDowns.get(0), View.VISIBLE);
    }

    private void toggleQuestion(Animation animation, int visibility) {
        if (visibility == View.INVISIBLE) {
            Timber.d("toggle question");
            mQuestionArea.startAnimation(animation);
            mQuestionArea.setVisibility(visibility);
        } else {
            mQuestionArea.setVisibility(visibility);
            mQuestionArea.startAnimation(animation);
        }
    }

    private void toggleOption(int position, Animation animation, int visibility) {
        TextView option = mOptionLabels.get(position);
        option.setVisibility(visibility);
        option.startAnimation(animation);
    }

    private void onPlayersAnswered() {
        showEnemiesResults();
        hideScaledImage();
        if (!hasUserAnswered()) {
            disableHints();
            blockOptions();
            blockImage();
            showRightAnswer();
        }
        hidePercentage();
        hideQuestion();
    }

    private void resetQuestion() {
        clearOptions();
        saveQuestionInfo();
        clearAnswersRatios();
        dropEnemiesIndicators();
        dropTime();
        dropRightAnsweredCount();
        clearUserSelection();
        if (isLastQuestion()) {
            resetRound();
            return;
        }
        addQuestionCount();
        bindData();
    }

    private void dropTime() {
        mCurrentMillisecond.set(0);
    }

    private void showEnemiesResults() {
        for (int i = 0; i < enemiesCount; i++) {
            Player enemy = mEnemies.get(i);
            TextView indicator = mEnemyAnswerIndicators.get(mEnemiesPositions[i]);
            indicator.setText("");
            indicator.setBackgroundResource(enemy.getAnswerOption() == mRightAnswerPosition
                    ? R.drawable.green
                    : R.drawable.red);
        }
    }

    private boolean isLastQuestion() {
        return mCurrentQuestionNumber == ConstantsManager.ROUND_QUESTIONS_COUNT;
    }

    private void addQuestionCount() {
        mCurrentQuestionNumber++;
    }

    private void clearAnswersRatios() {
        mAnswersRatios.clear();
    }

    private void dropRightAnsweredCount() {
        mPlayersAnsweredCount = 0;
    }

    private void clearUserSelection() {
        mUserOptionLabel = null;
    }

    private void dropEnemiesIndicators() {
        for (int i = 0; i < enemiesCount; i++) {
            TextView indicator = mEnemyAnswerIndicators.get(mEnemiesPositions[i]);
            indicator.setBackgroundResource(R.drawable.status_new);
            indicator.setText("");
            indicator.setVisibility(View.GONE);
        }
    }

    private void saveQuestionInfo() {
        if (mCurrentQuestion != null) {
            mCurrentQuestion.setIsPlayed(1);
            mCurrentQuestion.setIsRightAnswered(!hasUserAnswered() || !isUserAnswerCorrect() ? 0 : 1);
            double answerTime = mUser.getAnswerTime();
            mCurrentQuestion.setAnswerTime(answerTime / 1000);
            mCurrentQuestion.update();
            mCurrentQuestion = null;
        }
    }

    private void resetRound() {

        mCurrentQuestionNumber = 1;
        mCurrentTour++;

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

    private void setUpSupportingLabels() {
        mTourNumberLabel.setText(String.format(Locale.getDefault(), "%s %d", mDefaultTourText, mCurrentTour));
        mQuestionNumberLabel.setText(String.format(Locale.getDefault(), "%s %d", mDefaultQuestionText, mCurrentQuestionNumber));
    }

    private void onTick(long lap) {
        int interval = hasUserAnswered()
                ? ConstantsManager.COUNTDOWN_INTERVAL_BOOST * ConstantsManager.SPEED_BOOST
                : COUNTDOWN_INTERVAL_NORMAL;
        if (mCurrentMillisecond.addAndGet(interval)/1000 - (mCurrentMillisecond.get() - interval)/1000 > 0) {
            mTimerLabel.setText(String.valueOf((ConstantsManager.ROUND_LENGTH - mCurrentMillisecond.get()) / 1000));
        }
        mTimerView.setProgress(mCurrentMillisecond.get());
        Timber.d(String.valueOf(mCurrentMillisecond.get()));
        if (!hasEnemiesAnswered()) checkIfEnemiesAnswered(interval);
    }

    /*
        private Observable<Boolean> onTick(long lap) {
        mTimerView.setProgress(mCurrentMillisecond.addAndGet(ConstantsManager.COUNTDOWN_INTERVAL_NORMAL));
        if (mCurrentMillisecond.get() % 1000 == 0) {
            mTimerLabel.setText(String.valueOf((ConstantsManager.ROUND_LENGTH - mCurrentMillisecond.get())/1000));
        }
        if (!hasEnemiesAnswered()) checkIfEnemiesAnswered(ConstantsManager.COUNTDOWN_INTERVAL_NORMAL);
        return Observable.just(hasUserAnswered());
    }

    private Observable<Boolean> onBoost(long lap) {
        mTimerView.setProgress(mCurrentMillisecond.addAndGet(ConstantsManager.COUNTDOWN_INTERVAL_BOOST * ConstantsManager.SPEED_BOOST));
        mTimerLabel.setText(String.valueOf((ConstantsManager.ROUND_LENGTH - (mCurrentMillisecond.get()))/1000));
        if (!hasEnemiesAnswered()) checkIfEnemiesAnswered(ConstantsManager.COUNTDOWN_INTERVAL_BOOST * ConstantsManager.SPEED_BOOST);
        return Observable.just(isTimeLeft());
    }
     */

    private boolean isTimeLeft() {
        return mCurrentMillisecond.get() < ConstantsManager.ROUND_LENGTH;
    }


    private void checkIfEnemiesAnswered(int interval) {
        for (int i = 0; i < enemiesCount; i++) {
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
        setUpDefaultAnswersRatio();
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

    private boolean hasEnemiesAnswered() {
        return mPlayersAnsweredCount == enemiesCount && !hasUserAnswered();
    }

    private boolean hasUserAnswered() {
        return mUserOptionLabel != null;
    }

    private void unBlockOptions() {
        for (TextView option : mOptionLabels) {
            option.setClickable(true);
        }
    }

    private void clearOptions() {
        for (TextView option : mOptionLabels) {
            option.setBackgroundResource(R.drawable.round_option);
        }
    }

    private void unBlockImage() {
        mQuestionImage.setClickable(true);
    }

    private void showRightAnswerHint() {
        ImageView hint = mHints.get(0);
        hint.setClickable(false);
        mUsedHints.append(0, true);
        hint.setImageResource(R.drawable.right2);
        Animation slideOutLeft = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);
        Animation slideOutRight = AnimationUtils.loadAnimation(this, R.anim.slide_out_right);
        List<TextView> options = new ArrayList<>();
        options.addAll(mOptionLabels);
        options.remove(mRightAnswerPosition);
        List<Integer> visibleOptions = getVisibleOptions(options);
        for (int i = 0; i < visibleOptions.size(); i++) {
            TextView option = options.get(visibleOptions.get(i));
            option.startAnimation(i == 0 ? slideOutLeft : slideOutRight);
            option.setVisibility(View.INVISIBLE);
        }
    }

    private void showAudienceHelpHint() {
        if (getVisibleOptions(mOptionLabels).size() > 2) {
            ImageView hint = mHints.get(1);
            hint.setClickable(false);
            mUsedHints.append(1, true);
            hint.setImageResource(R.drawable.zal2);
            showPercentage();
        }
    }

    private void showPercentage() {
        Animation slideInRight = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
        for (TextView percent : mOptionPercents) {
            percent.setVisibility(View.VISIBLE);
            percent.startAnimation(slideInRight);
        }
    }

    private void hidePercentage() {
        if (mOptionPercents.get(0).getVisibility() == View.VISIBLE) {
            Animation slideOutLeft = AnimationUtils.loadAnimation(this, R.anim.slide_out_right);
            for (TextView percent : mOptionPercents) {
                percent.startAnimation(slideOutLeft);
                percent.setVisibility(View.GONE);
            }
        }
    }

    private void showFiftyFiftyHint() {
        if (getVisibleOptions(mOptionLabels).size() > 1) {
            ImageView hint = mHints.get(2);
            hint.setClickable(false);
            mUsedHints.append(2, true);
            hint.setImageResource(R.drawable.fifty_fifty_second);
            Animation slideOutLeft = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);
            Animation slideOutRight = AnimationUtils.loadAnimation(this, R.anim.slide_out_right);
            List<TextView> options = new ArrayList<>();
            options.addAll(mOptionLabels);
            options.remove(mRightAnswerPosition);
            options.remove(new Random().nextInt(3));
            for (int i = 0; i < options.size(); i++) {
                TextView option = options.get(i);
                option.startAnimation(i == 0 ? slideOutLeft : slideOutRight);
                option.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void startTimer(int interval) {
        mSubscription = Observable.interval(interval, TimeUnit.MILLISECONDS)
                .takeWhile(lap -> isTimeLeft())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onTick,throwable -> Timber.d(throwable.getClass().getName()),this::onPlayersAnswered);
    }

    private void setQuestion() {
        mCurrentQuestion = mQuestions.get((mCurrentTour - 1)
                * ConstantsManager.ROUND_QUESTIONS_COUNT
                + mCurrentQuestionNumber - 1);
    }

    private List<Integer> getVisibleOptions(List<TextView> options) {
        List<Integer> visibleOptions = new ArrayList<>();
        for (int i = 0; i < options.size(); i++) {
            TextView option = options.get(i);
            if (option.getVisibility() == View.VISIBLE) visibleOptions.add(i);
        }
        return visibleOptions;
    }

    @Override
    protected void boostTimer() {
        mSubscription.unsubscribe();
        startTimer(ConstantsManager.COUNTDOWN_INTERVAL_BOOST);
    }

    @Override
    protected List<Player> getEnemies() {
        return Player.get(enemiesCount, bet);
    }
}
