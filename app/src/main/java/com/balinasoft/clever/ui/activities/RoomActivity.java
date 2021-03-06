package com.balinasoft.clever.ui.activities;

import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.balinasoft.clever.R;
import com.balinasoft.clever.model.Player;
import com.balinasoft.clever.network.model.QuestionApiModel;
import com.balinasoft.clever.ui.adapters.PlayersAdapterRoom;
import com.balinasoft.clever.util.AnimationHelper;
import com.balinasoft.clever.util.AvatarMapper;
import com.balinasoft.clever.util.ConstantsManager;
import com.balinasoft.clever.util.UrlFormatter;

import org.androidannotations.annotations.AfterExtras;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.socket.client.Socket;
import timber.log.Timber;

import static com.balinasoft.clever.GameApplication.getSocket;

@EActivity(R.layout.activity_room)
public class RoomActivity extends BaseActivity {

    @ViewById(R.id.root)
    FrameLayout mRootView;

    @ViewById(R.id.content)
    LinearLayout mContent;

    @ViewById(R.id.toolbar)
    Toolbar mToolbar;

    @ViewById(R.id.players_list)
    RecyclerView mPlayersList;

    @ViewById(R.id.start_game_button)
    TextView mStartGameButton;

    @ViewById(R.id.connected)
    TextView mConnectedLabel;

    @ViewById(R.id.countDownLabel)
    TextView mCountDownLabel;

    @StringRes(R.string.room_title_default)
    String mTitle;

    @StringRes(R.string.confirm_exit)
    String mConfirmExitMessage;

    @StringRes(R.string.yes)
    String mOk;

    @StringRes(R.string.new_player)
    String mNewPlayerMessage;

    @StringRes(R.string.player_left)
    String mPlayerLeftMessage;

    @StringRes(R.string.connected_start)
    String mConnectedStart;

    @StringRes(R.string.connected_end)
    String mConnectedEnd;

    @StringRes(R.string.socket_error)
    String mSocketErrorMessage;

    @StringRes(R.string.not_enough_players)
    String mNotEnoughPlayersMessage;

    @Bean
    AnimationHelper mAnimationHelper;

    @Bean
    AvatarMapper mAvatarMapper;

    @Extra
    Parcelable[] parcelablePlayers;

    @Extra
    int roomNumber;

    @Extra
    int bet;

    @Extra
    int maxPlayers;

    @Extra
    String idInRoom;

    private List<Player> mPlayers = new ArrayList<>();

    private Socket mSocket = getSocket();

    private int mSecondsLeft = 5;

    @AfterExtras
    void initExtras() {
        for (Parcelable parcelablePlayer : parcelablePlayers) {
            mPlayers.add((Player) parcelablePlayer);
        }
    }

    @AfterViews
    void setUpViews() {
        setUpStartGameButton();
        showHowManyConnected();
        setUpActionBar();
        setUpPlayersList();
    }

