package com.valevich.game.ui.activities;

import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.valevich.game.R;
import com.valevich.game.model.Player;
import com.valevich.game.storage.model.Question;
import com.valevich.game.util.ConstantsManager;

import org.androidannotations.annotations.AfterExtras;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.ViewsById;
import org.androidannotations.annotations.res.StringRes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

@EActivity(R.layout.activity_tour)
public class TourActivity extends AppCompatActivity {

    @ViewsById({R.id.enemy_image_first,
            R.id.enemy_image_second,
            R.id.enemy_image_third,
            R.id.enemy_image_fourth,
            R.id.enemy_image_fifth})
    List<ImageView> mEnemyImages;

    @ViewsById({R.id.enemy_answer_indicator_first,
            R.id.enemy_answer_indicator_second,
            R.id.enemy_answer_indicator_third,
            R.id.enemy_answer_indicator_fourth,
            R.id.enemy_answer_indicator_fifth})
    List<TextView> mEnemyAnswerIndicators;

    @ViewsById({R.id.enemy_name_first,
            R.id.enemy_name_second,
            R.id.enemy_name_third,
            R.id.enemy_name_fourth,
            R.id.enemy_name_fifth})
    List<TextView> mEnemyNameLabels;

    @ViewsById({R.id.option_one,
            R.id.option_two,
            R.id.option_three,
            R.id.option_four})
    List<TextView> mOptionLabels;

    @ViewsById({R.id.area_option_one,
            R.id.area_option_two,
            R.id.area_option_three,
            R.id.area_option_four,
            R.id.area_option_five,})
    List<LinearLayout> mOptionAreas;

    @ViewsById({R.id.right_answer_hint,
            R.id.audience_help_hint,
            R.id.fifty_fifty_hint})
    List<ImageView> mHints;

    @ViewById(R.id.score_label)
    TextView mScoreLabel;

    @ViewById(R.id.tour_number_label)
    TextView mTourNumberLabel;

    @ViewById(R.id.question_number_label)
    TextView mQuestionNumberLabel;

    @ViewById(R.id.timer)
    ProgressBar mTimerView;

    @ViewById(R.id.timer_label)
    TextView mTimerLabel;

    @ViewById(R.id.category)
    TextView mCategoryLabel;

    @ViewById(R.id.question_label)
    TextView mQuestionLabel;

    @ViewById(R.id.question_label_bottom)
    TextView mQuestionLabelBottom;

    @ViewById(R.id.question_image)
    ImageView mQuestionImage;

    @ViewById(R.id.enemies_bar)
    LinearLayout mEnemiesBar;

    @StringRes(R.string.tour_label_text)
    String mDefaultTourText;

    @StringRes(R.string.question_label_text)
    String mDefaultQuestionText;

    @Extra
    int enemiesCount;

    @Extra
    int bet;

    private int[] mNeededEnemiesPositions;

    private int mTimeLeft = 25;

    private int mCurrentQuestion = 1;

    private int mCurrentTour = 1;

    private int mEnemiesAnsweredCount = 0;

    private boolean mIsBoostStarted;

    private TextView mUserOptionLabel;

    private int mRightAnswerPosition;// TODO: 31.08.2016 assign

    private static CountDownTimer mTimer;

    private List<Player> mEnemies;

    private List<Question> mQuestions;

    private Subscription mSubscription;

    @Override
    protected void onResume() {
        super.onResume();
        if (mQuestions == null) {
            mSubscription = Question
                    .getQuestions(ConstantsManager.MAX_QUESTIONS_COUNT)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(questions -> {
                        mQuestions = questions;
                        bindData();
                        startTimer();
                    });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mSubscription != null && !mSubscription.isUnsubscribed()) mSubscription.unsubscribe();
    }

    @AfterExtras
    void setEnemies() {
        mEnemies = Player.get(enemiesCount);
    }

    @AfterViews
    void setUpViews() {
        setUpEnemiesBar();
        setUpOptions();
        setUpSupportingLabels();
    }

