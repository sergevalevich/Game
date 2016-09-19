package com.balinasoft.clever.ui.activities;

import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.balinasoft.clever.GameApplication;
import com.balinasoft.clever.R;
import com.balinasoft.clever.util.ConstantsManager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.Calendar;
import java.util.GregorianCalendar;

import timber.log.Timber;

@EActivity(R.layout.activity_splash)
public class SplashActivity extends AppCompatActivity {

    @ViewById(R.id.logo)
    ImageView mLogo;

    @AfterViews
    void showLogo() {
        Animation fadeIn = AnimationUtils.loadAnimation(this,R.anim.fade_in_logo);
        Animation fadeOut = AnimationUtils.loadAnimation(this,R.anim.fade_out_logo);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mLogo.startAnimation(fadeOut);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mLogo.setVisibility(View.INVISIBLE);
                checkBonus();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mLogo.setVisibility(View.VISIBLE);
        mLogo.startAnimation(fadeIn);
    }

    private void enter() {
        EnterActivity_.intent(this).start();
        finish();
    }

    private void showBonus() {
        BonusActivity_.intent(this).start();
        finish();
    }

    private void checkBonus() {
        Timber.d("Checking bonus........");
        GregorianCalendar calendar = (GregorianCalendar) Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_YEAR);
        int savedDay = GameApplication.getLastAppLaunchDay();

        int dayRange;
        if (GameApplication.isFirstLaunch()) {
            dayRange = 1;
            GameApplication.setUserCoins(ConstantsManager.INIT_SCORE);
        } else {
            dayRange = currentDay - savedDay;
        }

        if (dayRange == 1
                || (dayRange == -364
                && !calendar.isLeapYear(calendar.get(Calendar.YEAR) - 1))
                || dayRange == -365) {

            int currentBonus = GameApplication.getBonus();
            if (currentBonus < ConstantsManager.MAX_BONUS) {
                GameApplication.setBonus(++currentBonus);
            }
            GameApplication.setUserCoins(GameApplication.getUserCoins() + currentBonus);
            GameApplication.setLastAppLaunchDay(currentDay);
            showBonus();

        } else if (Math.abs(dayRange) > 1) {
            GameApplication.setBonus(0);
            GameApplication.setLastAppLaunchDay(currentDay);
            enter();

        } else enter();

    }

}
