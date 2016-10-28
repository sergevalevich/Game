package com.balinasoft.clever.ui.activities;

import android.content.Intent;
import android.os.Bundle;
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
import com.facebook.GraphRequest;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import static com.balinasoft.clever.GameApplication.getFireBaseToken;
import static com.balinasoft.clever.GameApplication.getFacebookToken;
import static com.balinasoft.clever.GameApplication.getUserEmail;
import static com.balinasoft.clever.GameApplication.getVKToken;
import static com.balinasoft.clever.GameApplication.saveFacebookToken;
import static com.balinasoft.clever.GameApplication.saveVkToken;
import static com.balinasoft.clever.GameApplication.setOnlineMode;
import static com.balinasoft.clever.GameApplication.setUserEmail;
import static com.facebook.Profile.getCurrentProfile;

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
            VKSdk.login(this, ConstantsManager.VK_PERMISSIONS);
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
    void onInputValid() {
        if (mIsInteractionAllowed) auth(getAuthStream());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                saveVkToken(res.accessToken);
                setUserEmail(res.email);
                getVkFirstName();
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

    private void getVkFirstName() {
        VKRequest request = VKApi.users().get();
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                auth(logInWithVK(getVkFirstName(response.json)));
            }

            @Override
            public void onError(VKError error) {
                auth(logInWithVK(ConstantsManager.DEFAULT_USER_NAME));
            }
        });
    }

    private String getVkFirstName(JSONObject json) {
        String firstName = ConstantsManager.DEFAULT_USER_NAME;
        try {
            JSONArray array = json.getJSONArray("response");
            JSONObject user = array.getJSONObject(0);
            firstName = user.getString("first_name");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return firstName;
    }

    private void auth(Observable<LogInModel> authStream) {
        if (mNetworkStateChecker.isNetworkAvailable()) {
            disableInteraction();
            showTransparentBg();
            mAuthSub = authStream
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(logInModel -> {
                        enter();
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

    private Observable<LogInModel> logInWithFB(String firstName) {
        return mDataManager.logInWithFB(getFireBaseToken(), getFacebookToken(), getUserEmail(),firstName);
    }

    private Observable<LogInModel> logInWithVK(String firstName) {
        return mDataManager.logInWithVK(getFireBaseToken(), getVKToken(), getUserEmail(),firstName);
    }

    private void configureFacebookLogIn() {
        mFbLoginManager = LoginManager.getInstance();
        mFbCallbackManager = CallbackManager.Factory.create();
        mFbLoginManager.registerCallback(mFbCallbackManager, new FacebookCallback<LoginResult>() {

            private ProfileTracker mProfileTracker;

            @Override
            public void onSuccess(LoginResult loginResult) {
                Timber.d(loginResult.getAccessToken().getToken());
                saveFacebookToken(loginResult.getAccessToken().getToken());

                if(Profile.getCurrentProfile() == null) {
                    Timber.d("PROFILE IS NULL");
                    mProfileTracker = new ProfileTracker() {
                        @Override
                        protected void onCurrentProfileChanged(Profile profile, Profile profile2) {
                            getFbEmail(loginResult,profile2.getFirstName());
                            mProfileTracker.stopTracking();
                        }
                    };
                } else {
                    Timber.d("PROFILE IS NOT NULL");
                    getFbEmail(loginResult,getCurrentProfile().getFirstName());
                }
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

    private void getFbEmail(LoginResult loginResult, String userName) {
        GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), (object, response) -> {
            Bundle facebookData = getFacebookData(object);
            setUserEmail(facebookData.getString("email"));
            auth(logInWithFB(userName));
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "email");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private Bundle getFacebookData(JSONObject object) {
        Bundle bundle = new Bundle();
        if (object.has("email"))
            try {
                bundle.putString("email", object.getString("email"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        return bundle;
    }

    private void unSubscribe() {
        if (mAuthSub != null && !mAuthSub.isUnsubscribed()) mAuthSub.unsubscribe();
    }

    void notifyUserWith(String message) {
        Snackbar.make(mRootView, message, Snackbar.LENGTH_LONG).show();
    }

    private void enter() {
        Toast.makeText(this,mAuthSuccessMessage,Toast.LENGTH_LONG).show();
        setOnlineMode(true);
        MainActivity_.intent(AuthActivity.this)
                .flags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .start();
    }

    abstract Observable<LogInModel> getAuthStream();
}
