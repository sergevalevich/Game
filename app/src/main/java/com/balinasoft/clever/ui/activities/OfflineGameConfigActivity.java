package com.balinasoft.clever.ui.activities;

import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.balinasoft.clever.DataManager;
import com.balinasoft.clever.GameApplication;
import com.balinasoft.clever.R;
import com.balinasoft.clever.storage.model.Question;
import com.balinasoft.clever.util.ConstantsManager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.ViewsById;
import org.androidannotations.annotations.res.StringRes;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.balinasoft.clever.GameApplication.getUserCoins;

@EActivity(R.layout.activity_offline_config)
public class OfflineGameConfigActivity extends BaseActivity {

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

    @StringRes(R.string.try_again_message)
    String mRetryMessage;

    @StringRes(R.string.not_enough_coins)
    String mNotEnoughCoinsMessage;

    @Bean
    DataManager mDataManager;

    private Subscription mSubscription;

    private List<Question> mQuestions = new ArrayList<>();

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
        if (mSubscription != null) {
            setButtonClickable(false);
            notifyLoading(View.VISIBLE);
            getQuestions();
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
        if(areEnoughCoins()) {
            setButtonClickable(false);
            notifyLoading(View.VISIBLE);
            getQuestions();
        } else {
            notifyUserWith(mNotEnoughCoinsMessage);
        }
    }

    private boolean areEnoughCoins() {
        return getUserCoins() >= mBet;
    }

    private void getQuestions() {
        mSubscription = mDataManager.getQuestions()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onNext, this::onError, this::onCompleted);
    }

    private void onNext(Question question) {
        mQuestions.add(question);
    }

    private void onError(Throwable throwable) {
        clearQuestions();
        setButtonClickable(true);
        notifyLoading(View.GONE);
        notifyUserWith(throwable instanceof SocketTimeoutException
                ? mNetworkErrorMessage
                : throwable.getMessage());
    }

    private void onCompleted() {
        if (mQuestions.size() == ConstantsManager.MAX_QUESTIONS_COUNT)
            startGame();
        else {
            clearQuestions();
            setButtonClickable(true);
            notifyLoading(View.GONE);
        }
    }

    private void clearQuestions() {
        mQuestions.clear();
    }

    private void startGame() {
        Question[] questions = new Question[mQuestions.size()];
        questions = mQuestions.toArray(questions);
        TourActivityOffline_.intent(this)
                .enemiesCount(mPlayersCount + 1)
                .bet(mBet)
                .parcelableQuestions(questions)
                .start();
        finish();
    }

    private void setupActionBar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            setTitle("");
        }
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

    private void notifyUserWith(String message) {
        Snackbar snackbar = Snackbar.make(mRootView, message, Snackbar.LENGTH_LONG);

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
