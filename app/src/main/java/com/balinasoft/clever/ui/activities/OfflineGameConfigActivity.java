package com.balinasoft.clever.ui.activities;

import android.view.View;

import com.balinasoft.clever.R;
import com.balinasoft.clever.storage.model.Question;
import com.balinasoft.clever.util.ConstantsManager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;

import java.net.SocketTimeoutException;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@EActivity(R.layout.activity_offline_config)
public class OfflineGameConfigActivity extends ConfigActivityBase {

    private Subscription mSubscription;

    @Override
    @AfterViews
    void setUpOtherViews() {

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mSubscription != null) {
            setButtonClickable(false);
            notifyLoading(View.VISIBLE);
            getQuestions();
        }
    }

    @Override
    protected void onStop() {
        if (mSubscription != null && !mSubscription.isUnsubscribed()) mSubscription.unsubscribe();
        clearQuestions();
        setButtonClickable(true);
        notifyLoading(View.GONE);
        super.onStop();
    }

    private void getQuestions() {
        mSubscription = mDataManager.getQuestions()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onNext, this::onError, this::onCompleted);
    }

    private void onNext(Question question) {
        mQuestions.add(question);
    }

    private void onError(Throwable throwable) {
        clearQuestions();
        setButtonClickable(true);
        notifyLoading(View.GONE);
        notifyUserWith(throwable instanceof SocketTimeoutException
                ? mNetworkErrorMessage
                : throwable.getMessage());
    }

    private void onCompleted() {
        if (mQuestions.size() == ConstantsManager.MAX_QUESTIONS_COUNT)
            startGame();
        else {
            clearQuestions();
            setButtonClickable(true);
            notifyLoading(View.GONE);
        }
    }

    private void startGame() {
        Question[] questions = new Question[mQuestions.size()];
        questions = mQuestions.toArray(questions);
        TourActivityOffline_.intent(this)
                .enemiesCount(mPlayersCount + 1)
                .bet(mBet)
                .parcelableQuestions(questions)
                .start();
        finish();
    }

    @Override
    void createGame() {
        setButtonClickable(false);
        notifyLoading(View.VISIBLE);
        getQuestions();
    }
}
