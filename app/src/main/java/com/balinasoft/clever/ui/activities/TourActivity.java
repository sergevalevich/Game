package com.balinasoft.clever.ui.activities;

import android.content.res.Resources;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.balinasoft.clever.R;
import com.balinasoft.clever.model.Player;
import com.balinasoft.clever.storage.model.Question;
import com.balinasoft.clever.util.ConstantsManager;

import org.androidannotations.annotations.AfterExtras;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.ViewsById;
import org.androidannotations.annotations.res.StringRes;

import java.io.File;
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

    private List<Question> mQuestions = new ArrayList<>();

    private int[] mNeededEnemiesPositions;

    private SparseBooleanArray mUsedHints = new SparseBooleanArray();

    List<List<Integer>> mAnswersRatios = new ArrayList<>();

    private AtomicInteger mCurrentMillisecond = new AtomicInteger(0);

    private int mCurrentQuestionNumber = 1;

    private int mCurrentTour = 1;

    private int mPlayersAnsweredCount = 0;

    private TextView mUserOptionLabel;

    private int mRightAnswerPosition;

    private Question mCurrentQuestion;

    private List<Player> mEnemies;

    private Player mUser;

    private Subscription mSubscription;

    @Override
    protected void onResume() {
        super.onResume();
        if (mCurrentQuestion == null) startQuestion();
        else continueQuestion();
    }

    @Override
    protected void onStop() {
        if (mSubscription != null && !mSubscription.isUnsubscribed()) mSubscription.unsubscribe();
        super.onStop();
    }

    @AfterExtras
    void processExtras() {
        mEnemies = Player.get(enemiesCount,bet);
        mUser = Player.getUser(bet);
        for (Parcelable parcelableQuestion : parcelableQuestions) {
            mQuestions.add((Question) parcelableQuestion);
        }
    }

    @AfterViews
    void setUpViews() {
        setUpEnemiesBar();
        setUpOptions();
    }

    @Click(R.id.question_image)
    void onImageClicked() {
        if(mQuestionImage.getVisibility() == View.VISIBLE) {
            mScaledImageArea.setVisibility(View.VISIBLE);
        }
    }

    @Click(R.id.scaled_image_area)
    void onScaledAreaClicked() {
        mScaledImageArea.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {}

    private void setUpDefaultRatio() {
        for (TextView percent : mOptionPercents) {
            percent.setText("0%");
        }
    }

    private void bindData(Question question) {
        if (question == null) throw new RuntimeException("No more questions");
        Timber.d("bindData");
        setUpSupportingLabels();
        setUpTimer();

        mCurrentQuestion = question;
        List<Integer> positions = new ArrayList<>(Arrays.asList(0, 1, 2, 3));
        Collections.shuffle(positions);
        String[] answers = mCurrentQuestion.getFormattedAnswers();
        for (int i = 0; i < mOptionLabels.size() - 1; i++) {
            TextView option = mOptionLabels.get(positions.remove(0));
            option.setText(answers[i]);
        }

        mRightAnswerPosition = positions.remove(0);

        for(Player enemy : mEnemies) {
            enemy.setAnswerBy(mRightAnswerPosition);
        }

        setUpDefaultRatio();
        setAnswersRatioCombinations();

        TextView rightOption = mOptionLabels.get(mRightAnswerPosition);
        rightOption.setText(mCurrentQuestion.getRightAnswer());

        mCategoryLabel.setText(mCurrentQuestion.getThemeQuest());

        String mediaPath = mCurrentQuestion.getMediaPath();
        if (mediaPath != null) {
            int cornerRadius = (int) (12 / Resources.getSystem().getDisplayMetrics().density);
            String path = getFilesDir() + File.separator + mediaPath;
            Glide.with(this).load(path).crossFade()
                    .bitmapTransform(new RoundedCornersTransformation(this, cornerRadius, 0))
                    .into(mQuestionImage);
            Glide.with(this).load(path).crossFade().into(mScaledImage);
            mQuestionImage.setVisibility(View.VISIBLE);
            mQuestionLabelBottom.setText(mCurrentQuestion.getTextQuest());
            mQuestionLabelBottom.setVisibility(View.VISIBLE);
        } else {
            mQuestionLabel.setText(mCurrentQuestion.getTextQuest());
            mQuestionLabel.setVisibility(View.VISIBLE);
        }
        showQuestion();
    }

    private void setUpHints() {
        if (!mUsedHints.get(0)) mHints.get(0).setOnClickListener(view -> showRightAnswerHint());
        if (!mUsedHints.get(1)) mHints.get(1).setOnClickListener(view -> showAudienceHelpHint());
        if (!mUsedHints.get(2)) mHints.get(2).setOnClickListener(view -> showFiftyFiftyHint());
    }

    private void setUpTimer() {
        mTimerLabel.setText(String.valueOf(ConstantsManager.ROUND_LENGTH/1000));
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

    private void disableHints() {
        for (ImageView hint : mHints) {
            hint.setOnClickListener(null);
        }
    }

    private void hideQuestion() {

        Animation slideUpQuestionAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_up_fast);

        List<Integer> visibleOptions = getVisibleOptions(mOptionLabels);
        List<Animation> slideUps = new ArrayList<>(visibleOptions.size());
        for (int i = 0; i < visibleOptions.size(); i++) {
            slideUps.add(AnimationUtils.loadAnimation(this, R.anim.slide_up_fast));
        }

        for (int i = 0; i < slideUps.size() - 1; i++) {
            Animation slideUp = slideUps.get(i);
            final int visibleOption = i;
            slideUp.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    toggleOption(visibleOptions.get(visibleOption + 1), slideUps.get(visibleOption + 1), View.INVISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }


        slideUpQuestionAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                toggleOption(visibleOptions.get(0), slideUps.get(0), View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        toggleQuestion(slideUpQuestionAnimation, View.INVISIBLE);
    }

    private void setUpOptions() {
        for (TextView option : mOptionLabels) {
            option.setOnClickListener(view -> acceptAnswer(option));
        }
    }

    private void showOptions() {
        Animation slideDownAnimationOne = AnimationUtils.loadAnimation(this, R.anim.slide_down_fast);
        Animation slideDownAnimationTwo = AnimationUtils.loadAnimation(this, R.anim.slide_down_fast);
        Animation slideDownAnimationThree = AnimationUtils.loadAnimation(this, R.anim.slide_down_fast);
        Animation slideDownAnimationFour = AnimationUtils.loadAnimation(this, R.anim.slide_down_fast);
        slideDownAnimationOne.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                toggleOption(1, slideDownAnimationTwo, View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        slideDownAnimationTwo.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                toggleOption(2, slideDownAnimationThree, View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        slideDownAnimationThree.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                toggleOption(3, slideDownAnimationFour, View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        slideDownAnimationFour.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                unBlockOptions();
                unBlockImage();
                setUpHints();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        toggleOption(0, slideDownAnimationOne, View.VISIBLE);
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

    private void toggleOption(int position, Animation animation, int visibility) {
        TextView option = mOptionLabels.get(position);
        option.setVisibility(visibility);
        option.startAnimation(animation);
    }

    private void acceptAnswer(TextView option) {

        mUser.setAnswerTime(mCurrentMillisecond.get());

        mUserOptionLabel = option;
        mPlayersAnsweredCount++;

        blockOptions();
        disableHints();
        blockImage();

        showRightAnswer();
        if (!isUserAnswerCorrect()) {
            showWrongAnswer();
            return;
        }
        mUser.addRightAnswer();
        mUser.addPoints((ConstantsManager.ROUND_LENGTH - mUser.getAnswerTime())/100);
        mScoreLabel.setText(String.valueOf(mUser.getTotalScore()));
    }

    private Observable<Boolean> onPlayersAnswered(boolean b) {

        mScaledImageArea.setVisibility(View.GONE);

        mTimerView.setProgress(ConstantsManager.ROUND_LENGTH);
        mTimerLabel.setText("0");

        if (!hasUserAnswered()) {
            disableHints();
            blockOptions();
            blockImage();
            showRightAnswer();
        }

        for (int i = 0; i < enemiesCount; i++) {
            Player enemy = mEnemies.get(i);
            TextView indicator = mEnemyAnswerIndicators.get(mNeededEnemiesPositions[i]);
            indicator.setText("");
            indicator.setBackgroundResource(enemy.getAnswerOption() == mRightAnswerPosition
                    ? R.drawable.green
                    : R.drawable.red);
        }

        return Observable.just(b);
    }

    private Observable<Boolean> resetQuestion(boolean isLast) {

        hidePercentage();
        hideQuestion();
        clearOptions();

        if(mCurrentQuestion != null) {
            mCurrentQuestion.setIsPlayed(1);
            mCurrentQuestion.setIsRightAnswered(!hasUserAnswered() || !isUserAnswerCorrect() ? 0 : 1);
            double answerTime = mUser.getAnswerTime();
            mCurrentQuestion.setAnswerTime(answerTime/1000);
            mCurrentQuestion.update();
            mCurrentQuestion = null;
        }

        mAnswersRatios.clear();
        mQuestionLabel.setText("");
        mCategoryLabel.setText("");

        for (int i = 0; i < enemiesCount; i++) {
            TextView indicator = mEnemyAnswerIndicators.get(mNeededEnemiesPositions[i]);
            indicator.setBackgroundResource(R.drawable.status_new);
            indicator.setText("");
            indicator.setVisibility(View.GONE);
        }

        mQuestionImage.setVisibility(View.GONE);
        mQuestionLabelBottom.setVisibility(View.GONE);
        mQuestionLabel.setVisibility(View.GONE);

        mCurrentMillisecond.set(0);
        mUser.setAnswerTime(0);

        mPlayersAnsweredCount = 0;
        mUserOptionLabel = null;

        return Observable.just(isLast);
    }

    private void resetRound() {

        mCurrentQuestionNumber = 1;
        mCurrentTour++;

        List<Player> players = new ArrayList<>();
        players.add(new Player(mUser));
        for(Player enemy:mEnemies) {
            Player player = new Player(enemy);
            players.add(player);
        }

        Collections.sort(players, (player1, player2) -> player2.getTotalScore() - player1.getTotalScore());

        if (mCurrentTour == 4) {
            Player.countCoins(players);
        }

        Player[] pls = new Player[players.size()];
        pls = players.toArray(pls);

        ResultsActivity_.intent(this).parcelablePlayers(pls).tourNumber(mCurrentTour-1).start();
    }

    private void showRightAnswer() {
        TextView rightOption = mOptionLabels.get(mRightAnswerPosition);
        rightOption.setBackgroundResource(R.drawable.right_answer);
    }

    private void showWrongAnswer() {
        mUserOptionLabel.setBackgroundResource(R.drawable.wrong_answer);
    }

    private boolean isUserAnswerCorrect() {
        return mUserOptionLabel
                .getText()
                .toString()
                .equals(mCurrentQuestion.getRightAnswer());
    }

    private void setUpSupportingLabels() {
        mTourNumberLabel.setText(String.format(Locale.getDefault(), "%s %d", mDefaultTourText, mCurrentTour));
        mQuestionNumberLabel.setText(String.format(Locale.getDefault(), "%s %d", mDefaultQuestionText, mCurrentQuestionNumber));
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

    private boolean isTimeLeft() {
        return mCurrentMillisecond.get() < ConstantsManager.ROUND_LENGTH;
    }


    private void checkIfEnemiesAnswered(int interval) {
        for (int i = 0; i < enemiesCount; i++) {
            Player enemy = mEnemies.get(i);
            if (enemy.getAnswerTime()/interval == mCurrentMillisecond.get()/interval){
                TextView indicator = mEnemyAnswerIndicators.get(mNeededEnemiesPositions[i]);
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
                outRatio.add((startRatio.get(i)  * 100 / (k + 1)));
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

    private void blockOptions() {
        for (TextView option : mOptionLabels) {
            option.setClickable(false);
        }
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

    private void blockImage() {
        mQuestionImage.setClickable(false);
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
        if(mOptionPercents.get(0).getVisibility() == View.VISIBLE) {
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

    private void startQuestion() {
        mSubscription = getQuestionCycle()
                .subscribe(isLast -> {},
                        throwable -> Timber.d(throwable.getLocalizedMessage()),
                        () -> {
                            if (mCurrentQuestionNumber == ConstantsManager.ROUND_QUESTIONS_COUNT) {
                                resetRound();
                            } else {
                                mCurrentQuestionNumber++;
                                startQuestion();
                            }
                        });
    }

    private void continueQuestion() {
        mSubscription = startTimer(mCurrentQuestion)
                .subscribe(isLast -> Timber.d("isLast"),
                        throwable -> Timber.d(throwable.getLocalizedMessage()),
                        () -> {
                            if (mCurrentQuestionNumber == ConstantsManager.ROUND_QUESTIONS_COUNT) {
                                resetRound();
                            } else {
                                mCurrentQuestionNumber++;
                                startQuestion();
                            }
                        });
    }

    private Observable<Boolean> getQuestionCycle() {
        return getQuestion()
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .doOnNext(this::bindData)
                .delay(2, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
                .flatMap(this::startTimer);
    }

    private Observable<Boolean> startTimer(Question question) {
        return Observable
                .interval(ConstantsManager.COUNTDOWN_INTERVAL_NORMAL, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(this::onTick)
                .takeWhile(userAnswered -> !userAnswered && isTimeLeft())
                .concatWith(Observable
                        .interval(ConstantsManager.COUNTDOWN_INTERVAL_BOOST, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .flatMap(this::onBoost))
                .takeWhile(timeLeft -> isTimeLeft())
                .concatWith(Observable
                        .just(true)
                        .flatMap(this::onPlayersAnswered)
                        .delay(2, TimeUnit.SECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .flatMap(this::resetQuestion)
                        .delay(1, TimeUnit.SECONDS)
                        .observeOn(AndroidSchedulers.mainThread()));
    }

    private Observable<Question> getQuestion() {
        Question question = mQuestions.get((mCurrentTour - 1)
                *ConstantsManager.ROUND_QUESTIONS_COUNT
                + mCurrentQuestionNumber - 1);
        return question == null
                ? Observable.error(new RuntimeException("No question"))
                : Observable.just(question);
    }

    private List<Integer> getVisibleOptions(List<TextView> options) {
        List<Integer> visibleOptions = new ArrayList<>();
        for (int i = 0; i < options.size(); i++) {
            TextView option = options.get(i);
            if (option.getVisibility() == View.VISIBLE) visibleOptions.add(i);
        }
        return visibleOptions;
    }

}
