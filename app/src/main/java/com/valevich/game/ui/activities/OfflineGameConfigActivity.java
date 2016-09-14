package com.valevich.game.ui.activities;

import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.valevich.game.DataManager;
import com.valevich.game.GameApplication;
import com.valevich.game.R;
import com.valevich.game.network.model.LastUpdateModel;
import com.valevich.game.storage.model.Question;
import com.valevich.game.util.ConstantsManager;
import com.valevich.game.util.NetworkStateChecker;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.ViewsById;
import org.androidannotations.annotations.res.StringRes;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@EActivity(R.layout.activity_offline_config)
public class OfflineGameConfigActivity extends AppCompatActivity {

    @ViewById(R.id.root)
    FrameLayout mRootView;

    @ViewById(R.id.toolbar)
    Toolbar mToolbar;

    @ViewsById({R.id.coin_one, R.id.coin_five, R.id.coin_ten, R.id.coin_twenty, R.id.coin_fifty})
    List<ImageView> mCoins;

    @ViewsById({R.id.man_one, R.id.man_two, R.id.man_three, R.id.man_four, R.id.man_five})
    List<ImageView> mManIcons;

    @ViewById(R.id.start_game_button)
    TextView mStartGameButton;

    @ViewById(R.id.bet_value)
    TextView mBetValueLabel;

    @ViewById(R.id.players_number_value)
    TextView mPlayersNumberValueLabel;

    @ViewById(R.id.transparent_loading)
    FrameLayout mTransparentBg;

    @ViewById(R.id.progress_bar)
    MaterialProgressBar mProgressBar;

    @StringRes(R.string.network_error_message)
    String mNetworkErrorMessage;

    @StringRes(R.string.network_unavailbale_message)
    String mNetworkUnavailableMessage;

    @StringRes(R.string.try_again_message)
    String mRetryMessage;

    @StringRes(R.string.no_questions)
    String mNoQuestionsMessage;

    @Bean
    NetworkStateChecker mNetworkStateChecker;

    @Bean
    DataManager mDataManager;

    private Subscription mSubscription;

    private Observable<Question> mQuestionsObservable;

    private List<Question> mQuestions = new ArrayList<>();

    private List<Question> mCachedQuestions = new ArrayList<>();

    private int mPlayersCount = 2;

    private int mCoinPosition;

    private int mBet = 1;

