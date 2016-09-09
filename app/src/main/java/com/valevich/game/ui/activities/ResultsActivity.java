package com.valevich.game.ui.activities;

import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.valevich.game.GameApplication;
import com.valevich.game.R;
import com.valevich.game.model.Player;
import com.valevich.game.ui.adapters.PlayersAdapter;
import com.valevich.game.util.ConstantsManager;

import org.androidannotations.annotations.AfterExtras;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;

import java.util.Locale;

@EActivity(R.layout.activity_results)
public class ResultsActivity extends AppCompatActivity {

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

    @StringRes(R.string.exit)
    String mExitMessage;

    @Extra
    Parcelable[] parcelablePlayers;

    @Extra
    int tourNumber;

    private boolean mIsGameFinished = false;

    private PlayersAdapter mPlayersAdapter;

    private Player[] mPlayers;

    @AfterExtras
    void setAdapter() {
        mPlayers = new Player[parcelablePlayers.length];
        for (int i = 0; i < parcelablePlayers.length; i++) {
            mPlayers[i] = (Player) parcelablePlayers[i];
        }
        mPlayersAdapter = new PlayersAdapter(mPlayers,tourNumber);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setupActionBar();
        setUpCongrats();
        setUpPlayersList();
        setUpButton();
    }

    @Click(R.id.finish_round)
    void finishRound() {

        if(mIsGameFinished) exit();
        else if(tourNumber == 3) showUserResults();
        else finish();

    }

    private void exit() {
        Intent intent = new Intent(this,EnterActivity_.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {

    }

    private void showUserResults() {
        mIsGameFinished = true;
        int place = -1;
        int coins = 0;
        int points = 0;
        for(int i = 0; i<mPlayers.length; i++) {
            Player player = mPlayers[i];
            if(player.getName().equals(GameApplication.getUserName())) {
                place = i;
                coins = player.getCoinsPortion(mPlayers);
                points = player.getTotalScore();
            }
        }
        GameFinalActivity_.intent(this)
                .coins(coins)
                .points(points)
                .place(place)
                .start();
    }

    private void setUpButton() {
        if(mIsGameFinished) mFinishButton.setText(mExitMessage);
    }

    private void setUpPlayersList() {
        mPlayersAdapter.setGameFinished(mIsGameFinished);
        mPlayersList.setLayoutManager(new LinearLayoutManager(this));
        mPlayersList.setAdapter(mPlayersAdapter);
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
        String congratsStart = "Поздравляем с";
        String congratsEnd = "местом!";
        int playersPlace = 0;
        for(int i = 0; i<mPlayers.length; i++) {
            Player player = mPlayers[i];
            if(player.getName().equals(GameApplication.getUserName())) playersPlace = i+1;
        }
        if(playersPlace == 2) congratsStart += "о";

        return String.format(Locale.getDefault(),"%s %d %s",congratsStart,playersPlace,congratsEnd);
    }
}
