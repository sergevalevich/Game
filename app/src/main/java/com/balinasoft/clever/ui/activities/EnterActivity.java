package com.balinasoft.clever.ui.activities;

import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.balinasoft.clever.GameApplication;
import com.balinasoft.clever.R;
import com.balinasoft.clever.eventbus.EventBus;
import com.balinasoft.clever.eventbus.events.AvatarSelectedEvent;
import com.balinasoft.clever.eventbus.events.UserNameSelectedEvent;
import com.balinasoft.clever.ui.dialogs.AvatarDialog_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

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

    @Bean
    EventBus mEventBus;

    @AfterViews
    void setUpViews() {
        setUpUserImage();
        setUpUserName();
    }

    @Click(R.id.offline_game_btn)
    void onOfflinePicked() {
        navigateToGameConfig();
    }

    @Click(R.id.user_image)
    void onImageClicked() {
        showDialog();
    }

    @Click(R.id.user_name)
    void onNameClicked() {
        showDialog();
    }

    @Override
    protected void onStart() {
        super.onStart();
        subscribeBus();
        setUpUserCoins();
        setUpUserPoints();
        showViews();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unSubscribeBus();
        mLogo.setVisibility(View.INVISIBLE);
        mOfflineGameButton.setVisibility(View.INVISIBLE);
    }

    @Subscribe
    public void onAvatarSelected(AvatarSelectedEvent event) {
        mUserImage.setImageResource(event.getAvatarResId());
    }

    @Subscribe
    public void onUserNameSelected(UserNameSelectedEvent event) {
        mUserNameLabel.setText(event.getUserName());
    }

    private void subscribeBus() {
        mEventBus.register(this);
    }

    private void unSubscribeBus() {
        mEventBus.unregister(this);
    }

    private void showDialog() {
        AvatarDialog_.builder()
                .currentName(GameApplication.getUserName())
                .build()
                .show(getSupportFragmentManager(),"choose_image");
    }

    private void setUpUserPoints() {
        mPointsLabel.setText(String.valueOf(GameApplication.getUserScore()));
    }

    private void setUpUserCoins() {
        mCoinsLabel.setText(String.valueOf(GameApplication.getUserCoins()));
    }

    private void setUpUserName() {
        mUserNameLabel.setText(GameApplication.getUserName());
    }

    private void setUpUserImage() {
        mUserImage.setImageResource(GameApplication.getUserImage());
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

    private void navigateToGameConfig() {
        OfflineGameConfigActivity_.intent(this).start().withAnimation(R.anim.enter_pull_in, R.anim.exit_fade_out);
    }

}
