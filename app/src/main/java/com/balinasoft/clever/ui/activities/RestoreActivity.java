package com.balinasoft.clever.ui.activities;

import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.balinasoft.clever.DataManager;
import com.balinasoft.clever.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@EActivity(R.layout.activity_restore_pass)
public class RestoreActivity extends InputActivity {

    @ViewById(R.id.root)
    RelativeLayout mRootView;

    @ViewById(R.id.toolbar)
    Toolbar mToolbar;

    @ViewById(R.id.email)
    EditText mEmailField;

    @StringRes(R.string.restore)
    String mTitle;

    @StringRes(R.string.restore_error)
    String mRestoreFailedMessage;

    @StringRes(R.string.network_unavailbale_message)
    String mNetworkUnavailableMessage;

    @Bean
    DataManager mDataManager;

    private Subscription mRestoreSub;

    @AfterViews
    void setUpViews() {
        setUpActionBar();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unSubscribe();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    boolean isInputValid() {
        return mInputFieldValidator.isEmailValid(mEmailField.getText().toString().trim());
    }

    @Override
    void onInputInvalid() {
        notifyUserWith(mInvalidInputMessage);
    }

    @Override
    void handleInput() {
        if(mIsInteractionAllowed) {
            if (mNetworkStateChecker.isNetworkAvailable()) {
                disableInteraction();
                showTransparentBg();
                mRestoreSub = mDataManager.restore(mEmailField.getText().toString())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(restoreModel -> {
                            hideTransparentBg();
                            enableInteraction();
                            notifyUserWith(restoreModel.getMessage());
                        }, throwable -> {
                            hideTransparentBg();
                            enableInteraction();
                            notifyUserWith(throwable.getLocalizedMessage().isEmpty()
                                    ? mRestoreFailedMessage
                                    : throwable.getLocalizedMessage());
                        });
            } else {
                notifyUserWith(mNetworkUnavailableMessage);
            }
        }
    }

    private void unSubscribe() {
        if (mRestoreSub != null && !mRestoreSub.isUnsubscribed()) mRestoreSub.unsubscribe();
    }

    private void notifyUserWith(String message) {
        Snackbar.make(mRootView, message, Snackbar.LENGTH_LONG).show();
    }

    private void setUpActionBar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            setTitle(mTitle);
        }
    }

}
