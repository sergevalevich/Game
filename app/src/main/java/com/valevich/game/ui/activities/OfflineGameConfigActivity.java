package com.valevich.game.ui.activities;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.valevich.game.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.ViewsById;
import org.androidannotations.annotations.res.StringRes;

import java.util.List;
import java.util.Locale;

@EActivity(R.layout.activity_offline_config)
public class OfflineGameConfigActivity extends AppCompatActivity {

    @ViewById(R.id.root)
    RelativeLayout mRootView;

    @ViewById(R.id.toolbar)
    Toolbar mToolbar;

    @ViewsById({R.id.coin_one, R.id.coin_five, R.id.coin_ten, R.id.coin_twenty, R.id.coin_fifty})
    List<ImageView> mCoins;

    @ViewsById({R.id.man_one, R.id.man_two, R.id.man_three, R.id.man_four, R.id.man_five})
    List<ImageView> mManIcons;

    @StringRes(R.string.offline)
    String mModeTitle;

    private int mPlayersCount;

    private int mCoinPosition;

    private int mBet = 1;

    @AfterViews
    void setUpViews() {
        setupActionBar();
        setUpCoins();
        setUpManIcons();
    }

    @Click(R.id.start_game_button)
    void onStartGamePressed() {
        startGameWith(mPlayersCount + 1, mBet);
    }

    private void startGameWith(int enemiesCount, int bet) {
        TourActivity_.intent(this).enemiesCount(enemiesCount).bet(bet).start();
    }

    private void setupActionBar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            setTitle(mModeTitle);
        }
    }

    private void setUpCoins() {
        mCoins.get(0).setImageResource(R.drawable.coin1gold);
        for (int i = 0; i < mCoins.size(); i++) {
            ImageView coin = mCoins.get(i);
            final int position = i;
            coin.setOnClickListener(view -> setCoin(position));
        }
    }

    private void setUpManIcons() {
        mManIcons.get(0).setImageResource(R.drawable.manicongold);
        for (int i = 0; i < mManIcons.size(); i++) {
            ImageView manIcon = mManIcons.get(i);
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

        } else if (count > mPlayersCount) {
            for (int i = mPlayersCount + 1; i <= count; i++) {
                ImageView manIcon = mManIcons.get(i);
                manIcon.setImageResource(R.drawable.manicongold);
            }
            mPlayersCount = count;
        }
    }

    private void setCoin(int position) {
        if(position != mCoinPosition) {
            ImageView newCoin = mCoins.get(position);
            ImageView oldCoin = mCoins.get(mCoinPosition);
            newCoin.setImageResource(getImageResAt(position, true));
            oldCoin.setImageResource(getImageResAt(mCoinPosition, false));
            mCoinPosition = position;
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

}
