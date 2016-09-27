package com.balinasoft.clever.ui.activities;


import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.balinasoft.clever.R;
import com.balinasoft.clever.util.InputFieldValidator;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;

@EActivity
public abstract class InputActivity extends BaseActivity {

    @ViewById(R.id.transparent_loading)
    FrameLayout mTransparentBg;

    @ViewById(R.id.input_button)
    TextView mInputButton;

    @Bean
    InputFieldValidator mInputFieldValidator;

    @StringRes(R.string.invalid_input)
    String mInvalidInputMessage;

    @Override
    protected void onResume() {
        super.onResume();
        hideTransparentBg();
        enableInteraction();
    }

    @Click(R.id.input_button)
    void processInput() {
        hideKeyBoard();
        if(mIsInteractionAllowed) {
            if (isInputValid()) handleInput();
            else onInputInvalid();
        }
    }

    void showTransparentBg() {
        mTransparentBg.setVisibility(View.VISIBLE);
    }

    void hideTransparentBg() {
        mTransparentBg.setVisibility(View.GONE);
    }

    private void hideKeyBoard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    abstract boolean isInputValid();

    abstract void onInputInvalid();

    abstract void handleInput();
}
