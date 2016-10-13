package com.balinasoft.clever.ui.activities;

import android.content.res.Resources;
import android.os.Parcelable;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.balinasoft.clever.R;
import com.balinasoft.clever.model.IQuestion;
import com.balinasoft.clever.model.Player;
import com.balinasoft.clever.util.AnimationHelper;
import com.balinasoft.clever.util.ConstantsManager;
import com.bumptech.glide.Glide;

import org.androidannotations.annotations.AfterExtras;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.ViewsById;
import org.androidannotations.annotations.res.DimensionRes;
import org.androidannotations.annotations.res.StringRes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

import static com.balinasoft.clever.GameApplication.getUserId;
import static com.balinasoft.clever.util.ConstantsManager.COUNTDOWN_INTERVAL_NORMAL;

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

    @DimensionRes(R.dimen.default_corner_radius)
    float mCornerRadius;

    @Extra
    int bet;//////////////////

    @Extra
    Parcelable[] parcelableQuestions;/////////////

    @Extra
    Parcelable[] parcelablePlayers;//////////////////

    @Bean
    AnimationHelper mAnimationHelper;/////////////////

    int[] mEnemiesPositions;////////////////////

    List<Player> mEnemies; ////////////

    int mEnemiesCount;////////

    Player mUser; //////////////////

    List<IQuestion> mQuestions;///////////

    IQuestion mCurrentQuestion;///////////

    int mPlayersAnsweredCount = 0;////////

    int mRightAnswerPosition;///////////

    TextView mUserOptionLabel;//////////

    AtomicInteger mCurrentMillisecond = new AtomicInteger(0);//////////////

    SparseBooleanArray mUsedHints = new SparseBooleanArray();//////////////

    int mCurrentQuestionNumber = 1;////////////

    int mCurrentTour = 1;///////////////

    Subscription mSubscription;//////////////

    @Override
    protected void onDestroy() {
        if (mSubscription != null && !mSubscription.isUnsubscribed()) mSubscription.unsubscribe();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {}

    @AfterExtras
    void setPlayers() {
        mUser = createUser();///////////////////
        mEnemies = createEnemies();/////////////
        mEnemiesPositions = getEnemiesPositions();////////////////
        mQuestions = createQuestions();///////////////
        takeCoins();
    }

    @AfterViews
    void setUpViews() {
        setUpEnemiesBar();////////
        showEnemiesBar();////////
        setUpOptions();/////////
    }

    @Click(R.id.question_image)
    void onImageClicked() {
        if (mQuestionImage.getVisibility() == View.VISIBLE) showScaledImage();
    }

    @Click(R.id.scaled_image_area)
    void onScaledAreaClicked() {
        hideScaledImage();
    }

    void acceptAnswer(TextView option) {////////////////
        setUserAnswerTime();/////////////
        setUserOptionLabel(option);///////
        blockOptions();//////////////
        disableHints();//////////////
        blockImage();////////////////
        showRightAnswer();///////////
        addAnsweredPlayer();////////


        if (!isUserAnswerCorrect()) {////////////
            showWrongAnswer();/////////////////
        } else {
            addRightAnswerToUser();///////////////
            addUserPoints();////////////////////
            updateScoreLabel();/////////////////
        }
    }

    void blockOptions() {//////////////
        for (TextView option : mOptionLabels) {
            option.setClickable(false);
        }
    }

    void disableHints() {/////////////
        for (ImageView hint : mHints) {
            hint.setOnClickListener(null);
        }
    }

    boolean hasEnemiesAnswered() {
        return mPlayersAnsweredCount == mEnemies.size() && !hasUserAnswered();
    }

    boolean hasUserAnswered() {
        return mUserOptionLabel != null;
    }

    boolean haveAllAnswered() {
        return (mPlayersAnsweredCount == mEnemies.size() + 1) || (mPlayersAnsweredCount == 1 && mEnemiesCount == 0);
    }

    void boostTimer() {
        mSubscription.unsubscribe();
        startTimer();
    }

    void bindData() {
        setUpDefaultAnswersRatio();
        setUpSupportingLabels();
        setUpTimerViews();
        setQuestion();
        setQuestionCategory();
        setOptions();
        setQuestionContent();
        showQuestion();
    }

    void onTick(long lap) {
        int interval = isBoost()
                ? ConstantsManager.COUNTDOWN_INTERVAL_BOOST * ConstantsManager.SPEED_BOOST
                : COUNTDOWN_INTERVAL_NORMAL;
        if (mCurrentMillisecond.addAndGet(interval)/1000 - (mCurrentMillisecond.get() - interval)/1000 > 0) {
            mTimerLabel.setText(String.valueOf((ConstantsManager.ROUND_LENGTH - mCurrentMillisecond.get()) / 1000));
        }
        mTimerView.setProgress(mCurrentMillisecond.get());
    }

    void hideScaledImage() {
        mScaledImageArea.setVisibility(View.GONE);
    }

    void resetQuestion() {
        clearOptions();//
        dropEnemiesIndicators();//
        dropTime();//
        dropRightAnsweredCount();//
        clearUserSelection();//
        if (isLastQuestion()) {
            resetRound();
            return;
        }
        addQuestionCount();
        if (RESUMED_ACTIVITIES_COUNT > 0) bindData();
    }

    void startTimer() {
        mSubscription = Observable.interval(getInterval(), TimeUnit.MILLISECONDS)
                .takeWhile(lap -> isTimeLeft())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onTick, throwable -> Timber.d(throwable.getClass().getName()), this::onPlayersAnswered);
    }

    void onPlayersAnswered() {
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

    private void resetRound() {
        mCurrentQuestionNumber = 1;
        mCurrentTour++;
        onRoundReset();
    }

    private void clearOptions() {
        for (TextView option : mOptionLabels) {
            option.setBackgroundResource(R.drawable.round_option);
        }
    }

    private void showScaledImage() {
        mScaledImageArea.setVisibility(View.VISIBLE);
    }

    private void dropEnemiesIndicators() {
        for (int i = 0; i < mEnemies.size(); i++) {
            TextView indicator = mEnemyAnswerIndicators.get(mEnemiesPositions[i]);
            indicator.setBackgroundResource(R.drawable.status_new);
            indicator.setText("");
            indicator.setVisibility(View.GONE);
        }
    }

    private void dropRightAnsweredCount() {
        mPlayersAnsweredCount = 0;
    }

    private void clearUserSelection() {
        mUserOptionLabel = null;
    }

    private void dropTime() {
        mCurrentMillisecond.set(0);
    }

    private boolean isLastQuestion() {
        return mCurrentQuestionNumber == ConstantsManager.ROUND_QUESTIONS_COUNT;
    }

    private void addQuestionCount() {
        mCurrentQuestionNumber++;
    }

    private void setUpDefaultAnswersRatio() {
        for (TextView percent : mOptionPercents) {
            percent.setText("0%");
        }
    }

    private void setUpSupportingLabels() {
        mTourNumberLabel.setText(String.format(Locale.getDefault(), "%s %d", mDefaultTourText, mCurrentTour));
        mQuestionNumberLabel.setText(String.format(Locale.getDefault(), "%s %d", mDefaultQuestionText, mCurrentQuestionNumber));
    }

    private void setUpTimerViews() {
        mTimerLabel.setText(String.valueOf(ConstantsManager.ROUND_LENGTH / 1000));
        mTimerView.setProgress(0);
    }

    private void showQuestion() {
        Timber.d("SHOW_QUESTION");
        Animation slideDownAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        mAnimationHelper.setAnimationListener(slideDownAnimation, this::showOptions, null, null);
        toggleQuestion(slideDownAnimation, View.VISIBLE);
    }

    private void showOptions() {
        List<Animation> slideDowns = new ArrayList<>();
        for (int i = 0; i < mOptionLabels.size(); i++) {
            slideDowns.add(AnimationUtils.loadAnimation(this,R.anim.slide_down_fast));
        }
        setOptionSlideDownActions(slideDowns);
        toggleOption(0, slideDowns.get(0), View.VISIBLE);
    }

    private void setOptionSlideDownActions(List<Animation> slideDowns) {
        for (int i = 0; i < slideDowns.size(); i++) {
            Animation slideDown = slideDowns.get(i);
            int nextOptionPosition = i + 1;
            mAnimationHelper.setAnimationListener(slideDown, () -> {
                if (nextOptionPosition == slideDowns.size()) {
                    startTimer();
                    unBlockOptions();
                    unBlockImage();
                    setUpHints();
                } else {
                    toggleOption(nextOptionPosition, slideDowns.get(nextOptionPosition), View.VISIBLE);
                }
            }, null, null);
        }
    }

    private boolean isTimeLeft() {
        return mCurrentMillisecond.get() < ConstantsManager.ROUND_LENGTH;
    }

    private int getInterval() {
        return isBoost()
                ? ConstantsManager.COUNTDOWN_INTERVAL_BOOST
                : ConstantsManager.COUNTDOWN_INTERVAL_NORMAL;
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

    private void hideQuestion() {
        Animation slideUpQuestionAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_up_fast);
        slideUpQuestionAnimation.setStartOffset(2000);

        List<Integer> visibleOptions = getVisibleOptions(mOptionLabels);
        List<Animation> slideUps = new ArrayList<>(visibleOptions.size());
        for (int i = 0; i < visibleOptions.size(); i++) {
            slideUps.add(AnimationUtils.loadAnimation(this, R.anim.slide_up_fast));
        }

        setOptionSlideUpActions(slideUps, visibleOptions);
        setQuestionSlideUpAction(slideUpQuestionAnimation, visibleOptions.get(0), slideUps.get(0));
        toggleQuestion(slideUpQuestionAnimation, View.INVISIBLE);
    }

    private List<Integer> getVisibleOptions(List<TextView> options) {
        List<Integer> visibleOptions = new ArrayList<>();
        for (int i = 0; i < options.size(); i++) {
            TextView option = options.get(i);
            if (option.getVisibility() == View.VISIBLE) visibleOptions.add(i);
        }
        return visibleOptions;
    }

    private void setQuestionSlideUpAction(Animation slideUpQuestionAnimation, int firstOptionPosition, Animation optionAnimation) {
        mAnimationHelper.setAnimationListener(slideUpQuestionAnimation, () -> {
            mQuestionImage.setVisibility(View.GONE);
            mQuestionLabelBottom.setVisibility(View.GONE);
            mQuestionLabel.setVisibility(View.GONE);
            toggleOption(firstOptionPosition, optionAnimation, View.INVISIBLE);
        }, null, null);
    }

    private void setOptionSlideUpActions(List<Animation> slideUps, List<Integer> visibleOptions) {
        for (int i = 0; i < slideUps.size(); i++) {
            Animation slideUp = slideUps.get(i);
            final int nextVisibleOption = i + 1;
            mAnimationHelper.setAnimationListener(slideUp, () -> {
                if (nextVisibleOption == slideUps.size()) {
                    resetQuestion();
                } else toggleOption(visibleOptions.get(nextVisibleOption),
                        slideUps.get(nextVisibleOption),
                        View.INVISIBLE);
            }, null, null);
        }
    }

    private void showEnemiesResults() {
        for (int i = 0; i < mEnemies.size(); i++) {
            Player enemy = mEnemies.get(i);
            TextView indicator = mEnemyAnswerIndicators.get(mEnemiesPositions[i]);
            indicator.setText("");
            Timber.d("enemy answer %d rightanswer %d",enemy.getAnswerOption(),mRightAnswerPosition);
            indicator.setBackgroundResource(enemy.getAnswerOption() == mRightAnswerPosition
                    ? R.drawable.green
                    : R.drawable.red);
        }
    }

    private void unBlockOptions() {
        for (TextView option : mOptionLabels) {
            option.setClickable(true);
        }
    }

    private void setUpHints() {
        if (!mUsedHints.get(0)) mHints.get(0).setOnClickListener(view -> showRightAnswerHint());
        if (!mUsedHints.get(1)) mHints.get(1).setOnClickListener(view -> showAudienceHelpHint());
        if (!mUsedHints.get(2)) mHints.get(2).setOnClickListener(view -> showFiftyFiftyHint());
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

    private void toggleOption(int position, Animation animation, int visibility) {
        TextView option = mOptionLabels.get(position);
        option.setVisibility(visibility);
        option.startAnimation(animation);
    }

    private void toggleQuestion(Animation animation, int visibility) {
        if (visibility == View.INVISIBLE) {
            mQuestionArea.startAnimation(animation);
            mQuestionArea.setVisibility(visibility);
        } else {
            mQuestionArea.setVisibility(visibility);
            mQuestionArea.startAnimation(animation);
        }
    }

    boolean isUserAnswerCorrect() {/////////////////
        return mUserOptionLabel
                .getText()
                .toString()
                .equals(mCurrentQuestion.getRightAnswer());
    }

    private void setQuestionContent() {////////////////
        if (isMediaQuestion()) setUpMediaQuestion();
        else setUpTextQuestion();
    }

    private boolean isMediaQuestion() {////////////////
        String url = mCurrentQuestion.getMediaPath();
        Timber.d("Media url %s",url);
        return url != null && !url.isEmpty();
    }

    private void setUpMediaQuestion() {////////////////
        String mediaLocation = getMediaLocation();
        int cornerRadius = (int) (mCornerRadius / Resources.getSystem().getDisplayMetrics().density);
        Glide.with(this).load(mediaLocation).crossFade()
                .bitmapTransform(new RoundedCornersTransformation(this, cornerRadius, 0))
                .into(mQuestionImage);
        Glide.with(this).load(mediaLocation).crossFade().into(mScaledImage);
        mQuestionImage.setVisibility(View.VISIBLE);
        mQuestionLabelBottom.setText(mCurrentQuestion.getTextQuest());
        mQuestionLabelBottom.setVisibility(View.VISIBLE);
    }

    private void setUpTextQuestion() {/////////////////
        String questionText = mCurrentQuestion.getTextQuest();
        int questionLength = questionText.length();
        mQuestionLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, getQuestionTextSize(questionLength));
        mQuestionLabel.setText(questionText);
        mQuestionLabel.setVisibility(View.VISIBLE);
    }

    private float getQuestionTextSize(int length) {
        if (length < ConstantsManager.QUESTION_MAX_LENGTH)
            return getResources().getDimension(R.dimen.primary_text_size);
        else if (length < ConstantsManager.QUESTION_MAX_LENGTH_BIG)
            return getResources().getDimension(R.dimen.question_text_size_small);
        else if (length < ConstantsManager.QUESTION_MAX_LENGTH_LARGE)
            return getResources().getDimension(R.dimen.question_text_size_x_small);
        else return getResources().getDimension(R.dimen.question_text_size_x_x_small);
    }

    private void setQuestionCategory() {//////////////////
        mCategoryLabel.setText(mCurrentQuestion.getThemeQuest());
    }

    private void setOptions() {///////////////////
        List<Integer> positions = new ArrayList<>(Arrays.asList(0, 1, 2, 3));
        Collections.shuffle(positions);
        String[] answers = mCurrentQuestion.getOptions();
        for (int i = 0; i < mOptionLabels.size() - 1; i++) {
            String answer = answers[i];
            TextView option = mOptionLabels.get(positions.remove(0));
            Timber.d("textSize before %f",option.getTextSize());
            option.setTextSize(TypedValue.COMPLEX_UNIT_PX,getOptionTextSize(answer.length()));
            Timber.d("textSize after %f",option.getTextSize());
            option.setText(answer);
        }

        mRightAnswerPosition = positions.remove(0);
        String rightAnswer = mCurrentQuestion.getRightAnswer();
        TextView rightOption = mOptionLabels.get(mRightAnswerPosition);
        rightOption.setTextSize(TypedValue.COMPLEX_UNIT_PX,getOptionTextSize(rightAnswer.length()));
        rightOption.setText(rightAnswer);
    }

    private float getOptionTextSize(int length) {
        if(length < ConstantsManager.OPTION_MAX_LENGTH)
            return getResources().getDimension(R.dimen.secondary_text_size);
        else if(length < ConstantsManager.OPTION_MAX_LENGTH_BIG)
            return getResources().getDimension(R.dimen.option_text_size_small);
        else if(length < ConstantsManager.OPTION_MAX_LENGTH_LARGE)
            return getResources().getDimension(R.dimen.option_text_size_x_small);
        else return getResources().getDimension(R.dimen.option_text_size_x_x_small);
    }

    private void setUpOptions() {//////////////////////////
        for (TextView option : mOptionLabels) {
            option.setOnClickListener(view -> acceptAnswer(option));
            option.setClickable(false);
        }
    }

    private void setQuestion() { ////////////////////////////////////
        mCurrentQuestion = mQuestions.get((mCurrentTour - 1)
                * ConstantsManager.ROUND_QUESTIONS_COUNT
                + mCurrentQuestionNumber - 1);
    }

    private void addAnsweredPlayer() {/////////
        mPlayersAnsweredCount++;
    }

    private void blockImage() {
        mQuestionImage.setClickable(false);
    }//////////////////

    private void showRightAnswer() {/////////////////////////
        TextView rightOption = mOptionLabels.get(mRightAnswerPosition);
        rightOption.setBackgroundResource(R.drawable.right_answer);
    }

    private void showWrongAnswer() {///////////////////////
        mUserOptionLabel.setBackgroundResource(R.drawable.wrong_answer);
    }

    private void setUserAnswerTime() {///////////////////////////
        mUser.setAnswerTime(mCurrentMillisecond.get());
    }

    private void setUserOptionLabel(TextView option) {//////////////////////////
        mUserOptionLabel = option;
    }

    private void addRightAnswerToUser() {/////////////////
        mUser.addRightAnswer();
    }

    private void addUserPoints() {//////////////////////
        mUser.addPoints((ConstantsManager.ROUND_LENGTH - mUser.getAnswerTime())/100);
    }

    private void updateScoreLabel() {///////////////////
        mScoreLabel.setText(String.valueOf(mUser.getTotalScore()));
    }

    private void setUpEnemiesBar() {/////////////////////////////////
        for (int i = 0; i < mEnemies.size(); i++) {
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

    private void showEnemiesBar() {//////////////////////////
        Animation slideDownAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        mEnemiesBar.setVisibility(View.VISIBLE);
        mEnemiesBar.startAnimation(slideDownAnimation);
    }

    private List<Player> createEnemies() {//////////////
        List<Player> players = new ArrayList<>();
        for (Parcelable parcelablePlayer : parcelablePlayers) {
            Player player = (Player) parcelablePlayer;
            if (!player.getId().equals(getUserId()))
                players.add(player);
        }
        mEnemiesCount = players.size();
        return players;
    }



    private int[] getEnemiesPositions() {///////////////
        switch (mEnemies.size()) {
            case 1:
                return ConstantsManager.ENEMY_ONE;
            case 2:
                return ConstantsManager.ENEMY_TWO;
            case 3:
                return ConstantsManager.ENEMY_THREE;
            case 4:
                return ConstantsManager.ENEMY_FOUR;
            case 5:
                return ConstantsManager.ENEMY_FIVE;
            default:
                throw new RuntimeException("Wrong enemies count!" + String.valueOf(mEnemies.size()));
        }
    }

    private List<IQuestion> createQuestions() {///////////////////
        List<IQuestion> questions = new ArrayList<>();
        for (Parcelable parcelableQuestion : parcelableQuestions) {
            questions.add((IQuestion) parcelableQuestion);
        }
        return questions;
    }

    abstract Player createUser();//////////////////////

    abstract String getMediaLocation();

    abstract boolean isBoost();

    abstract void onRoundReset();

    abstract void takeCoins();
}
