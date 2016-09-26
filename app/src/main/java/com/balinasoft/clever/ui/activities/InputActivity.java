package com.balinasoft.clever.ui.activities;


import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.balinasoft.clever.R;
import com.balinasoft.clever.util.InputFieldValidator;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import rx.Observable;
import rx.Subscription;

@EActivity
public abstract class InputActivity extends BaseActivity {

    @ViewById(R.id.transparent_loading)
    FrameLayout mTransparentBg;

    @ViewById(R.id.input_button)
    TextView mInputButton;

    @Bean
    InputFieldValidator mInputFieldValidator;

    boolean mIsInteractionAllowed;

    private Subscription mFieldsSub;

    void unSubscribe() {
        if (mFieldsSub != null && !mFieldsSub.isUnsubscribed()) mFieldsSub.unsubscribe();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpAuthFields();
        hideTransparentBg();
        enableInteraction();
    }

    void disableInteraction() {
        mIsInteractionAllowed = false;
    }

    void enableInteraction() {
        mIsInteractionAllowed = true;
    }

    void showTransparentBg() {
        mTransparentBg.setVisibility(View.VISIBLE);
    }

    void hideTransparentBg() {
        mTransparentBg.setVisibility(View.GONE);
    }

    void hideKeyBoard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    abstract Observable<Boolean> getFieldsChanges();

    private void setUpAuthFields() {
        mFieldsSub = getFieldsChanges().subscribe(this::setAuthButtonEnabled);
    }

    private void setAuthButtonEnabled(boolean isInputValid) {
        mInputButton.setEnabled(isInputValid);
    }
}
