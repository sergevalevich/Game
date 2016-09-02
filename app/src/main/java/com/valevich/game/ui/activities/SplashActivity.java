package com.valevich.game.ui.activities;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.valevich.game.R;
import com.valevich.game.network.RestService;
import com.valevich.game.network.model.QuestionApiModel;
import com.valevich.game.storage.model.Question;
import com.valevich.game.util.NetworkStateChecker;
import com.valevich.game.util.Preferences_;
import com.valevich.game.util.UrlFormatter;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

@EActivity(R.layout.activity_splash)
public class SplashActivity extends AppCompatActivity {

    @ViewById(R.id.root)
    RelativeLayout mRootView;

    @ViewById(R.id.title)
    TextView mTitle;

    @ViewById(R.id.offline_game_btn)
    TextView mOfflineGameButton;

    @ViewById(R.id.online_game_btn)
    TextView mOnlineGameButton;

    @StringRes(R.string.network_unavailbale_message)
    String mNetworkUnavailableMessage;

    @Bean
    RestService mRestService;

    @Bean
    NetworkStateChecker mNetworkStateChecker;

    private Snackbar mSnackbar;

    private Subscription mSubscription;

    private Observable<Boolean> mLoadQuestionsObservable;

    @Pref
    static Preferences_ mPreferences;

    @Click(R.id.offline_game_btn)
    void onOfflinePicked() {
        toggleButtonsBlock(false);
        dismissButtons();
    }

    @AfterViews
    void setUpViews() {

    }

    @AfterInject
    void initObservable() {
        if(isSyncNeeded())
            mLoadQuestionsObservable = getLoadQuestionsObservable();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isSyncNeeded()) {
            if (mNetworkStateChecker.isNetworkAvailable()) {
                notifyLoading();
                loadQuestions();
            } else {
                notifyUserWith(mNetworkUnavailableMessage);
                showViews();
            }
        } else showViews();
    }

    @Override
    protected void onStop() {
        super.onStop();
        setButtonsVisibility(View.INVISIBLE);
        if (mSubscription != null && !mSubscription.isUnsubscribed()) mSubscription.unsubscribe();
    }

    private void showViews() {
        Animation slideDownAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        Animation slideInLeft = AnimationUtils.loadAnimation(this, R.anim.slide_in_left);
        Animation slideInRight = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);


        slideInLeft.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                setButtonsVisibility(View.VISIBLE);
                mOfflineGameButton.startAnimation(slideInRight);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


        slideDownAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mTitle.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mOnlineGameButton.startAnimation(slideInLeft);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mTitle.startAnimation(slideDownAnimation);
    }

    private void dismissButtons() {
        Animation slideOutLeft = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);
        Animation slideOutRight = AnimationUtils.loadAnimation(this, R.anim.slide_out_right);

        slideOutLeft.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mOfflineGameButton.startAnimation(slideOutRight);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                setButtonsVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        slideOutRight.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                setButtonsVisibility(View.INVISIBLE);
                toggleButtonsBlock(true);
                navigateToGameConfig();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mOnlineGameButton.startAnimation(slideOutLeft);
    }

    private void toggleButtonsBlock(boolean isClickable) {
        mOfflineGameButton.setClickable(isClickable);
        mOnlineGameButton.setClickable(isClickable);
    }

    private void navigateToGameConfig() {
        OfflineGameConfigActivity_.intent(this).start();
    }

    private void setButtonsVisibility(int visibility) {
        mOfflineGameButton.setVisibility(visibility);
        mOnlineGameButton.setVisibility(visibility);
    }

    private void loadQuestions() {
        mSubscription = mLoadQuestionsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        isSuccessful -> {
                        },
                        throwable -> {
                            dismissLoading();
                            notifyUserWith(throwable.getLocalizedMessage());
                        },
                        () -> {
                            mPreferences.isSyncNeeded().put(false);
                            dismissLoading();
                            showViews();
                        });
    }

    private Observable<Boolean> getLoadQuestionsObservable() {
        return Observable
                .zip(mRestService.loadQuestions()
                                .flatMap(Observable::from)
                                .flatMap(Question::insertQuestion)
                                .filter(questionApiModel -> questionApiModel.getMediaType() != null)
                                .flatMap(questionApiModel -> mRestService.loadQuestionMedia(UrlFormatter.getFileNameFrom(
                                        questionApiModel.getMediaType()),
                                        UrlFormatter.getFileNameFrom(questionApiModel.getMediaUrl()))),
                        mRestService.loadQuestions()
                                .flatMap(Observable::from)
                                .flatMap(Question::insertQuestion)
                                .filter(questionApiModel -> questionApiModel.getMediaType() != null)
                                .flatMap(this::getFileName), this::writeResponseBodyToDisk)
                .cache();
    }



    private Observable<String> getFileName(QuestionApiModel questionApiModel) {
        return Observable.just(UrlFormatter.getFileNameFrom(questionApiModel.getMediaUrl()));
    }

    private boolean writeResponseBodyToDisk(ResponseBody body, String filename) {
        try {
            // todo change the file location/name according to your needs
            File file = new File(getFilesDir() + File.separator + filename);

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(file);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;
                }

                outputStream.flush();

                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }

    private boolean isSyncNeeded() {
        return mPreferences.isSyncNeeded().get();
    }

    private void notifyUserWith(String message) {
        Snackbar.make(mRootView, message, Snackbar.LENGTH_LONG).show();
    }

    private void notifyLoading() {
        mSnackbar = Snackbar.make(mRootView,"Загрузка вопросов...",Snackbar.LENGTH_INDEFINITE);
        mSnackbar.show();
    }

    private void dismissLoading() {
        mSnackbar.dismiss();
    }

}
