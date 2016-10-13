package com.balinasoft.clever.ui.activities;

import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.balinasoft.clever.R;
import com.balinasoft.clever.model.Player;
import com.balinasoft.clever.ui.adapters.PlayersAdapterResults;
import com.balinasoft.clever.util.ConstantsManager;

import org.androidannotations.annotations.AfterExtras;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

import static com.balinasoft.clever.GameApplication.getUserId;

@EActivity(R.layout.activity_results)
public class ResultsActivity extends BaseActivity {

    @ViewById(R.id.players_list)
    RecyclerView mPlayersList;

    @ViewById(R.id.toolbar)
    TextView mToolbar;

    @ViewById(R.id.finish_round)
    TextView mFinishButton;

    @ViewById(R.id.congrats)
    TextView mCongrats;

    @StringRes(R.string.results_title_round)
    String mRoundFinishedTitle;

    @StringRes(R.string.results_title_game)
    String mGameFinishedTitle;

    @StringRes(R.string.finish_round)
    String mNextRoundMessage;

    @StringRes(R.string.exit)
    String mExitMessage;

    @StringRes(R.string.socket_error)
    String mSocketErrorMessage;

    @Extra
    Parcelable[] parcelablePlayers;

    @Extra
    int tourNumber;

    @Extra
    boolean isOnline;

    private boolean mIsGameFinished = false;

    private PlayersAdapterResults mPlayersAdapterResults;

    private Subscription mSubscription;

    private Player[] mPlayers;

    //private Socket mSocket;

    @AfterExtras
    void setAdapter() {
        mPlayers = new Player[parcelablePlayers.length];
        for (int i = 0; i < parcelablePlayers.length; i++) {
            mPlayers[i] = (Player) parcelablePlayers[i];
        }
        mPlayersAdapterResults = new PlayersAdapterResults(mPlayers,tourNumber);
        //if(isOnline) mSocket = getSocket();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //if(isOnline) startSocketListening();
        setupActionBar();
        setUpCongrats();
        setUpPlayersList();
        setUpButton();
        if(isOnline && tourNumber < 3) startTimer();
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        if(isOnline) stopSocketListening();
//    }

    @Override
    protected void onDestroy() {
        if(mSubscription != null && !mSubscription.isUnsubscribed()) mSubscription.unsubscribe();
        super.onDestroy();
    }

    @Click(R.id.finish_round)
    void finishRound() {

        if(mIsGameFinished) exit();
        else if(tourNumber == 3) showUserResults();
        else finish();

    }

    @Override
    public void onBackPressed() {

    }

//    @UiThread
//    void onMessageReceived(Object... args) {
//        JSONObject data = (JSONObject) args[0];
//        try {
//            String message = data.getString("message");
//            Timber.d("Message in tour");
//            if(message.equals(mUserLeftMessage)) {
//                mEnemiesCount--;
//            }
//            int success = data.getInt("success");
//            if(success == 1 && haveAllAnswered()) boostTimer();
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

    private void exit() {
        if(isOnline) navigateToLobby();
        else navigateToEnter();
    }

    private void navigateToEnter() {
        EnterActivity_.intent(this)
                .flags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .start();
    }

    private void navigateToLobby() {
        MainActivity_.intent(this)
                .flags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .start();
    }

//    private void startSocketListening() {
//        mSocket.on(ConstantsManager.ROOM_MESSAGE_EVENT, this::onMessageReceived);
//    }
//
//    private void stopSocketListening() {
//        mSocket.off(ConstantsManager.ROOM_MESSAGE_EVENT, this::onMessageReceived);
//    }

    private void showUserResults() {
        mIsGameFinished = true;
        int place = -1;
        int coins = 0;
        int points = 0;
        for(int i = 0; i<mPlayers.length; i++) {
            Player player = mPlayers[i];
            if(player.getId().equals(getUserId())) {
                place = i;
                coins = player.getCoinsPortion();
                points = player.getTotalScore();
            }
        }
        GameFinalActivity_.intent(this).coins(coins).points(points).place(place).start();
    }

    private void setUpButton() {
        if(tourNumber > 2  || !isOnline) {
            String buttonTitle;
            if (mIsGameFinished) buttonTitle = mExitMessage;
            else if (tourNumber == 3) buttonTitle = mGameFinishedTitle;
            else buttonTitle = mNextRoundMessage;
            mFinishButton.setText(buttonTitle);
            mFinishButton.setVisibility(View.VISIBLE);
        } else {
            mFinishButton.setVisibility(View.GONE);
        }
    }

    private void setUpPlayersList() {
        mPlayersAdapterResults.setGameFinished(mIsGameFinished);
        mPlayersList.setLayoutManager(new LinearLayoutManager(this));
        mPlayersList.setAdapter(mPlayersAdapterResults);
    }

    private void setupActionBar() {
        mToolbar.setText(!mIsGameFinished
                ? String.format(Locale.getDefault(), "%s %d", mRoundFinishedTitle, tourNumber)
                : mGameFinishedTitle);
    }

    private void setUpCongrats() {
        mCongrats.setText(getCongratsString());
    }

    private String getCongratsString() {
        String congratsStart = ConstantsManager.CONGRATS_START;
        String congratsEnd = ConstantsManager.CONGRATS_END;
        int playersPlace = 0;
        for(int i = 0; i<mPlayers.length; i++) {
            Player player = mPlayers[i];
            if(player.getId().equals(getUserId())) playersPlace = i+1;
        }
        if(playersPlace == 2) congratsStart += ConstantsManager.CONGRATS_ENDING;

        return String.format(Locale.getDefault(),"%s %d %s",congratsStart,playersPlace,congratsEnd);
    }

    private void startTimer() {
        Toast.makeText(this,"Следующий раунд начнётся через 10 секунд",Toast.LENGTH_SHORT).show();
        mSubscription = Observable.interval(10,TimeUnit.SECONDS)
                .take(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(tick -> Timber.d("tick"),
                        throwable -> Timber.d("error %s",throwable.getLocalizedMessage()),
                        this::finish);
    }
}
