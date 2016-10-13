package com.balinasoft.clever.ui.activities;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.balinasoft.clever.R;
import com.balinasoft.clever.util.AnimationHelper;
import com.balinasoft.clever.util.BonusHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import static com.balinasoft.clever.GameApplication.isAuthTokenExists;

@EActivity(R.layout.activity_splash)
public class SplashActivity extends BaseActivity implements BonusHelper.BonusListener {

    @ViewById(R.id.logo)
    ImageView mLogo;

    @Bean
    AnimationHelper mAnimationHelper;

    @Bean
    BonusHelper mBonusHelper;

    @Override
    public void success() {
        showBonus();
    }

    @Override
    public void fail() {
        enter();
    }

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
        if(isAuthTokenExists()) MainActivity_.intent(this).start();
        else EnterActivity_.intent(this).start();
        finish();
    }

    private void showBonus() {
        BonusActivity_.intent(this).start();
        finish();
    }

    private void checkBonus() {
        mBonusHelper.setBonusListener(this);
        mBonusHelper.checkBonus();
    }

}