    @Click(R.id.start_game_button)
    void onStartGameClicked() {
        if (mPlayers.size() > 1) startGame();
        else if (mRootView != null)
            Snackbar.make(mRootView, mNotEnoughPlayersMessage, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Timber.d("Starting listening");
        startSocketListening();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopSocketListening();
    }

    @Override
    public void onBackPressed() {
        confirmExit();
    }

    private void showHowManyConnected() {
        String connectedString = String.format(Locale.getDefault(),
                "%s %d %s %d",
                mConnectedStart,
                mPlayers.size(),
                mConnectedEnd,
                maxPlayers);
        mConnectedLabel.setText(connectedString);
    }

    private void setUpPlayersList() {
        mPlayersList.setLayoutManager(new LinearLayoutManager(this));
        mPlayersList.setAdapter(new PlayersAdapterRoom(mPlayers));
    }

    private void setUpActionBar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            setTitle(mTitle);
        }
    }

    private void setUpStartGameButton() {
        if (parcelablePlayers.length > 1) mStartGameButton.setVisibility(View.GONE);
    }

    private void startGame() {
        JSONObject roomInfo = new JSONObject();
        try {
            roomInfo.put("room", roomNumber);
            mSocket.emit(ConstantsManager.START_GAME_EVENT, roomInfo);
        } catch (JSONException ignored) {

        }
    }

    private void startSocketListening() {
        mSocket.on(ConstantsManager.ROOM_MESSAGE_EVENT, this::onPlayersCountChanged);
        mSocket.on(ConstantsManager.START_GAME_EVENT, this::onGameStarted);
        mSocket.on(Socket.EVENT_DISCONNECT, args -> onDisconnect());
    }

    private void stopSocketListening() {
        mSocket.off(ConstantsManager.ROOM_MESSAGE_EVENT, this::onPlayersCountChanged);
        mSocket.off(ConstantsManager.START_GAME_EVENT, this::onGameStarted);
        mSocket.off(Socket.EVENT_DISCONNECT, args -> onDisconnect());
    }

    @UiThread
    void onDisconnect() {
        Timber.i("Disconnecting");
        if (RESUMED_ACTIVITIES_COUNT > 0 && mRootView != null) {
            Snackbar.make(mRootView, mSocketErrorMessage, Snackbar.LENGTH_SHORT)
                    .setCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            finish();
                        }
                    })
                    .show();
        }
    }

    @UiThread
    void onPlayersCountChanged(Object... args) {
        JSONObject data = (JSONObject) args[0];
        Timber.d("RoomMessage %s",data.toString());
        try {
            String name = data.getString("name");
            String id = data.getString("id");
            String message = data.getString("message");
            int avatarId = data.getInt("avatar");

            Player player = new Player();
            player.setName(name);
            player.setId(id);
            player.setImageResId(mAvatarMapper.getAvatar(avatarId));

            if (message.equals(mNewPlayerMessage)) {
                mPlayers.add(player);
            } else if (message.equals(mPlayerLeftMessage)) {
                mPlayers.remove(player);
            }
            mPlayersList.setAdapter(new PlayersAdapterRoom(mPlayers));
            showHowManyConnected();
        } catch (JSONException ignored) {

        }
    }

    @UiThread
    void onGameStarted(Object... args) {
        JSONObject data = (JSONObject) args[0];
        try {
            JSONArray jsonQuestions = data.getJSONArray("quests");
            List<QuestionApiModel> questions = getQuestions(jsonQuestions);
            startRound(questions);
        } catch (JSONException ignored) {

        }
        Timber.d("onGame started %n %s", data.toString());
    }

    private void startRound(List<QuestionApiModel> questions) {
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in_timer);

        mAnimationHelper.setAnimationListener(fadeIn,
                () -> {
                    if (--mSecondsLeft == -1) {
                        navigateToRound(questions);
                    } else {
                        String text;
                        if (mSecondsLeft == 0) {
                            text = "Старт!";
                            mCountDownLabel.setTextSize(mConnectedLabel.getTextSize() - 4);
                        } else {
                            text = String.valueOf(mSecondsLeft);
                        }
                        mCountDownLabel.setText(text);
                        mCountDownLabel.startAnimation(fadeIn);
                    }
                }, null, null);

        mCountDownLabel.setText(String.valueOf(mSecondsLeft));
        mContent.setVisibility(View.GONE);
        mCountDownLabel.setVisibility(View.VISIBLE);
        mCountDownLabel.startAnimation(fadeIn);
}

    private void navigateToRound(List<QuestionApiModel> questions) {
        Player[] pls = new Player[mPlayers.size()];
        pls = mPlayers.toArray(pls);
        QuestionApiModel[] q = new QuestionApiModel[questions.size()];
        q = questions.toArray(q);
        Timber.d("players count %d",pls.length);
        TourActivityOnline_.intent(this)
                .parcelablePlayers(pls)
                .bet(bet)
                .room(roomNumber)
                .idInRoom(idInRoom)
                .parcelableQuestions(q)
                .start();
        finish();
    }

    private List<QuestionApiModel> getQuestions(JSONArray jsonQuestions) throws JSONException {
        List<QuestionApiModel> questions = new ArrayList<>();
        for (int i = 0; i < jsonQuestions.length(); i++) {
            QuestionApiModel question = new QuestionApiModel();
            JSONObject jsonQuestion = jsonQuestions.getJSONObject(i);
            question.setServerId(jsonQuestion.getString("_id"));
            question.setAnswers(jsonQuestion.getString("answers"));
            String media = jsonQuestion.getString("media");
            if (media != null && !media.equals("null"))
                question.setMedia(UrlFormatter.getQuestionUrlFrom(media));
            question.setMediaType(jsonQuestion.getString("media_type"));
            question.setRightAnswer(jsonQuestion.getString("right_answer"));
            question.setTextQuest(jsonQuestion.getString("text_quest"));
            question.setThemeQuest(jsonQuestion.getString("theme_quest"));
            question.setNumber(jsonQuestion.getInt("number"));
            question.setRound(jsonQuestion.getInt("round"));
            questions.add(question);
        }
        return questions;
    }

    private void confirmExit() {
        Snackbar.make(mRootView, mConfirmExitMessage, Snackbar.LENGTH_LONG)
                .setAction(mOk, view -> leave())
                .show();
    }

    private void leave() {
        JSONObject roomInfo = new JSONObject();
        try {
            roomInfo.put("room", roomNumber);
            Timber.d("leaving.........%s", roomInfo.toString());
            mSocket.emit(ConstantsManager.ROOM_LEAVING_EVENT, roomInfo);
            finish();
        } catch (JSONException ignored) {

        }
    }
}
