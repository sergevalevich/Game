package com.balinasoft.clever.ui.activities;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.balinasoft.clever.DataManager;
import com.balinasoft.clever.R;
import com.balinasoft.clever.network.model.LogInModel;
import com.balinasoft.clever.util.ConstantsManager;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import static com.balinasoft.clever.GameApplication.saveCleverToken;
import static com.balinasoft.clever.GameApplication.saveFacebookToken;
import static com.balinasoft.clever.GameApplication.saveUserId;
import static com.balinasoft.clever.GameApplication.saveVkToken;

@EActivity
public abstract class AuthActivity extends InputActivity {

    @ViewById(R.id.root)
    RelativeLayout mRootView;

    @ViewById(R.id.email)
    EditText mEmailField;

    @ViewById(R.id.password)
    EditText mPasswordField;

    @ViewById(R.id.facebook)
    ImageView mFacebookButton;

    @ViewById(R.id.vk)
    ImageView mVkButton;

    @StringRes(R.string.vk_fail)
    String mVkFailedMessage;

    @StringRes(R.string.facebook_fail)
    String mFacebookFailedMessage;

    @StringRes(R.string.network_unavailbale_message)
    String mNetworkUnavailableMessage;

    @StringRes(R.string.auth_error_message)
    String mAuthErrorMessage;

    @StringRes(R.string.auth_success)
    String mAuthSuccessMessage;

    @Bean
    DataManager mDataManager;

    private Subscription mAuthSub;

    private LoginManager mFbLoginManager;

    private CallbackManager mFbCallbackManager;

    @Override
    protected void onStop() {
        super.onStop();
        unSubscribe();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Click(R.id.vk)
    void logInWithVk() {
        if(mIsInteractionAllowed) {
            disableInteraction();
            showTransparentBg();
            VKSdk.login(this);
        }
    }

    @Click(R.id.facebook)
    void logInWithFacebook() {
        if(mIsInteractionAllowed) {
            disableInteraction();
            showTransparentBg();
            if (mFbLoginManager == null) configureFacebookLogIn();
            mFbLoginManager.logInWithReadPermissions(AuthActivity.this, ConstantsManager.FACEBOOK_PERMISSIONS);
        }
    }

    @Override
    void handleInput() {
        if(mIsInteractionAllowed) {
            if (mNetworkStateChecker.isNetworkAvailable()) {
                disableInteraction();
                showTransparentBg();
                mAuthSub = getAuthStream()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(logInModel -> {
                            if (logInModel.getSuccess() == 1) {
                                saveLogInData(logInModel);
                                enter();
                            } else {
                                hideTransparentBg();
                                enableInteraction();
                                notifyUserWith(logInModel.getMessage());
                            }
                        }, throwable -> {
                            hideTransparentBg();
                            enableInteraction();
                            notifyUserWith(throwable.getLocalizedMessage().isEmpty()
                                    ? mAuthErrorMessage
                                    : throwable.getLocalizedMessage());
                        });
            } else {
                notifyUserWith(mNetworkUnavailableMessage);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                saveVkToken(res.accessToken);
                enter();
            }

            @Override
            public void onError(VKError error) {
                hideTransparentBg();
                enableInteraction();
                notifyUserWith(mVkFailedMessage);
            }
        })) {
            if (mFbCallbackManager != null)
                mFbCallbackManager.onActivityResult(requestCode, resultCode, data);
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void configureFacebookLogIn() {
        mFbLoginManager = LoginManager.getInstance();
        mFbCallbackManager = CallbackManager.Factory.create();
        mFbLoginManager.registerCallback(mFbCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Timber.d(loginResult.getAccessToken().getToken());
                saveFacebookToken(loginResult.getAccessToken().getToken());
                enter();
            }

            @Override
            public void onCancel() {
                hideTransparentBg();
                enableInteraction();
                Timber.d("Cancel FB");
            }

            @Override
            public void onError(FacebookException e) {
                hideTransparentBg();
                enableInteraction();
                notifyUserWith(mFacebookFailedMessage);
            }
        });
    }


    private void unSubscribe() {
        if (mAuthSub != null && !mAuthSub.isUnsubscribed()) mAuthSub.unsubscribe();
    }

    void notifyUserWith(String message) {
        Snackbar.make(mRootView, message, Snackbar.LENGTH_LONG).show();
    }

    private void enter() {
        Toast.makeText(this,mAuthSuccessMessage,Toast.LENGTH_LONG).show();
        EnterActivity_.intent(AuthActivity.this)
                .flags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .start();
    }

    private void saveLogInData(LogInModel logInModel) {
        saveCleverToken(logInModel.getToken());
        saveUserId(logInModel.getId());
    }

    abstract Observable<LogInModel> getAuthStream();
}
