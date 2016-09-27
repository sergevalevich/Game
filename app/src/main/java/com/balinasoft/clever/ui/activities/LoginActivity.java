package com.balinasoft.clever.ui.activities;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.balinasoft.clever.R;
import com.balinasoft.clever.network.model.LogInModel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;

import rx.Observable;

@EActivity(R.layout.activity_login)
public class LoginActivity extends AuthActivity {

    @ViewById(R.id.toolbar)
    Toolbar mToolbar;

    @ViewById(R.id.password_restore)
    TextView mRestorePassButton;

    @ViewById(R.id.reg_action)
    TextView mSignUpButton;

    @StringRes(R.string.login_title)
    String mTitle;

    @AfterViews
    void setUpViews() {
        setUpActionBar();
        setUpButtons();
    }

    @Click(R.id.password_restore)
    void restorePass() {
        if(mIsInteractionAllowed) {
            disableInteraction();
            RestoreActivity_.intent(this).start();
        }
    }

    @Click(R.id.reg_action)
    void register() {
        if(mIsInteractionAllowed) {
            disableInteraction();
            SignUpActivity_.intent(this).start();
        }
    }

    @Override
    boolean isInputValid() {
        return mInputFieldValidator.isEmailValid(mEmailField.getText().toString().trim())
                && mInputFieldValidator.isPasswordValid(mPasswordField.getText().toString().trim());
    }

    @Override
    void onInputInvalid() {
        notifyUserWith(mInvalidInputMessage);
    }

    @Override
    Observable<LogInModel> getAuthStream() {
        return mDataManager.logIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
    }

    private void setUpActionBar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            setTitle(mTitle);
        }
    }

    private void setUpButtons() {
        mRestorePassButton.setTextColor(ContextCompat.getColorStateList(this,R.color.auth_text_colors_dark));
        mSignUpButton.setTextColor(ContextCompat.getColorStateList(this,R.color.auth_text_colors_light));
    }
}
