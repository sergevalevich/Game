package com.balinasoft.clever.ui.activities;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.balinasoft.clever.R;
import com.balinasoft.clever.util.AnimationHelper;
import com.balinasoft.clever.util.ConstantsManager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static com.balinasoft.clever.GameApplication.getBonus;
import static com.balinasoft.clever.GameApplication.getLastAppLaunchDay;
import static com.balinasoft.clever.GameApplication.getUserCoins;
import static com.balinasoft.clever.GameApplication.isFirstLaunch;
import static com.balinasoft.clever.GameApplication.setBonus;
import static com.balinasoft.clever.GameApplication.setLastAppLaunchDay;
import static com.balinasoft.clever.GameApplication.setUserCoins;

@EActivity(R.layout.activity_splash)
public class SplashActivity extends BaseActivity {

    @ViewById(R.id.logo)
    ImageView mLogo;

    @Bean
    AnimationHelper mAnimationHelper;

    @AfterViews
    void showLogo() {
        Animation fadeIn = AnimationUtils.loadAnimation(this,R.anim.fade_in_logo);
        Animation fadeOut = AnimationUtils.loadAnimation(this,R.anim.fade_out_logo);
        mAnimationHelper.setAnimationListener(fadeIn, () -> mLogo.startAnimation(fadeOut),null,null);
        mAnimationHelper.setAnimationListener(fadeOut, () -> {
            mLogo.setVisibility(View.INVISIBLE);
            checkBonus();
        },null,null);

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
        GregorianCalendar calendar = (GregorianCalendar) Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_YEAR);
        int savedDay = getLastAppLaunchDay();

        int dayRange;
        if (isFirstLaunch()) {
            dayRange = 1;
            setUserCoins(ConstantsManager.INIT_SCORE);
        } else {
            dayRange = currentDay - savedDay;
        }

        if (dayRange == 1
                || (dayRange == -364
                && !calendar.isLeapYear(calendar.get(Calendar.YEAR) - 1))
                || dayRange == -365) {

            int currentBonus = getBonus();
            if (currentBonus < ConstantsManager.MAX_BONUS) {
                setBonus(++currentBonus);
            }
            setUserCoins(getUserCoins() + currentBonus);
            setLastAppLaunchDay(currentDay);
            showBonus();

        } else if (Math.abs(dayRange) > 1) {
            setBonus(0);
            setLastAppLaunchDay(currentDay);
            enter();

        } else enter();

    }

}
