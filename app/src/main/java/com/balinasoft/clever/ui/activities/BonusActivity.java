package com.balinasoft.clever.ui.activities;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.balinasoft.clever.GameApplication;
import com.balinasoft.clever.R;
import com.balinasoft.clever.util.ConstantsManager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.Locale;

import static com.balinasoft.clever.GameApplication.getBonus;

@EActivity(R.layout.activity_bonus)
public class BonusActivity extends BaseActivity {

    @ViewById(R.id.root)
    RelativeLayout mRootView;

    @ViewById(R.id.bonus_area)
    LinearLayout mBonusArea;

    @ViewById(R.id.bonus_area_next)
    LinearLayout mBonusAreaNext;

    @ViewById(R.id.bonus_label)
    TextView mBonusLabel;

    @ViewById(R.id.bonus_label_next)
    TextView mNextBonusLabel;

    @AfterViews
    void setUpViews() {
        setUpBonus();
        showBonus();
    }

    @Click(R.id.root)
    void enter() {
        EnterActivity_.intent(this).start();
        finish();
    }

    private void setUpBonus() {
        int bonus = getBonus();
        int nextBonus = bonus < ConstantsManager.MAX_BONUS ? bonus + 1 : ConstantsManager.MAX_BONUS;
        mBonusLabel.setText(String.format(Locale.getDefault(), "%s%d", "+",bonus));
        mNextBonusLabel.setText(String.valueOf(nextBonus));
    }

    private void showBonus() {
        Animation scaleBonus = AnimationUtils.loadAnimation(this,R.anim.scale);
        Animation scaleNextBonus = AnimationUtils.loadAnimation(this,R.anim.scale);
        scaleBonus.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mBonusAreaNext.setVisibility(View.VISIBLE);
                mBonusAreaNext.startAnimation(scaleNextBonus);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mBonusArea.setVisibility(View.VISIBLE);
        mBonusArea.startAnimation(scaleBonus);
    }

    @Override
    public void onBackPressed() {

    }

}