    @AfterViews
    void setUpViews() {
        setupActionBar();
        setUpCoins();
        setUpManIcons();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mQuestionsObservable != null) {
            setButtonClickable(false);
            notifyLoading(View.VISIBLE);
            subscribeToQuestions();
        }
    }

    @Override
    protected void onStop() {
        if (mSubscription != null && !mSubscription.isUnsubscribed()) mSubscription.unsubscribe();
        clearQuestions();
        setButtonClickable(true);
        notifyLoading(View.GONE);
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.enter_fade_in, R.anim.exit_push_out);
    }

    @Click(R.id.start_game_button)
    void onStartGamePressed() {
        setButtonClickable(false);
        notifyLoading(View.VISIBLE);
        setQuestionsObservable();
        subscribeToQuestions();
    }

    private void setQuestionsObservable() {
        mQuestionsObservable = isNetworkAvailable()
                ? checkLastUpdate()
                : getCachedQuestions();
    }

    private void subscribeToQuestions() {
        mSubscription = mQuestionsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(question -> mQuestions.add(question), throwable -> {
                    clearQuestions();
                    setButtonClickable(true);
                    notifyLoading(View.GONE);
                    notifyUserWith(throwable instanceof SocketTimeoutException
                            ? mNetworkErrorMessage
                            : throwable.getMessage());
                }, () -> {
                    if (mQuestions.size() == ConstantsManager.MAX_QUESTIONS_COUNT)
                        startGame();
                    else {
                        clearQuestions();
                        setButtonClickable(true);
                        notifyLoading(View.GONE);
                    }
                });
    }

    private void clearQuestions() {
        mQuestions.clear();
        mCachedQuestions.clear();
    }

    private void startGame() {
        Question[] questions = new Question[mQuestions.size()];
        questions = mQuestions.toArray(questions);
        TourActivity_.intent(this)
                .enemiesCount(mPlayersCount + 1)
                .bet(mBet)
                .parcelableQuestions(questions)
                .start();
        finish();
    }

    private void setupActionBar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void setUpCoins() {
        mCoins.get(0).setImageResource(R.drawable.coin1gold);
        mBetValueLabel.setText(String.valueOf(ConstantsManager.DEFAULT_BET));
        for (int i = 0; i < mCoins.size(); i++) {
            ImageView coin = mCoins.get(i);
            final int position = i;
            coin.setOnClickListener(view -> setCoin(position));
        }
    }

    private void setUpManIcons() {
        mPlayersNumberValueLabel.setText(String.valueOf(ConstantsManager.DEFAULT_ENEMIES_COUNT));
        for (int i = 0; i < mManIcons.size(); i++) {
            ImageView manIcon = mManIcons.get(i);
            if (i < ConstantsManager.DEFAULT_ENEMIES_COUNT)
                manIcon.setImageResource(R.drawable.manicongold);
            final int count = i;
            manIcon.setOnClickListener(view -> setPlayersCount(count));
        }
    }

    private void setPlayersCount(int count) {
        if (count < mPlayersCount) {
            for (int i = count + 1; i <= mPlayersCount; i++) {
                ImageView manIcon = mManIcons.get(i);
                manIcon.setImageResource(R.drawable.manicongrey);
            }
            mPlayersCount = count;
            mPlayersNumberValueLabel.setText(String.valueOf(mPlayersCount + 1));

        } else if (count > mPlayersCount) {
            for (int i = mPlayersCount + 1; i <= count; i++) {
                ImageView manIcon = mManIcons.get(i);
                manIcon.setImageResource(R.drawable.manicongold);
            }
            mPlayersCount = count;
            mPlayersNumberValueLabel.setText(String.valueOf(mPlayersCount + 1));
        }
    }

    private void setCoin(int position) {
        if(position != mCoinPosition) {
            ImageView newCoin = mCoins.get(position);
            ImageView oldCoin = mCoins.get(mCoinPosition);
            newCoin.setImageResource(getImageResAt(position, true));
            oldCoin.setImageResource(getImageResAt(mCoinPosition, false));
            mCoinPosition = position;

            mBetValueLabel.setText(String.valueOf(mBet));
        }
    }

    private int getImageResAt(int position, boolean isHighlighted) {
        switch (position) {
            case 0:
                if(isHighlighted) {
                    mBet = 1;
                    return  R.drawable.coin1gold;
                } else return R.drawable.coin1;
            case 1:
                if(isHighlighted) {
                    mBet = 5;
                    return R.drawable.coin5gold;
                } else return R.drawable.coin5;

            case 2:
                if(isHighlighted) {
                    mBet = 10;
                    return R.drawable.coin10gold;
                } else return R.drawable.coin10;

            case 3:
                if(isHighlighted) {
                    mBet = 20;
                    return R.drawable.coin20gold;
                } else return R.drawable.coin20;

            case 4:
                if(isHighlighted) {
                    mBet = 50;
                    return R.drawable.coin50gold;
                } else return R.drawable.coin50;

            default:
                throw new RuntimeException("Invalid coin position");
        }
    }

    private Observable<Question> loadFreshQuestions(String lastUpdate) {
        return isNetworkAvailable()
                ? downloadQuestionsAndGet(lastUpdate)
                : Observable.error(new RuntimeException(mNetworkUnavailableMessage));
    }

    private Observable<Question> downloadQuestionsAndGet(String lastUpdate) {
        return mDataManager.downloadQuestions()
                .doOnNext(Collections::shuffle)
                .flatMap(questions -> mDataManager.saveQuestions(questions))
                .doOnNext(questions -> GameApplication.setLastUpdateId(lastUpdate))
                .flatMap(questions -> getCachedQuestions());
    }

    private Observable<Question> getCachedQuestions() {
        return isNetworkAvailable() ? getQuestionsWithPossibleMedia() : getQuestionsWithoutMedia();
    }

    private Observable<Question> getQuestionsWithoutMedia() {
        return mDataManager.getQuestions(ConstantsManager.MAX_QUESTIONS_COUNT, false)
                .flatMap(this::getQuestionsIfEnough);
    }

    private Observable<Question> getQuestionsWithPossibleMedia() {
        return mDataManager.getQuestions(ConstantsManager.MAX_QUESTIONS_COUNT, true)
                .flatMap(this::getQuestionsIfEnough)
                .flatMap(question ->
                        question.getMediaPath() != null
                                ? loadQuestionMedia(question)
                                : Observable.just(question));
    }

    private Observable<Question> getQuestionsIfEnough(List<Question> questions) {
        if (questions.size() < ConstantsManager.MAX_QUESTIONS_COUNT) {
            return Observable.error(new RuntimeException(mNoQuestionsMessage));
        } else {
            mCachedQuestions = questions;
            return Observable.from(questions);
        }
    }

    private Observable<Question> loadQuestionMedia(Question question) {
        return mDataManager.downloadQuestionsMedia(question)
                .onErrorResumeNext(replaceWithNonMedia());
    }

    private Observable<Question> replaceWithNonMedia() {
        List<String> questions = new ArrayList<>();
        for (int i = 0; i < mCachedQuestions.size(); i++) {
            Question q = mCachedQuestions.get(i);
            if (q.getMediaPath() == null)
                questions.add(q.getTextQuest());
        }
        return mDataManager.replaceWithNonMedia(questions).flatMap(newQuestion -> newQuestion == null
                ? Observable.error(new RuntimeException(mNoQuestionsMessage))
                : Observable.just(newQuestion));
    }

    private Observable<Question> checkLastUpdate() {
        return mDataManager.getLastUpdate()
                .onErrorResumeNext(throwable -> Observable.just(new LastUpdateModel("")))
                .flatMap(lastUpdateModel ->
                        GameApplication.getLastUpdateId().equals(lastUpdateModel.getId()) || lastUpdateModel.getId().isEmpty()
                                ? getCachedQuestions()
                                : loadFreshQuestions(lastUpdateModel.getId()));
    }

    private boolean isNetworkAvailable() {
        return mNetworkStateChecker.isNetworkAvailable();
    }

    private void notifyUserWith(String message) {
        Snackbar snackbar = Snackbar.make(mRootView, message, Snackbar.LENGTH_LONG)
                .setActionTextColor(ContextCompat.getColor(this, R.color.colorAccent));

        View view = snackbar.getView();
        TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);

        snackbar.show();
    }

    private void notifyLoading(int visibility) {
        mTransparentBg.setVisibility(visibility);
        mProgressBar.setVisibility(visibility);
    }

    private void setButtonClickable(boolean isClickable) {
        mStartGameButton.setClickable(isClickable);
    }

}
