package com.balinasoft.clever.ui.activities;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.balinasoft.clever.DataManager;
import com.balinasoft.clever.R;
import com.balinasoft.clever.eventbus.EventBus;
import com.balinasoft.clever.eventbus.events.AvatarSelectedEvent;
import com.balinasoft.clever.eventbus.events.UserNameSelectedEvent;
import com.balinasoft.clever.services.QuestionStatsService_;
import com.balinasoft.clever.services.UserStatsService_;
import com.balinasoft.clever.ui.dialogs.AvatarDialog_;
import com.balinasoft.clever.util.ConstantsManager;
import com.squareup.otto.Subscribe;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import timber.log.Timber;

import static com.balinasoft.clever.GameApplication.getCurrentTime;
import static com.balinasoft.clever.GameApplication.getLaunchTime;
import static com.balinasoft.clever.GameApplication.getUserCoins;
import static com.balinasoft.clever.GameApplication.getUserImage;
import static com.balinasoft.clever.GameApplication.getUserName;
import static com.balinasoft.clever.GameApplication.getUserScore;
import static com.balinasoft.clever.GameApplication.setSessionTime;

@EActivity(R.layout.activity_enter)
public class EnterActivity extends BaseActivity {

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

    @Bean
    DataManager mDataManager;

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

    @Override
    public void onBackPressed() {
        sendStats();
        super.onBackPressed();
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
                .currentName(getUserName())
                .build()
                .show(getSupportFragmentManager(), ConstantsManager.AVATAR_DIALOG_TAG);
    }

    private void setUpUserPoints() {
        mPointsLabel.setText(String.valueOf(getUserScore()));
    }

    private void setUpUserCoins() {
        mCoinsLabel.setText(String.valueOf(getUserCoins()));
    }

    private void setUpUserName() {
        mUserNameLabel.setText(getUserName());
    }

    private void setUpUserImage() {
        mUserImage.setImageResource(getUserImage());
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

    private void sendStats() {
        Timber.d("on back pressed %s",String.valueOf(RESUMED_ACTIVITIES_COUNT));
        if(mNetworkStateChecker.isNetworkAvailable()) {
            sendQuestionsStats();
            if(--RESUMED_ACTIVITIES_COUNT == 0) {
                setSessionTime(getCurrentTime() - getLaunchTime());
                sendUserStats();
            }
        }
    }

    private void sendQuestionsStats() {
        QuestionStatsService_.intent(this).start();
    }

    private void sendUserStats() {
        UserStatsService_.intent(this).start();
    }

}
