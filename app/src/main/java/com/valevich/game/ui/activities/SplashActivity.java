package com.valevich.game.ui.activities;

import android.support.v7.app.AppCompatActivity;

import com.valevich.game.R;

import org.androidannotations.annotations.EActivity;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;

@EActivity(R.layout.activity_splash)
public class SplashActivity extends AppCompatActivity {

    private Subscription mSubscription;

    @Override
    protected void onResume() {
        super.onResume();
        mSubscription = Observable.timer(1, TimeUnit.SECONDS).subscribe(
                aLong -> {},
                throwable -> {},
                this::enter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mSubscription != null && !mSubscription.isUnsubscribed()) mSubscription.unsubscribe();
    }

    private void enter() {
        EnterActivity_.intent(this).start();
        finish();
    }
}
