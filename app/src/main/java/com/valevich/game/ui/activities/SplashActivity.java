package com.valevich.game.ui.activities;

import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.trello.rxlifecycle.components.RxActivity;
import com.valevich.game.DataManager;
import com.valevich.game.R;
import com.valevich.game.storage.model.Question;
import com.valevich.game.util.NetworkStateChecker;
import com.valevich.game.util.Preferences_;
import com.valevich.game.util.UrlFormatter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;
import org.androidannotations.annotations.sharedpreferences.Pref;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@EActivity(R.layout.activity_splash)
public class SplashActivity extends RxActivity {

    @ViewById(R.id.root)
    RelativeLayout mRootView;

    @ViewById(R.id.title)
    TextView mTitle;

    @ViewById(R.id.offline_game_btn)
    TextView mOfflineGameButton;

    @ViewById(R.id.online_game_btn)
    TextView mOnlineGameButton;

    @ViewById(R.id.progress_bar)
    MaterialProgressBar mProgressBar;

    @StringRes(R.string.network_unavailbale_message)
    String mNetworkErrorMessage;

    @StringRes(R.string.try_again_message)
    String mRetryMessage;

    @Bean
    NetworkStateChecker mNetworkStateChecker;

    @Bean
    DataManager mDataManager;

    private Subscription mSubscription;

    private Snackbar mSnackbar;

    @Pref
    Preferences_ mPreferences;

    @Click(R.id.offline_game_btn)
    void onOfflinePicked() {
        toggleButtonsBlock(false);
        dismissButtons();
    }

    @AfterViews
    void setUpViews() {

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!isDataFresh()) loadQuestions();
        else showViews();
//        Question question = new Question();
//        question.setIsPlayed(0);
//        question.setRightAnswer("Right answer");
//        question.setMediaType("photo");
//        question.setAnswers("answer_1,answer_2,answer_3");
//        question.setTextQuest("What da heck?");
//        question.setThemeQuest("general");
//        question.setMediaPath("http://minionomaniya.ru/wp-content/uploads/2015/10/%D0%BC%D0%B8%D0%BD%D1%8C%D0%BE%D0%BD%D1%8B-%D0%BA%D0%B0%D1%80%D1%82%D0%B8%D0%BD%D0%BA%D0%B8-%D0%B2-%D1%85%D0%BE%D1%80%D0%BE%D1%88%D0%B5%D0%BC-%D0%BA%D0%B0%D1%87%D0%B5%D1%81%D1%82%D0%B2%D0%B5.jpg");
//        question.save();
//        Question q = new Question();
//        q.setIsPlayed(0);
//        q.setRightAnswer("Right answer_2");
//        q.setMediaType("photo");
//        q.setAnswers("answer_1,answer_2,answer_3");
//        q.setTextQuest("What?");
//        q.setThemeQuest("general");
//        q.setMediaPath("http://minionomaniya.ru/wp-content/uploads/2015/10/%D0%BC%D0%B8%D0%BD%D1%8C%D0%BE%D0%BD%D1%8B-%D0%BA%D0%B0%D1%80%D1%82%D0%B8%D0%BD%D0%BA%D0%B8-%D0%B2-%D1%85%D0%BE%D1%80%D0%BE%D1%88%D0%B5%D0%BC-%D0%BA%D0%B0%D1%87%D0%B5%D1%81%D1%82%D0%B2%D0%B5.jpg");
//        q.save();
//        Question question1 = new Question();
//        question1.setIsPlayed(0);
//        question1.setRightAnswer("Right answer_3");
//        question1.setMediaType("photo");
//        question1.setAnswers("answer_1,answer_2,answer_3");
//        question1.setTextQuest("Where?");
//        question1.setThemeQuest("general");
//        question1.setMediaPath("http://minionomaniya.ru/wp-content/uploads/2015/10/%D0%BC%D0%B8%D0%BD%D1%8C%D0%BE%D0%BD%D1%8B-%D0%BA%D0%B0%D1%80%D1%82%D0%B8%D0%BD%D0%BA%D0%B8-%D0%B2-%D1%85%D0%BE%D1%80%D0%BE%D1%88%D0%B5%D0%BC-%D0%BA%D0%B0%D1%87%D0%B5%D1%81%D1%82%D0%B2%D0%B5.jpg");
//        question1.save();
//        showViews();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mSnackbar!=null && mSnackbar.isShown()) mSnackbar.dismiss();
    }

    @Override
    protected void onDestroy() {
        if (mSubscription != null && !mSubscription.isUnsubscribed()) mSubscription.unsubscribe();
        super.onDestroy();
    }

    private void loadQuestions() {
        if (mNetworkStateChecker.isNetworkAvailable()) {
            notifyLoading();

            mSubscription = mDataManager.loadQuestions()
                    .compose(bindToLifecycle())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(isSuccessful -> {},
                            throwable -> {
                                dismissLoading();
                                notifyUserWith(mNetworkErrorMessage);
                            },
                            () -> {
                                mPreferences.isDataFresh().put(true);
                                dismissLoading();
                                showViews();
                            });

        } else {
            notifyUserWith(mNetworkErrorMessage);
        }
    }

    private void showViews() {

        if(areButtonsVisible()) setButtonsVisibility(View.INVISIBLE);//hide first

        Animation slideDownAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        Animation slideInLeft = AnimationUtils.loadAnimation(this, R.anim.slide_in_left);
        Animation slideInRight = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);


        slideInLeft.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                setButtonsVisibility(View.VISIBLE);
                mOfflineGameButton.startAnimation(slideInRight);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


        slideDownAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mTitle.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mOnlineGameButton.startAnimation(slideInLeft);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mTitle.startAnimation(slideDownAnimation);
    }

    private void dismissButtons() {
        Animation slideOutLeft = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);
        Animation slideOutRight = AnimationUtils.loadAnimation(this, R.anim.slide_out_right);

        slideOutLeft.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mOfflineGameButton.startAnimation(slideOutRight);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                setButtonsVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        slideOutRight.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                setButtonsVisibility(View.INVISIBLE);
                toggleButtonsBlock(true);
                navigateToGameConfig();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mOnlineGameButton.startAnimation(slideOutLeft);
    }

    private void toggleButtonsBlock(boolean isClickable) {
        mOfflineGameButton.setClickable(isClickable);
        mOnlineGameButton.setClickable(isClickable);
    }

    private void navigateToGameConfig() {
        OfflineGameConfigActivity_.intent(this).start();
    }

    private void setButtonsVisibility(int visibility) {
        mOfflineGameButton.setVisibility(visibility);
        mOnlineGameButton.setVisibility(visibility);
    }

    private boolean areButtonsVisible() {
        return mOfflineGameButton.getVisibility() == View.VISIBLE
                || mOnlineGameButton.getVisibility() == View.VISIBLE;
    }

    private boolean isDataFresh() {
        return mPreferences.isDataFresh().get();
    }

    private void notifyUserWith(String message) {
        mSnackbar = Snackbar.make(mRootView, message, Snackbar.LENGTH_INDEFINITE)
                .setAction(mRetryMessage, view -> {
                    loadQuestions();
                })
                .setActionTextColor(ContextCompat.getColor(this,R.color.colorAccent));

        View view = mSnackbar.getView();
        TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);

        mSnackbar.show();
    }

    private void notifyLoading() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void dismissLoading() {
        mProgressBar.setVisibility(View.GONE);
    }

}
