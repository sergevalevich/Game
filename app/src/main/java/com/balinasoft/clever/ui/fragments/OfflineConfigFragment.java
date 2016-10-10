package com.balinasoft.clever.ui.fragments;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.balinasoft.clever.R;
import com.balinasoft.clever.model.Player;
import com.balinasoft.clever.storage.model.Question;
import com.balinasoft.clever.ui.activities.TourActivityOffline_;
import com.balinasoft.clever.util.ConstantsManager;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.net.SocketTimeoutException;
import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.balinasoft.clever.GameApplication.getUserCoins;

@EFragment(R.layout.fragment_offline_config)
public class OfflineConfigFragment extends ConfigFragmentBase {

    @ViewById(R.id.toolbar)
    Toolbar mToolbar;

    private Subscription mSubscription;

    @Override
    void setUpOtherViews() {
        setupActionBar();
    }

    @Override
    boolean areEnoughCoins() {
        return getUserCoins() >= mBet;
    }


    @Override
    public void onStart() {
        super.onStart();
        if (mSubscription != null) {
            setButtonClickable(false);
            notifyLoading(View.VISIBLE);
            getQuestions();
        }
    }

    @Override
    public void onStop() {
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
        List<Player> players = Player.get(mPlayersCount + 1,mBet);
        Player[] pls = new Player[players.size()];
        pls = players.toArray(pls);
        TourActivityOffline_.intent(this)
                .bet(mBet)
                .parcelableQuestions(questions)
                .parcelablePlayers(pls)
                .start();
        getActivity().finish();
    }

    private void setupActionBar() {
        AppCompatActivity activity = (AppCompatActivity)getActivity();
        activity.setSupportActionBar(mToolbar);
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            activity.setTitle("");
        }
    }

    @Override
    void createGame() {
        setButtonClickable(false);
        notifyLoading(View.VISIBLE);
        getQuestions();
    }
}
