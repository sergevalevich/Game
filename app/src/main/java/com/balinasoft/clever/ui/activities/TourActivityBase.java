package com.balinasoft.clever.ui.activities;

import android.os.Parcelable;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.balinasoft.clever.R;
import com.balinasoft.clever.model.Player;
import com.balinasoft.clever.storage.model.Question;
import com.balinasoft.clever.util.ConstantsManager;

import org.androidannotations.annotations.AfterExtras;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.ViewsById;
import org.androidannotations.annotations.res.StringRes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import rx.Subscription;

@EActivity(R.layout.activity_tour)
public abstract class TourActivityBase extends BaseActivity {

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

    @ViewsById({R.id.option_one_percent,
            R.id.option_two_percent,
            R.id.option_three_percent,
            R.id.option_four_percent})
    List<TextView> mOptionPercents;

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

    @ViewById(R.id.question_area)
    FrameLayout mQuestionArea;

    @ViewById(R.id.question_label)
    TextView mQuestionLabel;

    @ViewById(R.id.question_label_bottom)
    TextView mQuestionLabelBottom;

    @ViewById(R.id.question_image)
    ImageView mQuestionImage;

    @ViewById(R.id.scaled_image_area)
    FrameLayout mScaledImageArea;

    @ViewById(R.id.scaled_image)
    ImageView mScaledImage;

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

    @Extra
    Parcelable[] parcelableQuestions;

    int[] mEnemiesPositions;

    List<Question> mQuestions = new ArrayList<>();

    Question mCurrentQuestion;

    List<Player> mEnemies;

    Player mUser;

    int mPlayersAnsweredCount = 0;

    int mRightAnswerPosition;

    TextView mUserOptionLabel;

    AtomicInteger mCurrentMillisecond = new AtomicInteger(0);

    SparseBooleanArray mUsedHints = new SparseBooleanArray();

    List<List<Integer>> mAnswersRatios = new ArrayList<>();

    int mCurrentQuestionNumber = 1;

    int mCurrentTour = 1;

    Subscription mSubscription;

    @AfterExtras
    void setPlayers() {
        setUser();
        setEnemies();
        setEnemiesPositions();
        setQuestions();
    }

    @AfterViews
    void setUpViews() {
        setUpEnemiesBar();
        showEnemiesBar();
        setUpOptions();
    }

    private void setEnemiesPositions() {
        switch (enemiesCount) {
            case 1:
                mEnemiesPositions = ConstantsManager.ENEMY_ONE;
                break;
            case 2:
                mEnemiesPositions = ConstantsManager.ENEMY_TWO;
                break;
            case 3:
                mEnemiesPositions = ConstantsManager.ENEMY_THREE;
                break;
            case 4:
                mEnemiesPositions = ConstantsManager.ENEMY_FOUR;
                break;
            case 5:
                mEnemiesPositions = ConstantsManager.ENEMY_FIVE;
                break;
            default:
                throw new RuntimeException("Wrong enemies count!");
        }
    }

    private void setUpOptions() {
        for (TextView option : mOptionLabels) {
            option.setOnClickListener(view -> acceptAnswer(option));
        }
    }

    private void acceptAnswer(TextView option) {

        setUserAnswerTime();
        setUserOptionLabel(option);
        addRightAnsweredUsersCount();
        blockOptions();
        disableHints();
        blockImage();
        showRightAnswer();
        if (!isUserAnswerCorrect()) {
            showWrongAnswer();
            return;
        }
        addRightAnswerToUser();
        addUserPoints();
        updateScoreLabel();
    }

    protected void blockOptions() {
        for (TextView option : mOptionLabels) {
            option.setClickable(false);
        }
    }

    protected void disableHints() {
        for (ImageView hint : mHints) {
            hint.setOnClickListener(null);
        }
    }

    protected void blockImage() {
        mQuestionImage.setClickable(false);
    }

    protected boolean isUserAnswerCorrect() {
        return mUserOptionLabel
                .getText()
                .toString()
                .equals(mCurrentQuestion.getRightAnswer());
    }

    protected void showRightAnswer() {
        TextView rightOption = mOptionLabels.get(mRightAnswerPosition);
        rightOption.setBackgroundResource(R.drawable.right_answer);
    }

    private void showWrongAnswer() {
        mUserOptionLabel.setBackgroundResource(R.drawable.wrong_answer);
    }

    private void setUserAnswerTime() {
        mUser.setAnswerTime(mCurrentMillisecond.get());
    }

    private void addRightAnsweredUsersCount() {
        mPlayersAnsweredCount++;
    }

    private void setUserOptionLabel(TextView option) {
        mUserOptionLabel = option;
    }

    private void addRightAnswerToUser() {
        mUser.addRightAnswer();
    }

    private void addUserPoints() {
        mUser.addPoints((ConstantsManager.ROUND_LENGTH - mUser.getAnswerTime())/100);
    }

    private void updateScoreLabel() {
        mScoreLabel.setText(String.valueOf(mUser.getTotalScore()));
    }

    private void setUpEnemiesBar() {
        for (int i = 0; i < enemiesCount; i++) {
            int position = mEnemiesPositions[i];
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

    private void setEnemies() {
        mEnemies = getEnemies();
    }

    private void setUser() {
        mUser = Player.getUser(bet);
    }

    private void setQuestions() {
        for (Parcelable parcelableQuestion : parcelableQuestions) {
            mQuestions.add((Question) parcelableQuestion);
        }
    }

    protected abstract List<Player> getEnemies();
}