    private void bindData() {
        Question question = mQuestions.remove(
                ConstantsManager.ROUND_QUESTIONS_COUNT * (mCurrentTour - 1)
                        + mCurrentQuestion - 1);

        List<Integer> positions = new ArrayList<>(Arrays.asList(0, 1, 2, 3));
        Collections.shuffle(positions);
        String[] answers = question.getFormattedAnswers();
        for (int i = 0; i < mOptionLabels.size() - 1; i++) {
            TextView option = mOptionLabels.get(positions.remove(0));
            option.setText(answers[i]);
        }

        mRightAnswerPosition = positions.remove(0);
        TextView rightOption = mOptionLabels.get(mRightAnswerPosition);
        rightOption.setText(question.getRightAnswer());

        mCategoryLabel.setText(question.getThemeQuest());

        String mediaPath = question.getMediaPath();
        if (mediaPath != null) {
            Glide.with(this).load(mediaPath).crossFade().into(mQuestionImage);
            mQuestionLabelBottom.setText(question.getTextQuest());
            mQuestionLabelBottom.setVisibility(View.VISIBLE);
        } else {
            mQuestionLabel.setText(question.getTextQuest());
            mQuestionLabel.setVisibility(View.VISIBLE);
        }
    }

    private void setUpOptions() {
        for (TextView option : mOptionLabels) {
            option.setOnClickListener(view -> acceptAnswer(option));
        }
    }

    private void acceptAnswer(TextView option) {
        mUserOptionLabel = option;
        mUserOptionLabel.setBackgroundResource(R.drawable.selected_answer);
        blockOptions();
    }

    private void onPlayersAnswered() {

        if (mUserOptionLabel != null && !isUserAnswerCorrect()) {
            mUserOptionLabel.setBackgroundResource(R.drawable.wrong_answer);
        }

        TextView rightOption = mOptionLabels.get(mRightAnswerPosition);
        rightOption.setBackgroundResource(R.drawable.right_answer);

        for (int i = 0; i < enemiesCount; i++) {
            Player enemy = mEnemies.get(i);
            TextView indicator = mEnemyAnswerIndicators.get(mNeededEnemiesPositions[i]);
            indicator.setText("");
            indicator.setBackgroundResource(enemy.getAnswerOption() == mRightAnswerPosition
                    ? R.drawable.green
                    : R.drawable.red);
        }
    }

    private boolean isUserAnswerCorrect() {
        return mUserOptionLabel
                .getText()
                .toString()
                .equals(mQuestions.get(mCurrentQuestion).getRightAnswer());
    }

    private void setUpSupportingLabels() {
        mTourNumberLabel.setText(String.format(Locale.getDefault(), "%s %d", mDefaultTourText, mCurrentTour));
        mQuestionNumberLabel.setText(String.format(Locale.getDefault(), "%s %d", mDefaultQuestionText, mCurrentQuestion));
    }

    private void setUpEnemiesBar() {
        switch (enemiesCount) {
            case 1:
                mNeededEnemiesPositions = ConstantsManager.ENEMY_ONE;
                break;
            case 2:
                mNeededEnemiesPositions = ConstantsManager.ENEMY_TWO;
                break;
            case 3:
                mNeededEnemiesPositions = ConstantsManager.ENEMY_THREE;
                break;
            case 4:
                mNeededEnemiesPositions = ConstantsManager.ENEMY_FOUR;
                break;
            case 5:
                mNeededEnemiesPositions = ConstantsManager.ENEMY_FIVE;

                break;
            default:
                throw new RuntimeException("Wrong enemies count!");
        }
        setUpEnemyViews();
        showEnemiesBar();
    }

    private void setUpEnemyViews() {
        for (int i = 0; i < enemiesCount; i++) {
            int position = mNeededEnemiesPositions[i];
            ImageView image = mEnemyImages.get(position);
            TextView name = mEnemyNameLabels.get(position);
            LinearLayout optionArea = mOptionAreas.get(position);
            Player enemy = mEnemies.get(i);
            image.setImageResource(enemy.getImageResId());
            name.setText(enemy.getName());
            optionArea.setVisibility(View.VISIBLE);
        }
    }

