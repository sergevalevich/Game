package com.valevich.game.ui.activities;

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
import com.valevich.game.R;
import com.valevich.game.model.Player;
import com.valevich.game.storage.model.Question;
import com.valevich.game.util.ConstantsManager;
import com.valevich.game.util.Preferences_;

import org.androidannotations.annotations.AfterExtras;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.ViewsById;
import org.androidannotations.annotations.res.StringRes;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.androidannotations.api.sharedpreferences.IntPrefField;

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

    @Pref
    Preferences_ mPreferences;

    private List<Question> mQuestions = new ArrayList<>();

    private int[] mNeededEnemiesPositions;

    private SparseBooleanArray mUsedHints = new SparseBooleanArray();

    List<List<Integer>> mAnswersRatios = new ArrayList<>();

    private int mCurrentMillisecond = 0;

    private int mPreBoostProgress;

    private int mCurrentQuestionNumber = 1;

    private int mCurrentTour = 1;

    private int mEnemiesAnsweredCount = 0;

    private TextView mUserOptionLabel;

    private int mRightAnswerPosition;

    private Question mCurrentQuestion;

    private List<Player> mEnemies;

    private Player mUser;

    private Subscription mSubscription;

    @Override
    protected void onResume() {
        super.onResume();
        startQuestion();
    }

    @Override
    protected void onStop() {
        resetQuestion(isLastQuestion());
        if (mSubscription != null && !mSubscription.isUnsubscribed())
            mSubscription.unsubscribe();
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

    @Override
    public void onBackPressed() {

    }

    private void setUpDefaultRatio() {
        for (TextView percent : mOptionPercents) {
            percent.setText("0%");
        }
    }

    private void bindData(Question question) {
        if (question == null) throw new RuntimeException("No more questions");
        Timber.d("bindData");
        setUpDefaultRatio();
        setAnswersRatioCombinations();
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
            enemy.setAnswer(mRightAnswerPosition);
        }

        TextView rightOption = mOptionLabels.get(mRightAnswerPosition);
        rightOption.setText(mCurrentQuestion.getRightAnswer());

        mCategoryLabel.setText(mCurrentQuestion.getThemeQuest());

        String mediaPath = mCurrentQuestion.getMediaPath();
        if (mediaPath != null) {
            int cornerRadius = (int) (12 / Resources.getSystem().getDisplayMetrics().density);
            Glide.with(this)
                    .load(getFilesDir() + File.separator + mediaPath).crossFade()
                    .bitmapTransform(new RoundedCornersTransformation(this, cornerRadius, 0))
                    .into(mQuestionImage);
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

        mUser.setAnswerTime(mCurrentMillisecond);

        mUserOptionLabel = option;

        blockOptions();
        disableHints();

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

        mTimerView.setProgress(0);
        mTimerLabel.setText("0");

        if (!hasUserAnswered()) {
            disableHints();
            blockOptions();
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

        if(mCurrentQuestion != null) {
            mCurrentQuestion.setIsPlayed(1);
            mCurrentQuestion.update();
        }

        mAnswersRatios.clear();

        for (TextView option : mOptionLabels) {
            option.setText("");
            option.setClickable(true);
            option.setBackgroundResource(R.drawable.round_option);
        }

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

        mCurrentMillisecond = 0;
        mUser.setAnswerTime(0);

        mEnemiesAnsweredCount = 0;
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

        Player[] pls = new Player[players.size()];
        pls = players.toArray(pls);

        if(mCurrentTour == 4) {
            IntPrefField userScore = mPreferences.userScore();
            IntPrefField userCoins = mPreferences.userCoins();
            userScore.put(userScore.get() + mUser.getTotalScore());
            userCoins.put(userCoins.get() + mUser.getCoinsPortion(pls));
        }
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

    private boolean isLastQuestion() {
        return mCurrentQuestionNumber > 7;
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
        Timber.d("onTick");
        int progress = (int) ((lap + 1) * ConstantsManager.COUNTDOWN_INTERVAL_NORMAL);
        mTimerView.setProgress(progress);
        if (progress % 1000 == 0) {
            mTimerLabel.setText(String.valueOf((ConstantsManager.ROUND_LENGTH - progress)/1000));
        }
        mCurrentMillisecond = progress;
        mPreBoostProgress = mCurrentMillisecond;
        if (!hasEnemiesAnswered()) checkIfEnemiesAnswered(ConstantsManager.COUNTDOWN_INTERVAL_NORMAL);
        return Observable.just(hasUserAnswered());
    }

    private Observable<Boolean> onBoost(long lap) {
        Timber.d("onBoost");
        int progress = (int) ((lap + 1) * ConstantsManager.COUNTDOWN_INTERVAL_BOOST) * ConstantsManager.SPEED_BOOST;
        mTimerView.setProgress(mPreBoostProgress + progress);
        mTimerLabel.setText(String.valueOf((ConstantsManager.ROUND_LENGTH - (mPreBoostProgress + progress))/1000));
        mCurrentMillisecond = mPreBoostProgress + progress;
        if (!hasEnemiesAnswered()) checkIfEnemiesAnswered(ConstantsManager.COUNTDOWN_INTERVAL_BOOST * ConstantsManager.SPEED_BOOST);
        return Observable.just(isTimeLeft());
    }

    private boolean isTimeLeft() {
        return mCurrentMillisecond < ConstantsManager.ROUND_LENGTH;
    }


    private void checkIfEnemiesAnswered(int interval) {
        for (int i = 0; i < enemiesCount; i++) {
            Player enemy = mEnemies.get(i);
            if (enemy.getAnswerTime()/interval == mCurrentMillisecond/interval){
                TextView indicator = mEnemyAnswerIndicators.get(mNeededEnemiesPositions[i]);
                indicator.setText(String.valueOf(++mEnemiesAnsweredCount));
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
        Timber.d("onClick");
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
                .doOnNext(this::bindData).delay(2, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
                .flatMap(questions -> Observable
                        .interval(ConstantsManager.COUNTDOWN_INTERVAL_NORMAL, TimeUnit.MILLISECONDS))
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
