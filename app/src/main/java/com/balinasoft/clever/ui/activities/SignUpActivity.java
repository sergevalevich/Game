package com.balinasoft.clever.ui.activities;

import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;

import com.balinasoft.clever.R;
import com.balinasoft.clever.network.model.LogInModel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;

import rx.Observable;

import static com.balinasoft.clever.GameApplication.getDeviceToken;

@EActivity(R.layout.activity_sign_up)
public class SignUpActivity extends AuthActivity {

    @ViewById(R.id.toolbar)
    Toolbar mToolbar;

    @ViewById(R.id.password_repeat)
    EditText mPasswordRepeatField;

    @StringRes(R.string.sign_up_title)
    String mTitle;

    @AfterViews
    void setUpViews() {
        setUpActionBar();
    }

    @Override
    boolean isInputValid() {
        return mInputFieldValidator.isEmailValid(mEmailField.getText().toString().trim())
                && mInputFieldValidator.isPasswordValid(mPasswordField.getText().toString().trim())
                && mPasswordField.getText().toString().trim().equals(mPasswordRepeatField.getText().toString().trim());
    }

    @Override
    void onInputInvalid() {
        notifyUserWith(mInvalidInputMessage);
    }

    @Override
    Observable<LogInModel> getAuthStream() {
        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();
        return mDataManager.register(getDeviceToken(), email, password)
                .flatMap(registerModel -> registerModel.getSuccess() == 1
                        ? mDataManager.logIn(email, password)
                        : Observable.error(new RuntimeException(registerModel.getMessage())));
    }

    private void setUpActionBar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            setTitle(mTitle);
        }
    }
}
