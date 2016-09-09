package com.valevich.game.ui.activities;

import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.valevich.game.R;
import com.valevich.game.util.Preferences_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

@EActivity(R.layout.activity_enter)
public class EnterActivity extends AppCompatActivity {

    @ViewById(R.id.root)
    RelativeLayout mRootView;

    @ViewById(R.id.toolbar)
    RelativeLayout mToolbar;

    @ViewById(R.id.offline_game_btn)
    TextView mOfflineGameButton;

    @ViewById(R.id.user_image)
    ImageView mUserImage;

    @ViewById(R.id.user_name)
    TextView mUserNameLabel;

    @ViewById(R.id.coins)
    TextView mCoinsLabel;

    @ViewById(R.id.points)
    TextView mPointsLabel;

    @ViewById(R.id.logo)
    ImageView mLogo;

    @Pref
    Preferences_ mPreferences;

    @AfterViews
    void setUpViews() {
        setUpUserImage();
        setUpUserName();
        setUpUserCoins();
        setUpUserPoints();
    }

    private void setUpUserPoints() {
        mPointsLabel.setText(String.valueOf(mPreferences.userScore().get()));
    }

    private void setUpUserCoins() {
        mCoinsLabel.setText(String.valueOf(mPreferences.userCoins().get()));
    }

    private void setUpUserName() {
        mUserNameLabel.setText(mPreferences.userName().get());
    }

    private void setUpUserImage() {
        mUserImage.setImageResource(mPreferences.userImage().get());
    }

    @Click(R.id.offline_game_btn)
    void onOfflinePicked() {
        //toggleButtonBlock(false);
        //dismissViews();
        navigateToGameConfig();
    }

    @Override
    protected void onStart() {
        super.onStart();
        showViews();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mLogo.setVisibility(View.INVISIBLE);
        mOfflineGameButton.setVisibility(View.INVISIBLE);
    }

    private void showViews() {

        Animation slideDownAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        Animation scaleLogo = AnimationUtils.loadAnimation(this, R.anim.scale);
        Animation scaleButton = AnimationUtils.loadAnimation(this, R.anim.scale);


        slideDownAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mToolbar.setVisibility(View.VISIBLE);
                mLogo.setVisibility(View.VISIBLE);
                mLogo.startAnimation(scaleLogo);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        scaleLogo.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mOfflineGameButton.setVisibility(View.VISIBLE);
                mOfflineGameButton.startAnimation(scaleButton);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mToolbar.startAnimation(slideDownAnimation);
    }

    private void dismissViews() {
        Animation shrinkButton = AnimationUtils.loadAnimation(this, R.anim.shrink);
        Animation shrinkLogo = AnimationUtils.loadAnimation(this, R.anim.shrink);
        shrinkLogo.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mLogo.setVisibility(View.INVISIBLE);
                mOfflineGameButton.startAnimation(shrinkButton);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        shrinkButton.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                toggleButtonBlock(true);
                mOfflineGameButton.setVisibility(View.INVISIBLE);
                navigateToGameConfig();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mLogo.startAnimation(shrinkLogo);
    }

    private void toggleButtonBlock(boolean isClickable) {
        mOfflineGameButton.setClickable(isClickable);
    }

    private void navigateToGameConfig() {
        OfflineGameConfigActivity_.intent(this).start().withAnimation(R.anim.enter_pull_in, R.anim.exit_fade_out);;
    }

}
