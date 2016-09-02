package com.valevich.game.ui.activities;

import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.valevich.game.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;

@EActivity(R.layout.activity_offline_config)
public class OfflineGameConfigActivity extends AppCompatActivity {

    @ViewById(R.id.root)
    RelativeLayout mRootView;

    @ViewById(R.id.toolbar)
    Toolbar mToolbar;

    @ViewById(R.id.seekBar_players)
    SeekBar mPlayersSeekBar;

    @ViewById(R.id.seekBar_bet)
    SeekBar mBetSeekBar;

    @ViewById(R.id.players_number_value)
    TextView mPlayersNumberValueLabel;

    @ViewById(R.id.bet_value)
    TextView mBetValueLabel;

    @StringRes(R.string.offline)
    String mModeTitle;

    @StringRes(R.string.set_players_count_msg)
    String mSetPlayersCountMessage;

    @StringRes(R.string.set_bet_msg)
    String mSetBetMessage;

    @AfterViews
    void setUpViews() {
        setupActionBar();
        setUpSeekBars();
    }

    @Click(R.id.start_game_button)
    void onStartGamePressed() {
        int enemiesCount = mPlayersSeekBar.getProgress();
        int bet = mBetSeekBar.getProgress();
        if (enemiesCount == 0) notifyUserWith(mSetPlayersCountMessage);
        else if (bet == 0) notifyUserWith(mSetBetMessage);
        else startGameWith(enemiesCount, bet);
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

    private void setUpSeekBars() {
        mPlayersSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                mPlayersNumberValueLabel.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mBetSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                mBetValueLabel.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void notifyUserWith(String message) {
        Snackbar.make(mRootView, message, Snackbar.LENGTH_LONG).show();
    }

}