    private void showEnemiesBar() {
        Animation slideDownAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        mEnemiesBar.setVisibility(View.VISIBLE);
        mEnemiesBar.startAnimation(slideDownAnimation);
    }

    private void startTimer() {
        Timber.d("Timer started");
        mTimer = new CountDownTimer(ConstantsManager.ROUND_LENGTH, ConstantsManager.COUNTDOWN_INTERVAL_NORMAL) {
            @Override
            public void onTick(long leftTimeInMilliseconds) {
                if(!mIsBoostStarted) {
                    long leftInSeconds = leftTimeInMilliseconds / 1000;
                    Timber.d("%d", ConstantsManager.ROUND_LENGTH - (int) leftTimeInMilliseconds);
                    mTimerView.setProgress(ConstantsManager.ROUND_LENGTH - (int) leftTimeInMilliseconds);
                    if (mTimeLeft - leftInSeconds > 1) {
                        checkIfPlayersAnswered();
                        mTimerLabel.setText(String.valueOf(leftInSeconds + 1));
                        mTimeLeft--;
                    }
                }

            }

            @Override
            public void onFinish() {
                mTimerView.setProgress(ConstantsManager.ROUND_LENGTH);
                mTimerLabel.setText("0");
                checkIfPlayersAnswered();
            }
        }.start();

    }

    private void speedUpTimer() {
        Timber.d("Timer boosted");
        mTimer.cancel();
        int currentProgress = mTimerView.getProgress();
        int timeLeft = mTimeLeft * 1000 / ConstantsManager.SPEED_BOOST;
        mTimer = new CountDownTimer(
                timeLeft,
                ConstantsManager.COUNTDOWN_INTERVAL_BOOST) {
            @Override
            public void onTick(long leftTimeInMilliseconds) {
                long leftInSeconds = leftTimeInMilliseconds / 1000;
                int progress = (timeLeft - (int) leftTimeInMilliseconds) * ConstantsManager.SPEED_BOOST + currentProgress;
                Timber.d("%d",progress);
                mTimerView.setProgress(progress);
                if (mTimeLeft - leftInSeconds > 1) {
                    mTimerLabel.setText(String.valueOf(leftInSeconds + 1));
                    mTimeLeft--;
                }
            }

            @Override
            public void onFinish() {
                mTimerView.setProgress(ConstantsManager.ROUND_LENGTH);
                mTimerLabel.setText("0");
                onPlayersAnswered();
            }
        }.start();
    }

    private void checkIfPlayersAnswered() {
        if (!mIsBoostStarted) {
            if (!hasEnemiesAnswered()) {
                checkIfEnemiesAnswered();
            } else if (hasUserAnswered()) {
                mIsBoostStarted = true;
                speedUpTimer();
            } else if (!isTimeLeft()) {
                onPlayersAnswered();
            }
        }

    }

    private boolean isTimeLeft() {
        return mTimeLeft > 0;
    }

    private void checkIfEnemiesAnswered() {
        for (int i = 0; i < enemiesCount; i++) {
            Player enemy = mEnemies.get(i);
            if (enemy.getAnswerTime() == ConstantsManager.ROUND_LENGTH / 1000 - mTimeLeft) {
                TextView indicator = mEnemyAnswerIndicators.get(mNeededEnemiesPositions[i]);
                indicator.setText(String.valueOf(++mEnemiesAnsweredCount));
                indicator.setVisibility(View.VISIBLE);
            }
        }
    }

    private boolean hasEnemiesAnswered() {
        return mEnemiesAnsweredCount == enemiesCount;
    }

    private boolean hasUserAnswered() {
        return mUserOptionLabel != null;
    }

    private void blockOptions() {
        for (TextView option : mOptionLabels) {
            option.setClickable(false);
        }
    }

}
