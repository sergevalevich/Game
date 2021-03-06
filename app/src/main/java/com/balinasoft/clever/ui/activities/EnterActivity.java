package com.balinasoft.clever.ui.activities;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
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
import com.balinasoft.clever.ui.dialogs.AvatarDialog;
import com.balinasoft.clever.ui.dialogs.AvatarDialog_;
import com.balinasoft.clever.util.AnimationHelper;
import com.balinasoft.clever.util.ConstantsManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.squareup.otto.Subscribe;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import timber.log.Timber;

import static com.balinasoft.clever.GameApplication.getCurrentTime;
import static com.balinasoft.clever.GameApplication.getLaunchTime;
import static com.balinasoft.clever.GameApplication.getOnlineCoins;
import static com.balinasoft.clever.GameApplication.getOnlineName;
import static com.balinasoft.clever.GameApplication.getOnlineScore;
import static com.balinasoft.clever.GameApplication.getUserCoins;
import static com.balinasoft.clever.GameApplication.getUserImage;
import static com.balinasoft.clever.GameApplication.getUserName;
import static com.balinasoft.clever.GameApplication.getUserScore;
import static com.balinasoft.clever.GameApplication.isAuthTokenExists;
import static com.balinasoft.clever.GameApplication.isOnlineMode;
import static com.balinasoft.clever.GameApplication.setOnlineMode;
import static com.balinasoft.clever.GameApplication.setSessionTime;

@EActivity
public class EnterActivity extends BaseActivity {

    @ViewById(R.id.root)
    RelativeLayout mRootView;

    @ViewById(R.id.toolbar)
    RelativeLayout mToolbar;

    @ViewById(R.id.offline_game_btn)
    TextView mOfflineGameButton;

    @ViewById(R.id.online_game_btn)
    TextView mOnlineGameButton;

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

    @Bean
    AnimationHelper mAnimationHelper;

    @Extra
    String message;

    private AvatarDialog mDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter);
        checkGooglePlayServices();
    }

    @AfterViews
    void setUpViews() {
        if(message != null && !message.isEmpty()) notifyUserWith(message);
        setUpUserImage();
        showViews();
    }

    @Click(R.id.offline_game_btn)
    void onOfflinePicked() {
        if(isOnlineMode()) {
            setOnlineMode(false);
            changeHeader();
        } else {
            playOffline();
        }
    }

    @Click(R.id.online_game_btn)
    void onOnlinePicked() {
        if (isAuthTokenExists()) {
            if(!isOnlineMode()) {
                setOnlineMode(true);
                changeHeader();
            } else {
                playOnline();
            }
        } else {
            navigateToLogin();
        }
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
        setUserInfo();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unSubscribeBus();
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

    private void changeHeader() {
        Animation slideUpAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_up_header);
        Animation slideDownAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        mAnimationHelper.setAnimationListener(slideUpAnimation, () -> {
            mToolbar.setVisibility(View.INVISIBLE);
            setUserInfo();
            mToolbar.startAnimation(slideDownAnimation);
        },null,null);
        mAnimationHelper.setAnimationListener(slideDownAnimation, () -> {
            mToolbar.setVisibility(View.VISIBLE);
            play();
        },null,null);
        mToolbar.startAnimation(slideUpAnimation);
    }

    private void setUserInfo() {
        if(isOnlineMode() && isAuthTokenExists()) showOnlineInfo();
        else showOfflineInfo();
    }

    private void showOnlineInfo() {
        setUpUserName(getOnlineName());
        setUpUserCoins(getOnlineCoins());
        setUpUserPoints(getOnlineScore());
    }

    private void showOfflineInfo() {
        setUpUserName(getUserName());
        setUpUserCoins(getUserCoins());
        setUpUserPoints(getUserScore());
    }

    private void showDialog() {
        if(mDialog == null) mDialog = createDialog();
        mDialog.show(getSupportFragmentManager(), ConstantsManager.AVATAR_DIALOG_TAG);
    }

    private AvatarDialog createDialog() {
        return AvatarDialog_.builder()
                .isOfflineMode(!(isOnlineMode() && isAuthTokenExists()))
                .build();
    }

    private void setUpUserPoints(int points) {
        mPointsLabel.setText(String.valueOf(points));
    }

    private void setUpUserCoins(int coins) {
        mCoinsLabel.setText(String.valueOf(coins));
    }

    private void setUpUserName(String username) {
        mUserNameLabel.setText(username);
    }

    private void setUpUserImage() {
        mUserImage.setImageResource(getUserImage());
    }

    private void showViews() {

        Animation slideDownAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        Animation scaleLogo = AnimationUtils.loadAnimation(this, R.anim.scale);
        Animation slideInLeft = AnimationUtils.loadAnimation(this, R.anim.slide_in_left);
        Animation slideInRight = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);

        mAnimationHelper.setAnimationListener(slideDownAnimation, null, () -> {
            mToolbar.setVisibility(View.VISIBLE);
            mLogo.setVisibility(View.VISIBLE);
            mLogo.startAnimation(scaleLogo);
        },null);

        mAnimationHelper.setAnimationListener(scaleLogo, () -> {
            mOfflineGameButton.setVisibility(View.VISIBLE);
            mOfflineGameButton.startAnimation(slideInLeft);
        },null,null);

        mAnimationHelper.setAnimationListener(slideInLeft, null, () -> {
            mOnlineGameButton.setVisibility(View.VISIBLE);
            mOnlineGameButton.startAnimation(slideInRight);
        },null);

        mToolbar.startAnimation(slideDownAnimation);
    }

    private void play() {
        if (isOnlineMode()) playOnline();
        else playOffline();
    }

    private void playOffline() {
        OfflineGameConfigActivity_.intent(this).start().withAnimation(R.anim.enter_pull_in, R.anim.exit_fade_out);
    }

    private void playOnline() {
        MainActivity_.intent(this).start().withAnimation(R.anim.enter_pull_in, R.anim.exit_fade_out);
    }

    private void navigateToLogin() {
        LoginActivity_.intent(this).start();
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

    private void notifyUserWith(String message) {
        Snackbar.make(mRootView,message,Snackbar.LENGTH_LONG).show();
    }

    private boolean checkGooglePlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.makeGooglePlayServicesAvailable(this);
            } else {finish();}
            return false;
        }
        return true;
    }

}
