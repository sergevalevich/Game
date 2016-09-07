package com.valevich.game.ui.activities;

import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.valevich.game.R;
import com.valevich.game.model.Player;
import com.valevich.game.ui.adapters.PlayersAdapter;
import com.valevich.game.util.ConstantsManager;

import org.androidannotations.annotations.AfterExtras;
import org.androidannotations.annotations.AfterViews;
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

    private PlayersAdapter mPlayersAdapter;

    private Player[] mPlayers;

    @AfterExtras
    void setAdapter() {
        mPlayers = new Player[parcelablePlayers.length];
        for (int i = 0; i < parcelablePlayers.length; i++) {
            mPlayers[i] = (Player) parcelablePlayers[i];
        }
        mPlayersAdapter = new PlayersAdapter(mPlayers);
    }

    @AfterViews
    void setUpViews() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        setupActionBar();
        setUpCongrats();
        setUpPlayersList();
        setUpButton();
    }

    @Click(R.id.finish_round)
    void finishRound() {
        if(tourNumber == 3) {
            showUserResults();
        } else if (tourNumber == 4) {
            exit();
        }
        else finish();
    }

    private void exit() {
        Intent intent = new Intent(this,SplashActivity_.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {

    }

    private void showUserResults() {
        tourNumber++;
        GameFinalActivity_.intent(this).start();
    }

    private void setUpButton() {
        if(tourNumber == 4) mFinishButton.setText(mExitMessage);
    }

    private void setUpPlayersList() {
        mPlayersAdapter.setTourNumber(tourNumber);
        mPlayersList.setLayoutManager(new LinearLayoutManager(this));
        mPlayersList.setAdapter(mPlayersAdapter);
    }

    private void setupActionBar() {
        mToolbar.setText(tourNumber < 4
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
            if(player.getName().equals(ConstantsManager.DEFAULT_USER_NAME)) playersPlace = i+1;
        }
        if(playersPlace == 2) congratsStart += "о";

        return String.format(Locale.getDefault(),"%s %d %s",congratsStart,playersPlace,congratsEnd);
    }
}
